package com.hyobin.neomusic.catalog.adapter.inbound.web

import com.hyobin.neomusic.auth.adapter.inbound.web.JwtAuthenticationFilter
import com.hyobin.neomusic.auth.application.AuthenticatedUser
import com.hyobin.neomusic.auth.domain.Role
import com.hyobin.neomusic.catalog.application.port.inbound.RegisterSongUseCase
import com.hyobin.neomusic.common.web.ArgumentResolverConfig
import com.hyobin.neomusic.common.web.GlobalExceptionHandler
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * 관리자 곡 등록 API의 '인증/인가'를 검증한다.
 * (필터는 슬라이스에 없으므로, 필터가 심는 사용자 속성을 requestAttr 로 직접 흉내 낸다)
 */
@WebMvcTest(AdminSongController::class)
@Import(GlobalExceptionHandler::class, ArgumentResolverConfig::class)
class AdminSongControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var registerSongUseCase: RegisterSongUseCase

    private val validBody = """
        {
          "id": "song_001",
          "title": "고향의 봄",
          "artist": "이원수",
          "tracks": [
            {"lang":"ko","label":"한국어","audioKey":"songs/song_001/ko.m4a","durationMs":180000,"bytes":1000,"checksum":"abc"}
          ],
          "lyrics": [
            {"lang":"ko","type":"IMAGE","imageKeys":["songs/song_001/ko_1.png"]}
          ]
        }
    """.trimIndent()

    private fun admin() = AuthenticatedUser(1, "admin", Role.ADMIN)
    private fun normalUser() = AuthenticatedUser(2, "할머니", Role.USER)

    @Test
    fun `토큰이 없으면 401을 반환한다`() {
        mockMvc.perform(
            post("/admin/songs").contentType(MediaType.APPLICATION_JSON).content(validBody),
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `관리자가 아니면 403을 반환한다`() {
        mockMvc.perform(
            post("/admin/songs").contentType(MediaType.APPLICATION_JSON).content(validBody)
                .requestAttr(JwtAuthenticationFilter.CURRENT_USER_ATTR, normalUser()),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `관리자여도 제목이 비면 400을 반환한다`() {
        val blankTitle = validBody.replace("\"고향의 봄\"", "\"\"")
        mockMvc.perform(
            post("/admin/songs").contentType(MediaType.APPLICATION_JSON).content(blankTitle)
                .requestAttr(JwtAuthenticationFilter.CURRENT_USER_ATTR, admin()),
        ).andExpect(status().isBadRequest)
    }
}
