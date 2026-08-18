package com.hyobin.neomusic.auth.adapter.inbound.web

import com.hyobin.neomusic.auth.application.AuthenticatedUser
import com.hyobin.neomusic.auth.application.port.inbound.ResetPasswordUseCase
import com.hyobin.neomusic.auth.domain.Role
import com.hyobin.neomusic.common.web.ArgumentResolverConfig
import com.hyobin.neomusic.common.web.GlobalExceptionHandler
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * 관리자 비밀번호 초기화 API의 인증/인가를 검증한다.
 */
@WebMvcTest(AdminMemberController::class)
@Import(GlobalExceptionHandler::class, ArgumentResolverConfig::class)
class AdminMemberControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var resetPasswordUseCase: ResetPasswordUseCase

    private val body = """{"newPassword":"new1234"}"""

    private fun admin() = AuthenticatedUser(1, "admin", Role.ADMIN)
    private fun normalUser() = AuthenticatedUser(2, "할머니", Role.USER)

    @Test
    fun `토큰이 없으면 401을 반환한다`() {
        mockMvc.perform(
            put("/admin/members/5/password").contentType(MediaType.APPLICATION_JSON).content(body),
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `관리자가 아니면 403을 반환한다`() {
        mockMvc.perform(
            put("/admin/members/5/password").contentType(MediaType.APPLICATION_JSON).content(body)
                .requestAttr(JwtAuthenticationFilter.CURRENT_USER_ATTR, normalUser()),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `관리자가 초기화하면 204를 반환한다`() {
        mockMvc.perform(
            put("/admin/members/5/password").contentType(MediaType.APPLICATION_JSON).content(body)
                .requestAttr(JwtAuthenticationFilter.CURRENT_USER_ATTR, admin()),
        ).andExpect(status().isNoContent)
    }

    @Test
    fun `비밀번호가 너무 짧으면 400을 반환한다`() {
        mockMvc.perform(
            put("/admin/members/5/password").contentType(MediaType.APPLICATION_JSON).content("""{"newPassword":"1"}""")
                .requestAttr(JwtAuthenticationFilter.CURRENT_USER_ATTR, admin()),
        ).andExpect(status().isBadRequest)
    }
}
