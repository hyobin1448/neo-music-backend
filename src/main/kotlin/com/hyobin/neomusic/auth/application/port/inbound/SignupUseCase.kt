package com.hyobin.neomusic.auth.application.port.inbound

import com.hyobin.neomusic.auth.domain.Member

interface SignupUseCase {
    fun signup(command: SignupCommand): Member
}

data class SignupCommand(
    val nickname: String,
    val password: String,
)
