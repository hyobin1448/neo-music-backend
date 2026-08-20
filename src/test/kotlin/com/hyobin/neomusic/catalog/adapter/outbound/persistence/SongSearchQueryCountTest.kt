package com.hyobin.neomusic.catalog.adapter.outbound.persistence

import com.hyobin.neomusic.catalog.domain.Checksum
import com.hyobin.neomusic.catalog.domain.Lang
import com.hyobin.neomusic.catalog.domain.Lyric
import com.hyobin.neomusic.catalog.domain.LyricType
import com.hyobin.neomusic.catalog.domain.Song
import com.hyobin.neomusic.catalog.domain.SongId
import com.hyobin.neomusic.catalog.domain.StorageKey
import com.hyobin.neomusic.catalog.domain.Track
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.hibernate.SessionFactory
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

/**
 * 검색이 곡 수에 비례해 쿼리를 늘리지 않는지(N+1 방지)를 Hibernate 통계로 검증.
 * SUBSELECT 페치 덕에 tracks/lyrics/imageKeys 는 곡마다가 아니라 한 번에 로드된다.
 */
@DataJpaTest
@Import(SongPersistenceAdapter::class)
class SongSearchQueryCountTest @Autowired constructor(
    private val adapter: SongPersistenceAdapter,
    private val em: TestEntityManager,
) {
    private fun song(id: String) = Song.create(
        id = SongId(id),
        title = "테스트곡-$id",
        artist = "가수",
        tracks = listOf(Track(Lang.of("ko"), "한국어", StorageKey("s/$id.m4a"), 1, 1, Checksum("c"))),
        lyrics = listOf(Lyric(Lang.of("ko"), LyricType.IMAGE, listOf(StorageKey("s/$id.png")))),
    )

    @Test
    fun `검색 결과가 6곡이어도 쿼리 수는 곡 수에 비례하지 않는다`() {
        repeat(6) { adapter.save(song("s$it"), it.toLong()) }
        em.flush(); em.clear()

        val stats = em.entityManager.entityManagerFactory
            .unwrap(SessionFactory::class.java).statistics
        stats.isStatisticsEnabled = true
        stats.clear()

        val results = adapter.searchActive("테스트곡")   // toDomain 매핑 시 자식 컬렉션까지 로드됨

        results shouldHaveSize 6
        // N+1이면 1(곡) + 6(tracks) + 6(lyrics) + 6(images) = 19.
        // SUBSELECT면 곡 수와 무관하게 소수(곡1 + 컬렉션 서브셀렉트 몇 개)에 그친다.
        withClue("실행된 SQL 문 수 = ${stats.prepareStatementCount}") {
            (stats.prepareStatementCount < 8) shouldBe true
        }
    }
}
