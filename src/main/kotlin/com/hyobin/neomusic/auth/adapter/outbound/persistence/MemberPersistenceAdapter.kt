package com.hyobin.neomusic.auth.adapter.outbound.persistence

import com.hyobin.neomusic.auth.application.port.outbound.LoadMemberPort
import com.hyobin.neomusic.auth.application.port.outbound.SaveMemberPort
import com.hyobin.neomusic.auth.domain.Member
import com.hyobin.neomusic.auth.domain.Nickname
import org.springframework.stereotype.Repository

@Repository
class MemberPersistenceAdapter(
    private val repository: MemberJpaRepository,
) : SaveMemberPort, LoadMemberPort {

    override fun save(member: Member): Member {
        // id가 있으면 기존 회원을 불러와 갱신, 없으면 새로 삽입
        val entity = if (member.id != null) {
            repository.findById(member.id).orElseGet { newEntity(member) }
        } else {
            newEntity(member)
        }
        entity.nickname = member.nickname.value
        entity.passwordHash = member.passwordHash.value
        entity.role = member.role
        entity.failedAttempts = member.failedAttempts
        entity.lockedUntil = member.lockedUntil
        return repository.save(entity).toDomain()
    }

    override fun findById(id: Long): Member? =
        repository.findById(id).map { it.toDomain() }.orElse(null)

    override fun findByNickname(nickname: Nickname): Member? =
        repository.findByNickname(nickname.value)?.toDomain()

    override fun existsByNickname(nickname: Nickname): Boolean =
        repository.existsByNickname(nickname.value)

    private fun newEntity(member: Member) = MemberJpaEntity(
        nickname = member.nickname.value,
        passwordHash = member.passwordHash.value,
        role = member.role,
        failedAttempts = member.failedAttempts,
        lockedUntil = member.lockedUntil,
    )
}
