package com.hyobin.neomusic.auth.application.port.outbound

import com.hyobin.neomusic.auth.application.AuthenticatedUser

/**
 * 출력 포트: 토큰 검증/파싱. 유효하면 사용자 정보를, 무효(위조·만료)면 null을 돌려준다.
 */
interface TokenParserPort {
    fun parse(token: String): AuthenticatedUser?
}
