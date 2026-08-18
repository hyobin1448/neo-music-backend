package com.hyobin.neomusic.auth.adapter.inbound.bootstrap

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 최초 관리자 계정 설정. (application.yml 의 neomusic.admin.* 을 바인딩)
 *
 * 운영에서는 환경변수(NEOMUSIC_ADMIN_PASSWORD 등)로 반드시 덮어쓸 것.
 */
@ConfigurationProperties(prefix = "neomusic.admin")
data class AdminProperties(
    /** 관리자 닉네임. */
    val nickname: String = "admin",
    /** 관리자 초기 비밀번호(원문). 서버 기동 시 해싱되어 저장된다. */
    val password: String = "admin1234",
    /** false 로 두면 부트스트랩을 건너뛴다. */
    val enabled: Boolean = true,
)
