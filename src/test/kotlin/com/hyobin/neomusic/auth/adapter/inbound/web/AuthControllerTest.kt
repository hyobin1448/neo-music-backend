package com.hyobin.neomusic.auth.adapter.inbound.web

import com.hyobin.neomusic.auth.application.port.inbound.LoginCommand
import com.hyobin.neomusic.auth.application.port.inbound.LoginResult
import com.hyobin.neomusic.auth.application.port.inbound.LoginUseCase
import com.hyobin.neomusic.auth.application.port.inbound.SignupCommand
import com.hyobin.neomusic.auth.application.port.inbound.SignupUseCase
import com.hyobin.neomusic.auth.domain.InvalidCredentialsException
import com.hyobin.neomusic.auth.domain.Member
import com.hyobin.neomusic.auth.domain.Nickname
import com.hyobin.neomusic.auth.domain.NicknameAlreadyExistsException
import com.hyobin.neomusic.auth.domain.PasswordHash
import com.hyobin.neomusic.auth.domain.Role
import com.hyobin.neomusic.common.web.GlobalExceptionHandler
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(AuthController::class)
@Import(GlobalExceptionHandler::class)
class AuthControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var signupUseCase: SignupUseCase

    @MockBean
    lateinit var loginUseCase: LoginUseCase

    private fun member() =
        Member.reconstitute(1, Nickname.of("할머니"), PasswordHash("h"), Role.USER, 0, null)

    @Test
    fun `회원가입 성공 시 201과 회원 정보를 반환한다`() {
        given(signupUseCase.signup(SignupCommand("할머니", "pw1234"))).willReturn(member())

        mockMvc.post("/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"nickname":"할머니","password":"pw1234"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.nickname") { value("할머니") }
            jsonPath("$.role") { value("USER") }
        }
    }

    @Test
    fun `중복 닉네임이면 409를 반환한다`() {
        given(signupUseCase.signup(SignupCommand("할머니", "pw1234")))
            .willThrow(NicknameAlreadyExistsException(Nickname.of("할머니")))

        mockMvc.post("/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"nickname":"할머니","password":"pw1234"}"""
        }.andExpect {
            status { isConflict() }
        }
    }

    @Test
    fun `비밀번호가 너무 짧으면 400을 반환한다`() {
        mockMvc.post("/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"nickname":"할머니","password":"1"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `로그인 성공 시 토큰을 반환한다`() {
        given(loginUseCase.login(LoginCommand("할머니", "pw1234")))
            .willReturn(LoginResult("jwt-token", member()))

        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"nickname":"할머니","password":"pw1234"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.accessToken") { value("jwt-token") }
        }
    }

    @Test
    fun `잘못된 로그인 정보면 401을 반환한다`() {
        given(loginUseCase.login(LoginCommand("할머니", "wrong")))
            .willThrow(InvalidCredentialsException())

        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"nickname":"할머니","password":"wrong"}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }
}
