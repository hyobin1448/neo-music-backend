package com.hyobin.neomusic.auth.adapter.outbound.security

import com.hyobin.neomusic.auth.application.port.outbound.PasswordEncoderPort
import com.hyobin.neomusic.auth.domain.PasswordHash
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

/**
 * 비밀번호 해싱 포트의 BCrypt 구현.
 * BCrypt는 솔트를 자동 포함하고 느리게 설계돼 무차별 대입에 강하다.
 */
@Component
class BCryptPasswordEncoderAdapter : PasswordEncoderPort {

    private val encoder = BCryptPasswordEncoder()

    override fun encode(rawPassword: String): PasswordHash =
        PasswordHash(encoder.encode(rawPassword))

    override fun matches(rawPassword: String, hash: PasswordHash): Boolean =
        encoder.matches(rawPassword, hash.value)
}
