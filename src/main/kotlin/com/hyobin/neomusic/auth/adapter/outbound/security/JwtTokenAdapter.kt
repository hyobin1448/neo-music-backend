package com.hyobin.neomusic.auth.adapter.outbound.security

import com.hyobin.neomusic.auth.application.port.outbound.TokenPort
import com.hyobin.neomusic.auth.domain.Member
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Clock
import java.util.Date

/**
 * 토큰 포트의 JWT 구현. 회원 식별자/닉네임/권한을 담아 서명한 액세스 토큰을 만든다.
 * 비밀키는 설정(neomusic.jwt.secret)에서 주입 — 운영에선 환경변수로 덮어쓴다.
 */
@Component
class JwtTokenAdapter(
    @Value("\${neomusic.jwt.secret}") secret: String,
    @Value("\${neomusic.jwt.expiration-seconds}") private val expirationSeconds: Long,
    private val clock: Clock,
) : TokenPort {

    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

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
}
