package com.hyobin.neomusic.playlist.adapter.outbound.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface PlaylistJpaRepository : JpaRepository<PlaylistJpaEntity, Long> {
    fun findByOwnerIdOrderByIdAsc(ownerId: Long): List<PlaylistJpaEntity>
}
