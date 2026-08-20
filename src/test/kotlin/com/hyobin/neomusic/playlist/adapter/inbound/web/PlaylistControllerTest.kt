package com.hyobin.neomusic.playlist.adapter.inbound.web

import com.hyobin.neomusic.auth.adapter.inbound.web.JwtAuthenticationFilter
import com.hyobin.neomusic.auth.application.AuthenticatedUser
import com.hyobin.neomusic.auth.domain.Role
import com.hyobin.neomusic.common.web.ArgumentResolverConfig
import com.hyobin.neomusic.common.web.GlobalExceptionHandler
import com.hyobin.neomusic.playlist.application.port.inbound.PlaylistUseCase
import com.hyobin.neomusic.playlist.domain.Playlist
import com.hyobin.neomusic.playlist.domain.PlaylistName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(PlaylistController::class)
@Import(GlobalExceptionHandler::class, ArgumentResolverConfig::class)
class PlaylistControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var playlistUseCase: PlaylistUseCase

    private fun user() = AuthenticatedUser(2, "할머니", Role.USER)

    @Test
    fun `토큰이 없으면 401을 반환한다`() {
        mockMvc.perform(
            post("/playlists").contentType(MediaType.APPLICATION_JSON).content("""{"name":"내 리스트"}"""),
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `생성하면 201과 플레이리스트를 반환한다`() {
        given(playlistUseCase.create(2L, "내 리스트"))
            .willReturn(Playlist.reconstitute(10, 2, PlaylistName.of("내 리스트"), listOf("s1")))

        mockMvc.perform(
            post("/playlists").contentType(MediaType.APPLICATION_JSON).content("""{"name":"내 리스트"}""")
                .requestAttr(JwtAuthenticationFilter.CURRENT_USER_ATTR, user()),
        ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.name").value("내 리스트"))
            .andExpect(jsonPath("$.songIds[0]").value("s1"))
    }

    @Test
    fun `이름이 비면 400을 반환한다`() {
        mockMvc.perform(
            post("/playlists").contentType(MediaType.APPLICATION_JSON).content("""{"name":""}""")
                .requestAttr(JwtAuthenticationFilter.CURRENT_USER_ATTR, user()),
        ).andExpect(status().isBadRequest)
    }
}
