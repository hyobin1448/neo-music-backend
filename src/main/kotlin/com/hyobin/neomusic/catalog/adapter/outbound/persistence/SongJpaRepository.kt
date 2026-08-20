package com.hyobin.neomusic.catalog.adapter.outbound.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * Spring Data JPA 리포지토리. 기본 CRUD(save/findById/findAll 등)를 자동 제공한다.
 */
interface SongJpaRepository : JpaRepository<SongJpaEntity, String> {

    // 메서드 이름만으로 쿼리가 자동 생성된다 (Spring Data JPA)
    fun findByIsDeletedFalseOrderByDisplayOrderAsc(): List<SongJpaEntity>

    fun findByLastModifiedVersionGreaterThan(version: Long): List<SongJpaEntity>

    /**
     * 삭제 안 된 곡 중 제목/아티스트에 검색어 포함(대소문자 무시).
     * 파생 메서드로는 "isDeleted=false AND (title OR artist)" 그룹핑이 안 돼 JPQL 로 명시.
     */
    @Query(
        """
        select s from SongJpaEntity s
        where s.isDeleted = false
          and (lower(s.title) like lower(concat('%', :q, '%'))
               or lower(s.artist) like lower(concat('%', :q, '%')))
        order by s.displayOrder asc
        """,
    )
    fun searchActive(@Param("q") query: String): List<SongJpaEntity>
}
