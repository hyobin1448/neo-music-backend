package com.hyobin.neomusic.auth.adapter.outbound.persistence

import com.hyobin.neomusic.auth.domain.Member
import com.hyobin.neomusic.auth.domain.Nickname
import com.hyobin.neomusic.auth.domain.PasswordHash

fun MemberJpaEntity.toDomain(): Member = Member.reconstitute(
    id = id!!,
    nickname = Nickname.of(nickname),
    passwordHash = PasswordHash(passwordHash),
    role = role,
    failedAttempts = failedAttempts,
    lockedUntil = lockedUntil,
)
