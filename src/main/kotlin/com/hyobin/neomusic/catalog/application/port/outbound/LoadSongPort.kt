package com.hyobin.neomusic.catalog.application.port.outbound

import com.hyobin.neomusic.catalog.application.SongPage
import com.hyobin.neomusic.catalog.domain.Song
import com.hyobin.neomusic.catalog.domain.SongId

/**
 * 출력 포트: "곡을 불러온다"는 능력.
 *
 * 동기화 조회는 전부 커서(동기화 버전) 기반이다.
 * 전역 버전은 변경 1건마다 하나씩 발급되어 곡마다 값이 겹치지 않으므로,
 * `lastModifiedVersion` 하나로 전체 순서가 결정된다 → 커서로 쓰기 적합하다.
 * (offset 페이징과 달리 조회 중 데이터가 바뀌어도 건너뛰거나 중복되지 않는다)
 */
interface LoadSongPort {
    fun findById(id: SongId): Song?

    /** 삭제되지 않은 곡을 동기화 버전 순으로. 첫 동기화(since 없음)에 사용. */
    fun findActivePage(afterVersion: Long, limit: Int): SongPage

    /** afterVersion 이후 바뀐 곡(삭제된 곡 포함)을 동기화 버전 순으로. 델타 동기화에 사용. */
    fun findChangedPage(afterVersion: Long, limit: Int): SongPage

    /** 삭제되지 않은 곡 중 제목/아티스트에 검색어가 포함된 곡(대소문자 무시). */
    fun searchActive(query: String): List<Song>
}
