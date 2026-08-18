package com.hyobin.neomusic.auth.application

import com.hyobin.neomusic.auth.application.port.inbound.LoginCommand
import com.hyobin.neomusic.auth.application.port.inbound.LoginResult
import com.hyobin.neomusic.auth.application.port.inbound.LoginUseCase
import com.hyobin.neomusic.auth.application.port.inbound.ResetPasswordCommand
import com.hyobin.neomusic.auth.application.port.inbound.ResetPasswordUseCase
import com.hyobin.neomusic.auth.application.port.inbound.SignupCommand
import com.hyobin.neomusic.auth.application.port.inbound.SignupUseCase
import com.hyobin.neomusic.auth.application.port.outbound.LoadMemberPort
import com.hyobin.neomusic.auth.application.port.outbound.PasswordEncoderPort
import com.hyobin.neomusic.auth.application.port.outbound.SaveMemberPort
import com.hyobin.neomusic.auth.application.port.outbound.TokenPort
import com.hyobin.neomusic.auth.domain.AccountLockedException
import com.hyobin.neomusic.auth.domain.InvalidCredentialsException
import com.hyobin.neomusic.auth.domain.Member
import com.hyobin.neomusic.auth.domain.MemberNotFoundException
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
) : SignupUseCase, LoginUseCase, ResetPasswordUseCase {

    @Transactional
    override fun signup(command: SignupCommand): Member {
        val nickname = Nickname.of(command.nickname)
        if (loadMemberPort.existsByNickname(nickname)) {
            throw NicknameAlreadyExistsException(nickname)
        }
        val member = Member.create(nickname, passwordEncoder.encode(command.password))
        return saveMemberPort.save(member)
    }

    // 비번 오류 시 실패 카운트 증가를 저장한 뒤 예외를 던지는데,
    // 예외가 트랜잭션을 롤백하면 그 증가가 취소돼 계정이 영원히 안 잠긴다.
    // → InvalidCredentialsException 은 롤백 대상에서 제외해 실패 누적이 커밋되게 한다.
    @Transactional(noRollbackFor = [InvalidCredentialsException::class])
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

    @Transactional
    override fun resetPassword(command: ResetPasswordCommand) {
        val member = loadMemberPort.findById(command.targetMemberId)
            ?: throw MemberNotFoundException(command.targetMemberId)
        // 도메인이 해시 교체 + 실패 카운트/잠금 해제를 함께 책임진다
        member.changePassword(passwordEncoder.encode(command.newPassword))
        saveMemberPort.save(member)
    }
}
