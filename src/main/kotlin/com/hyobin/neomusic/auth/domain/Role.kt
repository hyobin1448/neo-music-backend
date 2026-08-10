package com.hyobin.neomusic.auth.domain

/** 사용자 권한. ADMIN은 곡 등록/삭제·비번 초기화 등 관리자 기능을 쓸 수 있다. */
enum class Role {
    USER,
    ADMIN,
}
