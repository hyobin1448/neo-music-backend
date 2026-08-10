package com.hyobin.neomusic.auth.application.port.outbound

import com.hyobin.neomusic.auth.domain.Member

/**
 * 출력 포트: 인증 토큰(JWT) 발급. 실제 구현은 JWT 어댑터가 담당한다.
 */
interface TokenPort {
    /** 회원 정보를 담은 액세스 토큰을 발급한다. */
    fun issue(member: Member): String
}
