package com.hyobin.neomusic.catalog.adapter.inbound.web

import com.hyobin.neomusic.catalog.application.port.inbound.GetCatalogUseCase
import com.hyobin.neomusic.catalog.application.port.inbound.SearchSongsUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 입력 어댑터: 카탈로그 동기화 HTTP 엔드포인트.
 * HTTP 요청을 받아 유스케이스를 호출하고, 결과를 JSON DTO로 변환해 응답한다.
 *
 * GET /catalog          → 첫 동기화(활성 곡 전체)
 * GET /catalog?since=42 → 42 이후 변경분(변경 + 삭제)만
 */
@RestController
@RequestMapping("/catalog")
class CatalogController(
    private val getCatalogUseCase: GetCatalogUseCase,
    private val searchSongsUseCase: SearchSongsUseCase,
    private val assembler: CatalogResponseAssembler,
) {
    @GetMapping
    fun getCatalog(
        @RequestParam(required = false) since: Long?,
    ): CatalogResponse = assembler.toResponse(getCatalogUseCase.getCatalog(since))

    /** GET /catalog/search?q=아리랑 → 제목/아티스트 매칭 곡 목록 */
    @GetMapping("/search")
    fun search(
        @RequestParam q: String,
    ): SearchResponse {
        val songs = searchSongsUseCase.search(q)
        return SearchResponse(
            query = q,
            results = songs.map { assembler.toSongResponse(it) },
        )
    }
}

data class SearchResponse(
    val query: String,
    val results: List<SongResponse>,
)
