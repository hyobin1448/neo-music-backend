package com.hyobin.neomusic.storage.application.port.outbound

/**
 * 출력 포트: 파일 다운로드용 '시간 제한 서명 URL'을 만들고 검증한다.
 *
 * 저장 키를 그대로 노출하면 누구나 아무 파일이나 받아갈 수 있으므로,
 * "이 키를, 이 시각까지만" 이라는 서명을 붙여 내려준다.
 * 서명은 서버만 아는 비밀키로 만든 HMAC이라 위조할 수 없다.
 */
interface SignedUrlPort {
    /** 키에 대한 서명 경로를 만든다. 예: /files/songs/s1/ko.m4a?expires=..&sig=.. */
    fun sign(key: String): String

    /** expires(만료 시각) / sig(서명)가 유효한지 검증한다. */
    fun verify(key: String, expires: Long, sig: String): Boolean
}
