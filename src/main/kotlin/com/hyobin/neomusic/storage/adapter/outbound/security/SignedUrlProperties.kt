package com.hyobin.neomusic.storage.adapter.outbound.security

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 서명 URL 설정. (application.yml 의 neomusic.storage.url.*)
 * 운영에서는 secret 을 환경변수로 반드시 덮어쓸 것.
 */
@ConfigurationProperties(prefix = "neomusic.storage.url")
data class SignedUrlProperties(
    /** HMAC 서명에 쓰는 비밀키. */
    val secret: String = "dev-only-storage-secret-change-me-0123456789",
    /** 서명 URL 유효 시간(초). 기본 10분. */
    val validSeconds: Long = 600,
)
