package com.hyobin.neomusic.auth.adapter.inbound.web

import com.hyobin.neomusic.auth.application.AuthenticatedUser
import com.hyobin.neomusic.auth.application.port.inbound.ResetPasswordCommand
import com.hyobin.neomusic.auth.application.port.inbound.ResetPasswordUseCase
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 관리자용 회원 관리 엔드포인트.
 *
 * PUT /admin/members/{memberId}/password → 비밀번호 초기화(204). 회원 없으면 404.
 * 관리자 전용(USER면 403), 토큰 없으면 401.
 */
@RestController
@RequestMapping("/admin/members")
class AdminMemberController(
    private val resetPasswordUseCase: ResetPasswordUseCase,
) {
    @PutMapping("/{memberId}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun resetPassword(
        user: AuthenticatedUser,
        @PathVariable memberId: Long,
        @Valid @RequestBody request: ResetPasswordRequest,
    ) {
        user.requireAdmin()
        resetPasswordUseCase.resetPassword(ResetPasswordCommand(memberId, request.newPassword))
    }
}

data class ResetPasswordRequest(
    @field:Size(min = 4, max = 100, message = "비밀번호는 4자 이상이어야 합니다")
    val newPassword: String,
)
