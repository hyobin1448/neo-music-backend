package com.hyobin.neomusic.auth.application.port.outbound

import com.hyobin.neomusic.auth.domain.PasswordHash

/**
 * 출력 포트: 비밀번호 해싱/검증. 실제 구현은 BCrypt 어댑터가 담당한다.
 * application은 "해싱한다/맞는지 확인한다"만 알고, 알고리즘은 모른다.
 */
interface PasswordEncoderPort {
    fun encode(rawPassword: String): PasswordHash
    fun matches(rawPassword: String, hash: PasswordHash): Boolean
}
