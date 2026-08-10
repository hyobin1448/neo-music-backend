package com.hyobin.neomusic.auth.adapter.inbound.web

import com.hyobin.neomusic.auth.application.port.inbound.LoginCommand
import com.hyobin.neomusic.auth.application.port.inbound.LoginUseCase
import com.hyobin.neomusic.auth.application.port.inbound.SignupCommand
import com.hyobin.neomusic.auth.application.port.inbound.SignupUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 인증 HTTP 엔드포인트.
 * POST /auth/signup → 회원가입 (201)
 * POST /auth/login  → 로그인, JWT 발급 (200)
 */
@RestController
@RequestMapping("/auth")
class AuthController(
    private val signupUseCase: SignupUseCase,
    private val loginUseCase: LoginUseCase,
) {
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signup(@Valid @RequestBody request: SignupRequest): MemberResponse =
        MemberResponse.from(signupUseCase.signup(SignupCommand(request.nickname, request.password)))

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): LoginResponse {
        val result = loginUseCase.login(LoginCommand(request.nickname, request.password))
        return LoginResponse(
            accessToken = result.accessToken,
            nickname = result.member.nickname.value,
            role = result.member.role.name,
        )
    }
}
