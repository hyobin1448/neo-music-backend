package com.hyobin.neomusic.catalog.application.port.inbound

import com.hyobin.neomusic.catalog.domain.Song

/**
 * 입력 포트: 곡 검색.
 * 제목/아티스트에 검색어가 포함된 활성 곡을 반환한다.
 * (가사 검색은 가사가 텍스트로 구조화된 이후 2차로 확장)
 */
interface SearchSongsUseCase {
    fun search(query: String): List<Song>
}
