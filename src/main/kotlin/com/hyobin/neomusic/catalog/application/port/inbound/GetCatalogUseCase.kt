package com.hyobin.neomusic.catalog.application.port.inbound

import com.hyobin.neomusic.catalog.application.CatalogSnapshot

/**
 * 입력 포트(유스케이스): 카탈로그 동기화 조회.
 */
interface GetCatalogUseCase {
    /**
     * @param since null이면 첫 동기화(활성 곡), 값이 있으면 그 버전 이후 변경분만.
     * @param limit 한 번에 내려줄 최대 곡 수. 곡이 늘어도 응답이 무한정 커지지 않도록 상한을 둔다.
     */
    fun getCatalog(since: Long? = null, limit: Int = DEFAULT_LIMIT): CatalogSnapshot

    companion object {
        /** 클라가 limit 을 안 보냈을 때. */
        const val DEFAULT_LIMIT = 100

        /** 클라가 큰 값을 보내도 여기서 잘린다. */
        const val MAX_LIMIT = 500
    }
}
