package com.hyobin.neomusic.catalog.application.port.outbound

import com.hyobin.neomusic.catalog.domain.Song
import com.hyobin.neomusic.catalog.domain.SongId

/**
 * 출력 포트: "곡을 불러온다"는 능력.
 */
interface LoadSongPort {
    fun findById(id: SongId): Song?
    fun findAll(): List<Song>

    /** 삭제되지 않은 곡 전체(큐레이션 순서). 첫 동기화(since 없음)에 사용. */
    fun findAllActive(): List<Song>

    /** lastModifiedVersion > version 인 곡(삭제된 곡 포함). 델타 동기화에 사용. */
    fun findChangedSince(version: Long): List<Song>

    /** 삭제되지 않은 곡 중 제목/아티스트에 검색어가 포함된 곡(대소문자 무시). */
    fun searchActive(query: String): List<Song>
}
