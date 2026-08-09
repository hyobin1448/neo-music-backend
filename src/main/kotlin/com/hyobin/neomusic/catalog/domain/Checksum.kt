package com.hyobin.neomusic.catalog.domain

/**
 * 파일 무결성 체크섬 값 객체 (sha256).
 *
 * 델타 동기화에서 "이 파일이 바뀌었는지"를 판단해 재다운로드를 스킵하는 데 쓰인다.
 * (곡 제목만 바뀌고 오디오는 그대로면, 체크섬이 같으니 오디오는 다시 안 받는다)
 */
@JvmInline
value class Checksum(val value: String) {
    init {
        require(value.isNotBlank()) { "Checksum은 비어 있을 수 없습니다." }
    }
}
