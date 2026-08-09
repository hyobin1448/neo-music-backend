package com.hyobin.neomusic.catalog.adapter.outbound.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * 전역 카탈로그 버전. 테이블에 딱 한 행(id=1)만 존재한다.
 * 곡이 생성/수정/삭제될 때마다 version이 +1 되고, 그 값을 곡의 lastModifiedVersion에 스탬프한다.
 */
@Entity
@Table(name = "catalog_version")
class CatalogVersionJpaEntity(
    @Id
    val id: Int = SINGLETON_ID,

    @Column(nullable = false)
    var version: Long = 0,
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
