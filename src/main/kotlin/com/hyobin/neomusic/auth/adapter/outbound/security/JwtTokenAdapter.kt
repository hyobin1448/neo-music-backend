package com.hyobin.neomusic.auth.adapter.outbound.security

import com.hyobin.neomusic.auth.application.AuthenticatedUser
import com.hyobin.neomusic.auth.application.port.outbound.TokenParserPort
import com.hyobin.neomusic.auth.application.port.outbound.TokenPort
import com.hyobin.neomusic.auth.domain.Member
import com.hyobin.neomusic.auth.domain.Role
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Clock
import java.util.Date

/**
 * 토큰 발급/검증의 JWT 구현.
 * 회원 식별자/닉네임/권한을 담아 서명하고, 반대로 검증·파싱한다.
 * 만료 판정은 주입된 Clock을 사용 → 테스트에서 고정 시각으로 검증 가능.
 */
@Component
class JwtTokenAdapter(
    @Value("\${neomusic.jwt.secret}") secret: String,
    @Value("\${neomusic.jwt.expiration-seconds}") private val expirationSeconds: Long,
    private val clock: Clock,
) : TokenPort, TokenParserPort {

    private val key = Keys.hmacShaKeyFor(secret.toByteArray())
    private val jwtClock = io.jsonwebtoken.Clock { Date.from(clock.instant()) }

    override fun issue(member: Member): String {
        val now = clock.instant()
        return Jwts.builder()
            .subject(member.id.toString())
            .claim("nickname", member.nickname.value)
            .claim("role", member.role.name)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(expirationSeconds)))
            .signWith(key)
            .compact()
    }

    override fun parse(token: String): AuthenticatedUser? =
        try {
            val claims = Jwts.parser()
                .clock(jwtClock)          // 만료 판정에 주입된 시계 사용
                .verifyWith(key)          // 서명 검증
                .build()
                .parseSignedClaims(token)
                .payload
            AuthenticatedUser(
                memberId = claims.subject.toLong(),
                nickname = claims.get("nickname", String::class.java),
                role = Role.valueOf(claims.get("role", String::class.java)),
            )
        } catch (e: Exception) {
            null   // 위조·만료·형식오류 → 인증 실패
        }
}
