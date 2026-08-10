package com.hyobin.neomusic.auth.domain

/**
 * 닉네임 값 객체. 로그인 ID 겸 표시 이름으로 쓰인다.
 * 어르신 사용자를 고려해 규칙은 단순하게(2~20자) 둔다.
 */
@JvmInline
value class Nickname private constructor(val value: String) {
    companion object {
        const val MIN = 2
        const val MAX = 20

        fun of(raw: String): Nickname {
            val v = raw.trim()
            require(v.length in MIN..MAX) { "닉네임은 $MIN~${MAX}자여야 합니다. 입력값 길이: ${v.length}" }
            return Nickname(v)
        }
    }
}
