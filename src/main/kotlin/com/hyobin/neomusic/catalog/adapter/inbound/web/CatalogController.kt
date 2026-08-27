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
 * GET /catalog                   → 첫 동기화(활성 곡)
 * GET /catalog?since=42           → 42 이후 변경분(변경 + 삭제)만
 * GET /catalog?since=42&limit=50  → 한 번에 최대 50곡
 *
 * 응답이 hasMore=true 면 아직 남은 변경분이 있다는 뜻이다.
 * 클라는 응답의 nextSince 를 다음 요청의 since 로 넣어 이어받는다.
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
        // 애노테이션 defaultValue 에는 상수를 끼워 넣을 수 없어(문자열 리터럴만 가능),
        // 기본값은 유스케이스의 DEFAULT_LIMIT 한 곳에서만 관리한다.
        @RequestParam(required = false) limit: Int?,
    ): CatalogResponse = assembler.toResponse(
        getCatalogUseCase.getCatalog(since, limit ?: GetCatalogUseCase.DEFAULT_LIMIT),
    )

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
