package com.hyobin.neomusic.catalog.adapter.inbound.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.hyobin.neomusic.catalog.application.port.inbound.GetCatalogUseCase
import com.hyobin.neomusic.catalog.application.port.inbound.GetSongUseCase
import com.hyobin.neomusic.catalog.application.port.inbound.SearchSongsUseCase
import com.hyobin.neomusic.catalog.domain.Checksum
import com.hyobin.neomusic.catalog.domain.Lang
import com.hyobin.neomusic.catalog.domain.Lyric
import com.hyobin.neomusic.catalog.domain.LyricType
import com.hyobin.neomusic.catalog.domain.Song
import com.hyobin.neomusic.catalog.domain.SongId
import com.hyobin.neomusic.catalog.domain.SongNotFoundException
import com.hyobin.neomusic.catalog.domain.StorageKey
import com.hyobin.neomusic.catalog.domain.Track
import com.hyobin.neomusic.storage.adapter.outbound.security.HmacSignedUrlAdapter
import com.hyobin.neomusic.storage.adapter.outbound.security.SignedUrlProperties
import com.hyobin.neomusic.storage.application.port.outbound.SignedUrlPort
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * GET /catalog/songs/{id} — 곡 단위 서명 URL 재발급 엔드포인트.
 *
 * 다른 웹 슬라이스 테스트와 달리 SignedUrlPort 를 모킹하지 않고 **진짜 HMAC 어댑터**를 쓴다.
 * 이 API 의 값어치가 "응답을 조립할 때 서명이 새로 찍힌다"는 것 하나이므로,
 * 가짜 서명으로 바꿔치기하면 검증할 게 남지 않기 때문이다.
 * 대신 Clock 을 조작 가능한 것으로 넣어 '시간이 흐른 뒤 다시 호출' 상황을 만든다.
 */
@WebMvcTest(CatalogController::class)
@Import(CatalogResponseAssembler::class, CatalogSongControllerTest.SigningConfig::class)
class CatalogSongControllerTest {

    /** 테스트에서 앞으로 감을 수 있는 시계. (instant() 와 이름이 겹치지 않게 프로퍼티는 now) */
    class MutableClock(var now: Instant) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId): Clock = this
        override fun instant(): Instant = now
    }

    @TestConfiguration
    class SigningConfig {
        @Bean
        fun clock() = MutableClock(Instant.parse("2026-01-01T00:00:00Z"))

        /**
         * 진짜 서명기. 설정은 빈으로 올리지 않고 직접 넘긴다
         * (@ConfigurationProperties 데이터 클래스를 빈으로 두면 슬라이스에서 재바인딩을 시도해 깨진다).
         */
        @Bean
        fun signedUrl(clock: MutableClock): SignedUrlPort =
            HmacSignedUrlAdapter(SignedUrlProperties(secret = "test-secret-123", validSeconds = 600), clock)
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var clock: MutableClock

    @MockBean
    lateinit var getSongUseCase: GetSongUseCase

    // 컨트롤러가 함께 주입받는 유스케이스들 — 이 테스트에선 쓰지 않는다.
    @MockBean
    lateinit var getCatalogUseCase: GetCatalogUseCase

    @MockBean
    lateinit var searchSongsUseCase: SearchSongsUseCase

    private fun sampleSong(): Song = Song.create(
        id = SongId("song_001"),
        title = "아리랑",
        artist = "전통",
        coverKey = StorageKey("covers/song_001.jpg"),
        displayOrder = 1,
        tracks = listOf(Track(Lang.of("ko"), "한국어", StorageKey("songs/song_001/ko.m4a"), 1000, 100, Checksum("c"))),
        lyrics = listOf(Lyric(Lang.of("ko"), LyricType.IMAGE, listOf(StorageKey("songs/song_001/ko.png")))),
    )

    /** "/files/{key}?expires=E&sig=S" 형식인가. */
    private val signedUrlPattern = Regex("""^/files/\S+\?expires=\d+&sig=[\w-]+$""")

    private fun expiresOf(url: String): Long =
        url.substringAfter("expires=").substringBefore("&").toLong()

    @Test
    fun `존재하는 곡은 200과 함께 서명된 URL들을 내려준다`() {
        given(getSongUseCase.getSong(SongId("song_001"))).willReturn(sampleSong())

        val body = mockMvc.get("/catalog/songs/song_001").andExpect {
            status { isOk() }
            jsonPath("$.id") { value("song_001") }
            jsonPath("$.title") { value("아리랑") }
        }.andReturn().response.contentAsString

        // 트랙 · 가사 · 커버 URL 이 전부 서명 URL 형식이어야 한다
        listOf(
            jsonString(body, "tracks", "audioUrl"),
            jsonString(body, "lyrics", "imageUrls"),
            jsonString(body, "coverUrl"),
        ).forEach { it shouldMatch signedUrlPattern }
    }

    @Test
    fun `같은 곡을 시간이 흐른 뒤 다시 호출하면 서명이 갱신된다`() {
        given(getSongUseCase.getSong(SongId("song_001"))).willReturn(sampleSong())

        val first = mockMvc.get("/catalog/songs/song_001").andReturn().response.contentAsString
        val firstAudio = jsonString(first, "tracks", "audioUrl")

        // 300초 경과 — 아직 첫 URL 이 살아 있는 시점이지만, 재발급은 만료 창을 새로 연다
        clock.now = clock.now.plusSeconds(300)

        val second = mockMvc.get("/catalog/songs/song_001").andReturn().response.contentAsString
        val secondAudio = jsonString(second, "tracks", "audioUrl")

        // 이 API 의 존재 이유: 만료 시각이 뒤로 밀리고 서명 자체가 달라진다
        expiresOf(secondAudio) shouldBe expiresOf(firstAudio) + 300
        secondAudio shouldNotBe firstAudio
        secondAudio shouldMatch signedUrlPattern
    }

    @Test
    fun `없는 곡은 404를 반환한다`() {
        willThrow(SongNotFoundException(SongId("song_missing")))
            .given(getSongUseCase).getSong(SongId("song_missing"))

        mockMvc.get("/catalog/songs/song_missing").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `삭제된 곡은 404를 반환한다`() {
        // 유스케이스가 삭제 곡을 '없는 곡'과 동일하게 취급한다는 계약
        // (그 계약 자체는 CatalogServiceTest 가 실제 DB 로 검증한다)
        willThrow(SongNotFoundException(SongId("song_deleted")))
            .given(getSongUseCase).getSong(SongId("song_deleted"))

        mockMvc.get("/catalog/songs/song_deleted").andExpect {
            status { isNotFound() }
        }
    }

    /** 응답 JSON 에서 문자열 값 하나 꺼내기 (배열이면 첫 원소). */
    private fun jsonString(body: String, vararg path: String): String {
        var node = ObjectMapper().readTree(body)
        path.forEach { field ->
            node = node.get(field)
            while (node.isArray) node = node.get(0)
        }
        return node.asText()
    }
}
