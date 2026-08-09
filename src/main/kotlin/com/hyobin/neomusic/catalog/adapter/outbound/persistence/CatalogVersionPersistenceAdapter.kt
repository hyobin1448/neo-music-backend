package com.hyobin.neomusic.catalog.adapter.outbound.persistence

import com.hyobin.neomusic.catalog.application.port.outbound.CatalogVersionPort
import org.springframework.stereotype.Repository

/**
 * 전역 버전 포트의 JPA 구현.
 *
 * 참고(동시성): 여기선 읽고-더하고-쓰기 방식이다. 곡 변경은 '관리자'만 하고 빈도가 낮아 충돌이 사실상 없다.
 * 고동시성이 필요하면 비관적 락(SELECT ... FOR UPDATE)이나 원자적 UPDATE로 강화할 수 있다.
 */
@Repository
class CatalogVersionPersistenceAdapter(
    private val repository: CatalogVersionJpaRepository,
) : CatalogVersionPort {

    override fun current(): Long =
        repository.findById(CatalogVersionJpaEntity.SINGLETON_ID)
            .map { it.version }
            .orElse(0)

    override fun next(): Long {
        val entity = repository.findById(CatalogVersionJpaEntity.SINGLETON_ID)
            .orElseGet { CatalogVersionJpaEntity() }
        entity.version += 1
        return repository.save(entity).version
    }
}
