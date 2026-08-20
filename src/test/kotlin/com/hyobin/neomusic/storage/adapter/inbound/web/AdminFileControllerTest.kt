package com.hyobin.neomusic.storage.adapter.inbound.web

import com.hyobin.neomusic.auth.adapter.inbound.web.JwtAuthenticationFilter
import com.hyobin.neomusic.auth.application.AuthenticatedUser
import com.hyobin.neomusic.auth.domain.Role
import com.hyobin.neomusic.common.web.ArgumentResolverConfig
import com.hyobin.neomusic.common.web.GlobalExceptionHandler
import com.hyobin.neomusic.storage.application.port.inbound.StoreFileUseCase
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * 파일 업로드 API의 인증/인가를 검증한다. (정상 업로드는 라이브 E2E 로 확인)
 */
@WebMvcTest(AdminFileController::class)
@Import(GlobalExceptionHandler::class, ArgumentResolverConfig::class)
class AdminFileControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var storeFileUseCase: StoreFileUseCase

    private fun sampleFile() =
        MockMultipartFile("file", "ko.m4a", "audio/mp4", "audio-bytes".toByteArray())

    private fun normalUser() = AuthenticatedUser(2, "할머니", Role.USER)

    @Test
    fun `토큰이 없으면 401을 반환한다`() {
        mockMvc.perform(
            multipart("/admin/files").file(sampleFile()),
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `관리자가 아니면 403을 반환한다`() {
        mockMvc.perform(
            multipart("/admin/files").file(sampleFile())
                .requestAttr(JwtAuthenticationFilter.CURRENT_USER_ATTR, normalUser()),
        ).andExpect(status().isForbidden)
    }
}
