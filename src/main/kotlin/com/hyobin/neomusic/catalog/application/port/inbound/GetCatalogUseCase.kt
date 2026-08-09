package com.hyobin.neomusic.catalog.application.port.inbound

import com.hyobin.neomusic.catalog.application.CatalogSnapshot

/**
 * 입력 포트(유스케이스): 카탈로그 동기화 조회.
 */
interface GetCatalogUseCase {
    /**
     * @param since null이면 첫 동기화(활성 곡 전체), 값이 있으면 그 버전 이후 변경분만.
     */
    fun getCatalog(since: Long?): CatalogSnapshot
}
