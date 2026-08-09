package com.hyobin.neomusic.catalog.adapter.inbound.web

import com.hyobin.neomusic.catalog.application.CatalogSnapshot
import com.hyobin.neomusic.catalog.application.port.inbound.GetCatalogUseCase
import com.hyobin.neomusic.catalog.domain.Checksum
import com.hyobin.neomusic.catalog.domain.Lang
import com.hyobin.neomusic.catalog.domain.Lyric
import com.hyobin.neomusic.catalog.domain.LyricType
import com.hyobin.neomusic.catalog.domain.Song
import com.hyobin.neomusic.catalog.domain.SongId
import com.hyobin.neomusic.catalog.domain.StorageKey
import com.hyobin.neomusic.catalog.domain.Track
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * 웹 계층 슬라이스 테스트. 유스케이스는 모킹하고 HTTP↔JSON 변환만 검증한다.
 */
@WebMvcTest(CatalogController::class)
class CatalogControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var getCatalogUseCase: GetCatalogUseCase

    private fun sampleSong(): Song = Song.create(
        id = SongId("song_001"),
        title = "아리랑",
        artist = "전통",
        coverKey = StorageKey("covers/song_001.jpg"),
        displayOrder = 1,
        tracks = listOf(Track(Lang.of("ko"), "한국어", StorageKey("s/ko.m4a"), 1000, 100, Checksum("c"))),
        lyrics = listOf(Lyric(Lang.of("ko"), LyricType.IMAGE, listOf(StorageKey("s/ko.png")))),
    )

    @Test
    fun `GET catalog은 스냅샷을 JSON으로 반환한다`() {
        val snapshot = CatalogSnapshot(
            version = 2,
            changed = listOf(sampleSong()),
            deleted = listOf(SongId("song_009")),
        )
        given(getCatalogUseCase.getCatalog(null)).willReturn(snapshot)

        mockMvc.get("/catalog").andExpect {
            status { isOk() }
            jsonPath("$.version") { value(2) }
            jsonPath("$.changed[0].id") { value("song_001") }
            jsonPath("$.changed[0].tracks[0].lang") { value("ko") }
            jsonPath("$.deleted[0]") { value("song_009") }
        }
    }
}
