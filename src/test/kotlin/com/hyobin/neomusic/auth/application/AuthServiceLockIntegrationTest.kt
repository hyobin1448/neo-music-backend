package com.hyobin.neomusic.auth.application

import com.hyobin.neomusic.auth.application.port.inbound.LoginCommand
import com.hyobin.neomusic.auth.application.port.inbound.ResetPasswordCommand
import com.hyobin.neomusic.auth.application.port.inbound.SignupCommand
import com.hyobin.neomusic.auth.domain.AccountLockedException
import com.hyobin.neomusic.auth.domain.InvalidCredentialsException
import com.hyobin.neomusic.auth.domain.Member
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/**
 * 로그인 실패 누적 잠금을 '실제 DB 커밋'으로 검증하는 회귀 테스트.
 *
 * 일부러 @Transactional 을 붙이지 않는다 — 붙이면 실패 카운트 증가가
 * 테스트 트랜잭션 안에만 있어 커밋 롤백 여부(이 버그의 핵심)를 검증할 수 없다.
 * (login 이 예외로 롤백되면 실패 카운트가 취소돼 계정이 영원히 안 잠기던 버그)
 */
@SpringBootTest
class AuthServiceLockIntegrationTest @Autowired constructor(
    private val authService: AuthService,
) {

    @Test
    fun `비번을 한계까지 틀리면 실패가 커밋되어 계정이 잠긴다`() {
        val nickname = "잠금회귀테스트"
        authService.signup(SignupCommand(nickname, "goodpw"))

        // 각 실패는 401 을 던지지만, 실패 카운트는 커밋되어 누적되어야 한다
        repeat(Member.MAX_FAILED_ATTEMPTS) {
            shouldThrow<InvalidCredentialsException> {
                authService.login(LoginCommand(nickname, "wrong"))
            }
        }

        // 한계 도달 → 올바른 비번이어도 잠겨서 로그인 불가
        shouldThrow<AccountLockedException> {
            authService.login(LoginCommand(nickname, "goodpw"))
        }
    }

    @Test
    fun `관리자 초기화 후에는 다시 로그인된다`() {
        val nickname = "초기화회귀테스트"
        val member = authService.signup(SignupCommand(nickname, "oldpw"))

        repeat(Member.MAX_FAILED_ATTEMPTS) {
            shouldThrow<InvalidCredentialsException> {
                authService.login(LoginCommand(nickname, "wrong"))
            }
        }

        authService.resetPassword(ResetPasswordCommand(member.id!!, "newpw"))

        // 잠금 해제 + 새 비번으로 정상 로그인 (예외 없이 통과하면 성공)
        authService.login(LoginCommand(nickname, "newpw"))
    }
}
