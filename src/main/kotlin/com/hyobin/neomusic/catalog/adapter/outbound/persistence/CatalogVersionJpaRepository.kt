package com.hyobin.neomusic.catalog.adapter.outbound.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface CatalogVersionJpaRepository : JpaRepository<CatalogVersionJpaEntity, Int>
