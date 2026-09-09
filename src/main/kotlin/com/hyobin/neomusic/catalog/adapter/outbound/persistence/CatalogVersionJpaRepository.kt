package com.hyobin.neomusic.catalog.adapter.outbound.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CatalogVersionJpaRepository : JpaRepository<CatalogVersionJpaEntity, Int> {

    /**
     * 전역 버전 행을 잠그고 읽는다(`SELECT ... FOR UPDATE`).
     *
     * 잠금은 **호출한 트랜잭션이 커밋될 때까지** 유지된다. 이 때문에 두 가지가 동시에 해결된다.
     *  - 같은 버전이 두 번 발급되는 것 (읽고-더하고-쓰기 경합)
     *  - 버전 발급 순서와 커밋 순서가 뒤집혀, 뒤늦게 커밋된 낮은 버전을 동기화가 건너뛰는 것
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from CatalogVersionJpaEntity v where v.id = :id")
    fun findAndLock(@Param("id") id: Int): CatalogVersionJpaEntity?
}
