package com.hyobin.neomusic.catalog.application

import com.hyobin.neomusic.catalog.domain.Checksum
import com.hyobin.neomusic.catalog.domain.Lang
import com.hyobin.neomusic.catalog.domain.Lyric
import com.hyobin.neomusic.catalog.domain.LyricType
import com.hyobin.neomusic.catalog.domain.Song
import com.hyobin.neomusic.catalog.domain.SongAlreadyExistsException
import com.hyobin.neomusic.catalog.domain.SongId
import com.hyobin.neomusic.catalog.domain.SongNotFoundException
import com.hyobin.neomusic.catalog.domain.StorageKey
import com.hyobin.neomusic.catalog.domain.Track
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

/**
 * 델타 동기화 시나리오를 실제 DB(H2)로 검증하는 통합 테스트.
 * 이 프로젝트의 핵심 로직이라 여러 케이스를 꼼꼼히 확인한다.
 */
@SpringBootTest
@Transactional  // 각 테스트 후 롤백 → 서로 격리
class CatalogServiceTest @Autowired constructor(
    private val catalogService: CatalogService,
) {

    private fun song(id: String, title: String = "제목", order: Int = 0): Song =
        Song.create(
            id = SongId(id),
            title = title,
            artist = "가수",
            displayOrder = order,
            tracks = listOf(
                Track(Lang.of("ko"), "한국어", StorageKey("s/$id/ko.m4a"), 1000, 100, Checksum("c-$id")),
            ),
            lyrics = listOf(
                Lyric(Lang.of("ko"), LyricType.IMAGE, listOf(StorageKey("s/$id/ko.png"))),
            ),
        )

    @Test
    fun `첫 동기화(since 없음)는 활성 곡 전체를 반환한다`() {
        catalogService.register(song("song_001", order = 1))
        catalogService.register(song("song_002", order = 2))

        val snapshot = catalogService.getCatalog(since = null)

        snapshot.changed shouldHaveSize 2
        snapshot.deleted.shouldBeEmpty()
        snapshot.version shouldBe 2
    }

    @Test
    fun `since 이후에 바뀐 곡만 반환한다`() {
        catalogService.register(song("song_001"))                 // 버전 1
        val versionAfterFirst = catalogService.getCatalog(null).version  // = 1
        catalogService.register(song("song_002"))                 // 버전 2

        val delta = catalogService.getCatalog(since = versionAfterFirst)

        delta.changed.map { it.id.value } shouldBe listOf("song_002")
        delta.version shouldBe 2
    }

    @Test
    fun `같은 id로 다시 등록하면 SongAlreadyExists로 거부한다`() {
        catalogService.register(song("song_001"))
        shouldThrow<SongAlreadyExistsException> {
            catalogService.register(song("song_001", title = "다른제목"))
        }
    }

    @Test
    fun `없는 곡을 수정하면 SongNotFound로 거부한다`() {
        shouldThrow<SongNotFoundException> {
            catalogService.update(song("nope"))
        }
    }

    @Test
    fun `수정하면 내용이 바뀌고 버전이 올라간다`() {
        catalogService.register(song("song_001", title = "원제"))   // 버전 1
        catalogService.update(song("song_001", title = "수정본"))    // 버전 2

        val snapshot = catalogService.getCatalog(since = null)
        snapshot.changed.single().title shouldBe "수정본"
        snapshot.version shouldBe 2
    }

    @Test
    fun `없는 곡을 삭제하면 SongNotFound로 거부한다`() {
        shouldThrow<SongNotFoundException> {
            catalogService.delete(SongId("nope"))
        }
    }

    @Test
    fun `삭제하면 tombstone으로 전파되고, 첫 동기화에는 안 보인다`() {
        catalogService.register(song("song_001"))                 // 버전 1
        val v1 = catalogService.getCatalog(null).version          // = 1
        catalogService.delete(SongId("song_001"))                 // 버전 2, 삭제 표시

        // 델타에는 deleted로 나온다
        val delta = catalogService.getCatalog(since = v1)
        delta.changed.shouldBeEmpty()
        delta.deleted shouldBe listOf(SongId("song_001"))

        // 첫 동기화(새 사용자)에는 삭제된 곡이 아예 안 보인다
        catalogService.getCatalog(null).changed.shouldBeEmpty()
    }
}
