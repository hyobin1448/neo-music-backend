package com.hyobin.neomusic.auth.domain

/**
 * 비밀번호 '해시' 값 객체.
 * 원문 비밀번호는 도메인에 절대 들어오지 않는다 — 해시된 값만 보관한다.
 * (실제 해싱/검증은 BCrypt 어댑터가 담당. 도메인은 해시 문자열만 안다)
 */
@JvmInline
value class PasswordHash(val value: String) {
    init {
        require(value.isNotBlank()) { "비밀번호 해시는 비어 있을 수 없습니다." }
    }
}
