package com.hyobin.neomusic.catalog.adapter.outbound.persistence

import org.springframework.data.jpa.repository.JpaRepository

/**
 * Spring Data JPA 리포지토리. 기본 CRUD(save/findById/findAll 등)를 자동 제공한다.
 * (델타 동기화용 커스텀 쿼리는 다음 단계에서 추가)
 */
interface SongJpaRepository : JpaRepository<SongJpaEntity, String> {

    // 메서드 이름만으로 쿼리가 자동 생성된다 (Spring Data JPA)
    fun findByIsDeletedFalseOrderByDisplayOrderAsc(): List<SongJpaEntity>

    fun findByLastModifiedVersionGreaterThan(version: Long): List<SongJpaEntity>
}
