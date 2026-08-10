package com.hyobin.neomusic.auth.application.port.inbound

import com.hyobin.neomusic.auth.domain.Member

interface LoginUseCase {
    fun login(command: LoginCommand): LoginResult
}

data class LoginCommand(
    val nickname: String,
    val password: String,
)

data class LoginResult(
    val accessToken: String,
    val member: Member,
)
