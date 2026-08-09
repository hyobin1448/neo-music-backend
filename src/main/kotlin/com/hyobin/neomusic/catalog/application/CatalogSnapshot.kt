package com.hyobin.neomusic.catalog.application

import com.hyobin.neomusic.catalog.domain.Song
import com.hyobin.neomusic.catalog.domain.SongId

/**
 * 카탈로그 동기화 결과.
 * @param version 응답 시점의 전역 버전 (클라가 다음 동기화 때 since로 보냄)
 * @param changed 신규·변경된 곡 (삭제 아님)
 * @param deleted 삭제된 곡 id 목록 (tombstone 전파)
 */
data class CatalogSnapshot(
    val version: Long,
    val changed: List<Song>,
    val deleted: List<SongId>,
)
