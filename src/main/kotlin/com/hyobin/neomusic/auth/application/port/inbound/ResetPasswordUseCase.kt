package com.hyobin.neomusic.auth.application.port.inbound

/**
 * 입력 포트: 관리자가 회원의 비밀번호를 초기화한다.
 * 소셜 로그인 없이 닉네임+비번만 쓰는 구조라, 비번을 잊은 어르신을
 * 관리자가 수동으로 리셋해주는 경로. 리셋 시 로그인 잠금도 함께 풀린다.
 */
interface ResetPasswordUseCase {
    fun resetPassword(command: ResetPasswordCommand)
}

data class ResetPasswordCommand(
    val targetMemberId: Long,
    val newPassword: String,
)
