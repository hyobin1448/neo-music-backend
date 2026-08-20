package com.hyobin.neomusic.catalog.adapter.inbound.web

import com.hyobin.neomusic.catalog.application.CatalogSnapshot
import com.hyobin.neomusic.catalog.application.port.inbound.GetCatalogUseCase
import com.hyobin.neomusic.catalog.application.port.inbound.SearchSongsUseCase
import com.hyobin.neomusic.catalog.domain.Checksum
import com.hyobin.neomusic.catalog.domain.Lang
import com.hyobin.neomusic.catalog.domain.Lyric
import com.hyobin.neomusic.catalog.domain.LyricType
import com.hyobin.neomusic.catalog.domain.Song
import com.hyobin.neomusic.catalog.domain.SongId
import com.hyobin.neomusic.catalog.domain.StorageKey
import com.hyobin.neomusic.catalog.domain.Track
import com.hyobin.neomusic.storage.application.port.outbound.SignedUrlPort
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * 웹 계층 슬라이스 테스트. 유스케이스는 모킹하고 HTTP↔JSON 변환만 검증한다.
 * 조립기(CatalogResponseAssembler)는 실제로 쓰되, 서명은 SignedUrlPort 목으로 대체한다.
 */
@WebMvcTest(CatalogController::class)
@Import(CatalogResponseAssembler::class)
class CatalogControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var getCatalogUseCase: GetCatalogUseCase

    @MockBean
    lateinit var searchSongsUseCase: SearchSongsUseCase

    @MockBean
    lateinit var signedUrl: SignedUrlPort

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
        // 저장 키 → (가짜) 서명 URL 로 변환된다
        given(signedUrl.sign("covers/song_001.jpg")).willReturn("/files/covers/song_001.jpg?sig=COVER")
        given(signedUrl.sign("s/ko.m4a")).willReturn("/files/s/ko.m4a?sig=AUDIO")
        given(signedUrl.sign("s/ko.png")).willReturn("/files/s/ko.png?sig=IMG")

        mockMvc.get("/catalog").andExpect {
            status { isOk() }
            jsonPath("$.version") { value(2) }
            jsonPath("$.changed[0].id") { value("song_001") }
            jsonPath("$.changed[0].coverUrl") { value("/files/covers/song_001.jpg?sig=COVER") }
            jsonPath("$.changed[0].tracks[0].lang") { value("ko") }
            jsonPath("$.changed[0].tracks[0].audioUrl") { value("/files/s/ko.m4a?sig=AUDIO") }
            jsonPath("$.changed[0].lyrics[0].imageUrls[0]") { value("/files/s/ko.png?sig=IMG") }
            jsonPath("$.deleted[0]") { value("song_009") }
        }
    }

    @Test
    fun `GET catalog search는 매칭된 곡 목록을 반환한다`() {
        given(searchSongsUseCase.search("아리")).willReturn(listOf(sampleSong()))
        given(signedUrl.sign("covers/song_001.jpg")).willReturn("/files/cover?sig=C")
        given(signedUrl.sign("s/ko.m4a")).willReturn("/files/audio?sig=A")
        given(signedUrl.sign("s/ko.png")).willReturn("/files/img?sig=I")

        mockMvc.get("/catalog/search") {
            param("q", "아리")
        }.andExpect {
            status { isOk() }
            jsonPath("$.query") { value("아리") }
            jsonPath("$.results[0].id") { value("song_001") }
            jsonPath("$.results[0].tracks[0].audioUrl") { value("/files/audio?sig=A") }
        }
    }
}
