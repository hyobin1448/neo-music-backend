package com.hyobin.neomusic.catalog.adapter.outbound.persistence

import com.hyobin.neomusic.catalog.domain.Checksum
import com.hyobin.neomusic.catalog.domain.Lang
import com.hyobin.neomusic.catalog.domain.Lyric
import com.hyobin.neomusic.catalog.domain.LyricType
import com.hyobin.neomusic.catalog.domain.Song
import com.hyobin.neomusic.catalog.domain.SongId
import com.hyobin.neomusic.catalog.domain.StorageKey
import com.hyobin.neomusic.catalog.domain.Track
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

/**
 * 영속성 어댑터 통합 테스트 (실제 H2 DB 사용).
 * 저장 후 영속성 컨텍스트를 비우고 다시 읽어, 진짜 DB 왕복이 맞는지 검증한다.
 */
@DataJpaTest
@Import(SongPersistenceAdapter::class)
class SongPersistenceAdapterTest @Autowired constructor(
    private val adapter: SongPersistenceAdapter,
    private val em: TestEntityManager,
) {

    @Test
    fun `곡을 저장하고 다시 읽으면 값이 그대로 복원된다`() {
        val song = Song.create(
            id = SongId("song_001"),
            title = "아리랑",
            artist = "전통",
            coverKey = StorageKey("covers/song_001.jpg"),
            displayOrder = 1,
        ).apply {
            addTrack(Track(Lang.of("ko"), "한국어", StorageKey("songs/song_001/ko.m4a"), 200_000, 3_000_000, Checksum("sha256:ko")))
            addTrack(Track(Lang.of("en"), "English", StorageKey("songs/song_001/en.m4a"), 190_000, 2_900_000, Checksum("sha256:en")))
            addLyric(Lyric(Lang.of("ko"), LyricType.IMAGE, listOf(StorageKey("songs/song_001/ko_1.png"), StorageKey("songs/song_001/ko_2.png"))))
        }

        adapter.save(song, version = 1)
        em.flush()   // DB로 실제 반영
        em.clear()   // 1차 캐시 비우기 → findById가 진짜 DB에서 다시 읽도록

        val loaded = adapter.findById(SongId("song_001"))!!

        loaded.title shouldBe "아리랑"
        loaded.artist shouldBe "전통"
        loaded.coverKey shouldBe StorageKey("covers/song_001.jpg")
        loaded.displayOrder shouldBe 1
        loaded.isDeleted shouldBe false
        loaded.tracks shouldHaveSize 2
        loaded.tracks.map { it.lang.code }.toSet() shouldBe setOf("ko", "en")
        loaded.lyrics shouldHaveSize 1
        loaded.lyrics.first().imageKeys shouldHaveSize 2
    }

    @Test
    fun `같은 id로 다시 저장하면 중복 없이 갱신된다 (UPSERT)`() {
        val song = Song.create(SongId("song_002"), "제목", "가수")
        adapter.save(song, version = 1)
        em.flush(); em.clear()

        // 제목을 바꿔 같은 id로 재저장
        val reloaded = adapter.findById(SongId("song_002"))!!
        reloaded.rename("바뀐제목", "가수")
        adapter.save(reloaded, version = 2)
        em.flush(); em.clear()

        adapter.findAll() shouldHaveSize 1                 // 행이 늘어나지 않음
        adapter.findById(SongId("song_002"))!!.title shouldBe "바뀐제목"
    }
}
