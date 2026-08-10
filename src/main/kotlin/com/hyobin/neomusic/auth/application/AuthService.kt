package com.hyobin.neomusic.auth.application

import com.hyobin.neomusic.auth.application.port.inbound.LoginCommand
import com.hyobin.neomusic.auth.application.port.inbound.LoginResult
import com.hyobin.neomusic.auth.application.port.inbound.LoginUseCase
import com.hyobin.neomusic.auth.application.port.inbound.SignupCommand
import com.hyobin.neomusic.auth.application.port.inbound.SignupUseCase
import com.hyobin.neomusic.auth.application.port.outbound.LoadMemberPort
import com.hyobin.neomusic.auth.application.port.outbound.PasswordEncoderPort
import com.hyobin.neomusic.auth.application.port.outbound.SaveMemberPort
import com.hyobin.neomusic.auth.application.port.outbound.TokenPort
import com.hyobin.neomusic.auth.domain.AccountLockedException
import com.hyobin.neomusic.auth.domain.InvalidCredentialsException
import com.hyobin.neomusic.auth.domain.Member
import com.hyobin.neomusic.auth.domain.Nickname
import com.hyobin.neomusic.auth.domain.NicknameAlreadyExistsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

/**
 * 인증 유스케이스. 포트를 조합해 회원가입/로그인 흐름을 orchestration 한다.
 * 비밀번호 해싱/검증, 토큰 발급, 잠금 정책이 여기서 엮인다.
 */
@Service
class AuthService(
    private val saveMemberPort: SaveMemberPort,
    private val loadMemberPort: LoadMemberPort,
    private val passwordEncoder: PasswordEncoderPort,
    private val tokenPort: TokenPort,
    private val clock: Clock,
) : SignupUseCase, LoginUseCase {

    @Transactional
    override fun signup(command: SignupCommand): Member {
        val nickname = Nickname.of(command.nickname)
        if (loadMemberPort.existsByNickname(nickname)) {
            throw NicknameAlreadyExistsException(nickname)
        }
        val member = Member.create(nickname, passwordEncoder.encode(command.password))
        return saveMemberPort.save(member)
    }

    @Transactional
    override fun login(command: LoginCommand): LoginResult {
        val nickname = Nickname.of(command.nickname)
        // 존재하지 않는 닉네임도 '자격 증명 오류'로 통일 (어느 쪽이 틀렸는지 노출 안 함)
        val member = loadMemberPort.findByNickname(nickname) ?: throw InvalidCredentialsException()

        val now = clock.instant()
        if (member.isLocked(now)) {
            throw AccountLockedException(member.lockedUntil!!)
        }

        if (!passwordEncoder.matches(command.password, member.passwordHash)) {
            member.recordLoginFailure(now)   // 실패 누적 → 한계 도달 시 잠김
            saveMemberPort.save(member)
            throw InvalidCredentialsException()
        }

        member.recordLoginSuccess()          // 실패 카운트/잠금 초기화
        saveMemberPort.save(member)
        val token = tokenPort.issue(member)
        return LoginResult(accessToken = token, member = member)
    }
}
