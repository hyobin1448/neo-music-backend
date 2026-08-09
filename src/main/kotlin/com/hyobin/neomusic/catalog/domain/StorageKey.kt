package com.hyobin.neomusic.catalog.domain

/**
 * 파일 저장 키 값 객체 (예: songs/song_001/ko.m4a).
 *
 * URL이 아니라 "저장소 내 경로"임을 타입으로 명확히 한다.
 * 실제 다운로드 URL은 바깥(어댑터) 계층에서 이 키로 signed URL을 만들어 제공한다.
 * → 저장소를 로컬에서 S3로 바꿔도 도메인은 이 키만 알면 되고 아무것도 바뀌지 않는다.
 */
@JvmInline
value class StorageKey(val value: String) {
    init {
        require(value.isNotBlank()) { "StorageKey는 비어 있을 수 없습니다." }
    }
}
