package com.hyobin.neomusic.auth.application

import com.hyobin.neomusic.auth.domain.ForbiddenException
import com.hyobin.neomusic.auth.domain.Role

/**
 * 요청을 보낸 '인증된 사용자'. JWT에서 추출한 최소 정보만 담는다.
 * 컨트롤러는 이 타입을 파라미터로 받아 현재 사용자를 얻는다.
 */
data class AuthenticatedUser(
    val memberId: Long,
    val nickname: String,
    val role: Role,
) {
    val isAdmin: Boolean get() = role == Role.ADMIN

    /** 관리자 전용 작업을 보호한다. 관리자가 아니면 403. */
    fun requireAdmin() {
        if (!isAdmin) throw ForbiddenException()
    }
}
