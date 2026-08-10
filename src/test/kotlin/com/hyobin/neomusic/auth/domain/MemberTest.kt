package com.hyobin.neomusic.auth.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.Instant

class MemberTest : StringSpec({

    val now = Instant.parse("2026-01-01T00:00:00Z")

    fun newMember() = Member.create(Nickname.of("할머니"), PasswordHash("hashed"))

    "신규 회원은 USER 권한이고 잠겨 있지 않다" {
        val m = newMember()
        m.role shouldBe Role.USER
        m.isLocked(now) shouldBe false
    }

    "연속 로그인 실패가 한계에 도달하면 잠긴다" {
        val m = newMember()
        repeat(Member.MAX_FAILED_ATTEMPTS) { m.recordLoginFailure(now) }
        m.isLocked(now) shouldBe true
    }

    "잠금 시간이 지나면 다시 풀린다" {
        val m = newMember()
        repeat(Member.MAX_FAILED_ATTEMPTS) { m.recordLoginFailure(now) }
        val afterLock = now.plus(Member.LOCK_DURATION).plusSeconds(1)
        m.isLocked(afterLock) shouldBe false
    }

    "로그인 성공하면 실패 카운트가 초기화된다" {
        val m = newMember()
        m.recordLoginFailure(now)
        m.recordLoginFailure(now)
        m.recordLoginSuccess()
        m.failedAttempts shouldBe 0
    }

    "비밀번호를 바꾸면 잠금이 풀린다" {
        val m = newMember()
        repeat(Member.MAX_FAILED_ATTEMPTS) { m.recordLoginFailure(now) }
        m.changePassword(PasswordHash("newHash"))
        m.isLocked(now.plusSeconds(1)) shouldBe false
        m.passwordHash shouldBe PasswordHash("newHash")
    }

    "닉네임이 너무 짧으면 만들 수 없다" {
        shouldThrow<IllegalArgumentException> { Nickname.of("a") }
    }
})
