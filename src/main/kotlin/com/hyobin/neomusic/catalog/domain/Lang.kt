package com.hyobin.neomusic.catalog.domain

/**
 * 언어 코드 값 객체 (예: ko, en).
 *
 * 소문자로 정규화해서 "KO"와 "ko"가 서로 다른 값으로 취급되는 버그를 막는다.
 * 정규화가 필요하므로 생성자를 private으로 막고 [of] 팩토리로만 만들게 한다.
 */
@JvmInline
value class Lang private constructor(val code: String) {
    companion object {
        fun of(raw: String): Lang {
            val normalized = raw.trim().lowercase()
            require(normalized.isNotBlank()) { "언어 코드는 비어 있을 수 없습니다." }
            return Lang(normalized)
        }
    }
}
