package com.hyobin.neomusic.auth.adapter.outbound.security

import com.hyobin.neomusic.auth.domain.Member
import com.hyobin.neomusic.auth.domain.Nickname
import com.hyobin.neomusic.auth.domain.PasswordHash
import com.hyobin.neomusic.auth.domain.Role
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class JwtTokenAdapterTest : StringSpec({

    val secret = "test-secret-that-is-long-enough-for-hmac-32b!!"
    val issuedAt = Instant.parse("2026-01-01T00:00:00Z")
    val adapter = JwtTokenAdapter(secret, expirationSeconds = 3600, clock = Clock.fixed(issuedAt, ZoneOffset.UTC))

    val member = Member.reconstitute(7, Nickname.of("영수"), PasswordHash("h"), Role.ADMIN, 0, null)

    "발급한 토큰을 파싱하면 같은 사용자 정보가 나온다" {
        val token = adapter.issue(member)
        val parsed = adapter.parse(token)!!
        parsed.memberId shouldBe 7L
        parsed.nickname shouldBe "영수"
        parsed.role shouldBe Role.ADMIN
    }

    "위조된 토큰은 파싱되지 않는다" {
        val token = adapter.issue(member)
        adapter.parse(token.dropLast(4) + "XXXX").shouldBeNull()
    }

    "만료된 토큰은 파싱되지 않는다" {
        val token = adapter.issue(member)   // 만료 = issuedAt + 3600초
        // 발급 2시간 뒤 시점의 파서로 검증 → 만료
        val laterAdapter = JwtTokenAdapter(secret, 3600, Clock.fixed(issuedAt.plusSeconds(7200), ZoneOffset.UTC))
        laterAdapter.parse(token).shouldBeNull()
    }
})
