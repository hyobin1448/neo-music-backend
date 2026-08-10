package com.hyobin.neomusic.auth.application.port.outbound

import com.hyobin.neomusic.auth.domain.Member

interface SaveMemberPort {
    fun save(member: Member): Member
}
