package com.hyobin.neomusic.catalog.adapter.outbound.persistence

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * Spring Data JPA 리포지토리. 기본 CRUD(save/findById/findAll 등)를 자동 제공한다.
 */
interface SongJpaRepository : JpaRepository<SongJpaEntity, String> {

    /**
     * 첫 동기화: 삭제되지 않은 곡을 동기화 버전 순으로.
     * (표시 순서 displayOrder 가 아니라 버전 순 — 커서로 이어받아야 하므로)
     */
    @Query(
        """
        select s from SongJpaEntity s
        where s.isDeleted = false and s.lastModifiedVersion > :after
        order by s.lastModifiedVersion asc
        """,
    )
    fun findActiveAfter(@Param("after") after: Long, pageable: Pageable): List<SongJpaEntity>

    /** 델타 동기화: 버전 이후 바뀐 곡(삭제 tombstone 포함)을 버전 순으로. */
    @Query(
        """
        select s from SongJpaEntity s
        where s.lastModifiedVersion > :after
        order by s.lastModifiedVersion asc
        """,
    )
    fun findChangedAfter(@Param("after") after: Long, pageable: Pageable): List<SongJpaEntity>

    /**
     * 삭제 안 된 곡 중 제목/아티스트에 검색어 포함(대소문자 무시).
     * 파생 메서드로는 "isDeleted=false AND (title OR artist)" 그룹핑이 안 돼 JPQL 로 명시.
     *
     * :q 는 어댑터에서 LIKE 메타문자(%, _)를 이스케이프해 넘긴다 → escape '!' 로 리터럴 취급.
     */
    @Query(
        """
        select s from SongJpaEntity s
        where s.isDeleted = false
          and (lower(s.title) like lower(concat('%', :q, '%')) escape '!'
               or lower(s.artist) like lower(concat('%', :q, '%')) escape '!')
        order by s.displayOrder asc
        """,
    )
    fun searchActive(@Param("q") query: String): List<SongJpaEntity>
}
