package com.hyobin.neomusic.auth.adapter.inbound.web

import com.hyobin.neomusic.auth.domain.Member
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignupRequest(
    @field:NotBlank
    val nickname: String,
    @field:NotBlank
    @field:Size(min = 4, max = 64, message = "비밀번호는 4자 이상이어야 합니다.")
    val password: String,
)

data class LoginRequest(
    @field:NotBlank
    val nickname: String,
    @field:NotBlank
    val password: String,
)

data class MemberResponse(
    val id: Long?,
    val nickname: String,
    val role: String,
) {
    companion object {
        fun from(member: Member): MemberResponse =
            MemberResponse(id = member.id, nickname = member.nickname.value, role = member.role.name)
    }
}

data class LoginResponse(
    val accessToken: String,
    val nickname: String,
    val role: String,
)
