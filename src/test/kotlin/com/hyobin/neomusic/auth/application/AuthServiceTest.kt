package com.hyobin.neomusic.auth.application

import com.hyobin.neomusic.auth.application.port.inbound.LoginCommand
import com.hyobin.neomusic.auth.application.port.inbound.SignupCommand
import com.hyobin.neomusic.auth.application.port.outbound.LoadMemberPort
import com.hyobin.neomusic.auth.application.port.outbound.PasswordEncoderPort
import com.hyobin.neomusic.auth.application.port.outbound.SaveMemberPort
import com.hyobin.neomusic.auth.application.port.outbound.TokenPort
import com.hyobin.neomusic.auth.domain.AccountLockedException
import com.hyobin.neomusic.auth.domain.InvalidCredentialsException
import com.hyobin.neomusic.auth.domain.Member
import com.hyobin.neomusic.auth.domain.Nickname
import com.hyobin.neomusic.auth.domain.NicknameAlreadyExistsException
import com.hyobin.neomusic.auth.domain.PasswordHash
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class AuthServiceTest : StringSpec() {

    // 테스트마다 새 인스턴스(=새 목) → 상태 격리
    override fun isolationMode() = IsolationMode.InstancePerTest

    private val saveMemberPort = mockk<SaveMemberPort>()
    private val loadMemberPort = mockk<LoadMemberPort>()
    private val passwordEncoder = mockk<PasswordEncoderPort>()
    private val tokenPort = mockk<TokenPort>()
    private val now = Instant.parse("2026-01-01T00:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val service = AuthService(saveMemberPort, loadMemberPort, passwordEncoder, tokenPort, clock)

    init {
        "회원가입하면 비밀번호가 해시되어 저장된다" {
            every { loadMemberPort.existsByNickname(any()) } returns false
            every { passwordEncoder.encode("pw1234") } returns PasswordHash("HASH")
            every { saveMemberPort.save(any()) } answers { firstArg() }

            val member = service.signup(SignupCommand("할머니", "pw1234"))

            member.passwordHash shouldBe PasswordHash("HASH")
            verify { saveMemberPort.save(any()) }
        }

        "중복 닉네임이면 회원가입에 실패한다" {
            every { loadMemberPort.existsByNickname(any()) } returns true

            shouldThrow<NicknameAlreadyExistsException> {
                service.signup(SignupCommand("할머니", "pw1234"))
            }
        }

        "로그인 성공 시 토큰을 발급한다" {
            val member = Member.create(Nickname.of("할머니"), PasswordHash("HASH"))
            every { loadMemberPort.findByNickname(Nickname.of("할머니")) } returns member
            every { passwordEncoder.matches("pw1234", PasswordHash("HASH")) } returns true
            every { saveMemberPort.save(any()) } answers { firstArg() }
            every { tokenPort.issue(any()) } returns "jwt-token"

            val result = service.login(LoginCommand("할머니", "pw1234"))

            result.accessToken shouldBe "jwt-token"
        }

        "비밀번호가 틀리면 실패를 기록하고 예외를 던진다" {
            val member = Member.create(Nickname.of("할머니"), PasswordHash("HASH"))
            every { loadMemberPort.findByNickname(any()) } returns member
            every { passwordEncoder.matches(any(), any()) } returns false
            every { saveMemberPort.save(any()) } answers { firstArg() }

            shouldThrow<InvalidCredentialsException> {
                service.login(LoginCommand("할머니", "wrong"))
            }
            member.failedAttempts shouldBe 1
            verify { saveMemberPort.save(member) }
        }

        "존재하지 않는 닉네임이면 자격 증명 오류를 던진다" {
            every { loadMemberPort.findByNickname(any()) } returns null

            shouldThrow<InvalidCredentialsException> {
                service.login(LoginCommand("없음", "pw1234"))
            }
        }

        "잠긴 계정으로 로그인하면 잠금 예외를 던진다" {
            val member = Member.create(Nickname.of("할머니"), PasswordHash("HASH"))
            repeat(Member.MAX_FAILED_ATTEMPTS) { member.recordLoginFailure(now) }  // 잠김 상태로 만들기
            every { loadMemberPort.findByNickname(any()) } returns member

            shouldThrow<AccountLockedException> {
                service.login(LoginCommand("할머니", "pw1234"))
            }
        }
    }
}
