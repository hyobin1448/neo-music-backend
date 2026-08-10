package com.hyobin.neomusic.auth.adapter.outbound.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface MemberJpaRepository : JpaRepository<MemberJpaEntity, Long> {
    fun findByNickname(nickname: String): MemberJpaEntity?
    fun existsByNickname(nickname: String): Boolean
}
