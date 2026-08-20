package com.hyobin.neomusic.storage.application.port.outbound

/**
 * 출력 포트: 파일 저장소 추상화.
 *
 * 키(예: "songs/song_001/ko.m4a")로 파일을 저장/조회/삭제한다.
 * 카탈로그의 StorageKey 에 의존하지 않고 순수 String 키만 다뤄
 * 특정 기능에 묶이지 않는 범용 저장소로 둔다.
 * 로컬 파일시스템 → S3 등으로 교체해도 이 포트만 다시 구현하면 된다.
 */
interface FileStoragePort {
    /** 파일을 저장한다. 같은 키면 덮어쓴다. */
    fun store(key: String, content: ByteArray)

    /** 파일 내용을 읽는다. 없으면 null. */
    fun load(key: String): ByteArray?

    /** 파일 존재 여부. */
    fun exists(key: String): Boolean

    /** 파일을 삭제한다. 없으면 조용히 무시. */
    fun delete(key: String)
}
