package com.hyobin.neomusic.auth.application.port.outbound

import com.hyobin.neomusic.auth.domain.Member
import com.hyobin.neomusic.auth.domain.Nickname

interface LoadMemberPort {
    fun findById(id: Long): Member?
    fun findByNickname(nickname: Nickname): Member?
    fun existsByNickname(nickname: Nickname): Boolean
}
