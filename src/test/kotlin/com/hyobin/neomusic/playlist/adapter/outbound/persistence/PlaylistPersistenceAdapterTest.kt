package com.hyobin.neomusic.playlist.adapter.outbound.persistence

import com.hyobin.neomusic.playlist.domain.Playlist
import com.hyobin.neomusic.playlist.domain.PlaylistName
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(PlaylistPersistenceAdapter::class)
class PlaylistPersistenceAdapterTest @Autowired constructor(
    private val adapter: PlaylistPersistenceAdapter,
    private val em: TestEntityManager,
) {

    @Test
    fun `저장하면 id가 발급되고 곡 순서가 그대로 복원된다`() {
        val playlist = Playlist.create(ownerId = 1, name = PlaylistName.of("내 애창곡")).apply {
            addSong("s3"); addSong("s1"); addSong("s2")
        }

        val saved = adapter.save(playlist)
        em.flush(); em.clear()

        saved.id.shouldNotBeNull()
        val found = adapter.findById(saved.id!!)!!
        found.name shouldBe PlaylistName.of("내 애창곡")
        found.songIds shouldBe listOf("s3", "s1", "s2")   // 저장한 순서 보존
    }

    @Test
    fun `수정하면 곡 구성과 순서가 통째로 교체된다`() {
        val saved = adapter.save(
            Playlist.create(1, PlaylistName.of("리스트")).apply { addSong("a"); addSong("b") },
        )
        em.flush(); em.clear()

        val loaded = adapter.findById(saved.id!!)!!
        loaded.reorder(listOf("b", "a"))
        loaded.addSong("c")
        adapter.save(loaded)
        em.flush(); em.clear()

        adapter.findById(saved.id!!)!!.songIds shouldBe listOf("b", "a", "c")
    }

    @Test
    fun `소유자별 목록을 생성 순으로 조회한다`() {
        adapter.save(Playlist.create(ownerId = 1, name = PlaylistName.of("첫번째")))
        adapter.save(Playlist.create(ownerId = 1, name = PlaylistName.of("두번째")))
        adapter.save(Playlist.create(ownerId = 2, name = PlaylistName.of("남의것")))
        em.flush(); em.clear()

        val mine = adapter.findByOwner(1)
        mine shouldHaveSize 2
        mine.map { it.name.value } shouldBe listOf("첫번째", "두번째")
    }
}
