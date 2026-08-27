package com.hyobin.neomusic.catalog.application

import com.hyobin.neomusic.catalog.application.port.inbound.GetCatalogUseCase
import com.hyobin.neomusic.catalog.domain.Song
import com.hyobin.neomusic.catalog.domain.SongId
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

/**
 * 카탈로그 동기화 페이징.
 *
 * 전역 버전은 변경 1건마다 하나씩 발급되어 곡마다 값이 겹치지 않는다.
 * 즉 lastModifiedVersion 하나로 전체 순서가 정해지므로 커서로 쓸 수 있고,
 * offset 페이징과 달리 조회 중 데이터가 바뀌어도 건너뛰거나 중복되지 않는다.
 */
@SpringBootTest
@Transactional
class CatalogPagingTest @Autowired constructor(
    private val catalogService: CatalogService,
) {

    private fun register(id: String) = catalogService.register(Song.create(SongId(id), "제목", "가수"))

    /** hasMore 가 false 가 될 때까지 nextSince 로 이어받아 전부 모은다. */
    private fun syncAll(limit: Int): List<String> {
        val collected = mutableListOf<String>()
        var snapshot = catalogService.getCatalog(since = null, limit = limit)
        collected += snapshot.changed.map { it.id.value }
        while (snapshot.hasMore) {
            snapshot = catalogService.getCatalog(since = snapshot.nextSince, limit = limit)
            collected += snapshot.changed.map { it.id.value }
        }
        return collected
    }

    @Test
    fun `limit 보다 많으면 잘라서 주고 hasMore 로 알린다`() {
        repeat(5) { register("song_$it") }

        val first = catalogService.getCatalog(since = null, limit = 2)

        first.changed shouldHaveSize 2
        first.hasMore shouldBe true
    }

    @Test
    fun `nextSince 로 이어받으면 누락도 중복도 없이 전부 받는다`() {
        repeat(5) { register("song_$it") }

        // 한 번에 2곡씩 끊어 받아도 5곡이 등록 순서대로 정확히 모인다
        syncAll(limit = 2) shouldContainExactly
            listOf("song_0", "song_1", "song_2", "song_3", "song_4")
    }

    @Test
    fun `다 받으면 hasMore 는 false 이고 nextSince 는 현재 버전이다`() {
        repeat(3) { register("song_$it") }

        val snapshot = catalogService.getCatalog(since = null, limit = 10)

        snapshot.hasMore shouldBe false
        snapshot.nextSince shouldBe snapshot.version
    }

    /**
     * 이 API 를 쓰는 쪽이 가장 하기 쉬운 실수를 테스트로 박아둔다.
     * 덜 받았는데 version(전역 현재 버전)을 다음 since 로 쓰면, 아직 안 받은
     * 페이지를 통째로 건너뛰고 영영 못 받는다. 그래서 nextSince 를 따로 내려준다.
     */
    @Test
    fun `덜 받았을 때 nextSince 와 version 은 다르다`() {
        repeat(5) { register("song_$it") }

        val first = catalogService.getCatalog(since = null, limit = 2)

        first.hasMore shouldBe true
        first.nextSince shouldBe 2L     // 방금 받은 마지막 곡의 버전 — 여기서 이어받아야 한다
        first.version shouldBe 5L       // 전역 현재 버전 — 이걸 쓰면 song_2~4 를 건너뛴다
    }

    @Test
    fun `삭제도 페이지에 나뉘어 tombstone 으로 전파된다`() {
        repeat(4) { register("song_$it") }
        val afterRegister = catalogService.getCatalog(since = null, limit = 10).version
        catalogService.delete(SongId("song_0"))
        catalogService.delete(SongId("song_1"))

        val page = catalogService.getCatalog(since = afterRegister, limit = 1)

        page.deleted.map { it.value } shouldContainExactly listOf("song_0")
        page.hasMore shouldBe true

        val next = catalogService.getCatalog(since = page.nextSince, limit = 1)
        next.deleted.map { it.value } shouldContainExactly listOf("song_1")
        next.hasMore shouldBe false
    }

    @Test
    fun `limit 은 상한과 하한으로 잘린다`() {
        repeat(3) { register("song_$it") }

        // 0 이나 음수를 보내도 최소 1건은 온다
        catalogService.getCatalog(since = null, limit = 0).changed shouldHaveSize 1
        // 상한을 넘겨 보내도 예외 없이 처리된다
        catalogService.getCatalog(since = null, limit = GetCatalogUseCase.MAX_LIMIT * 10)
            .changed shouldHaveSize 3
    }
}
