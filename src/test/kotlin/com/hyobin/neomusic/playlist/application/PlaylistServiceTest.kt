package com.hyobin.neomusic.playlist.application

import com.hyobin.neomusic.auth.domain.ForbiddenException
import com.hyobin.neomusic.playlist.domain.PlaylistNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

/**
 * 플레이리스트 유스케이스를 실제 DB(H2)로 검증. 소유권 규칙에 집중.
 */
@SpringBootTest
@Transactional
class PlaylistServiceTest @Autowired constructor(
    private val service: PlaylistService,
) {
    private val owner = 1L
    private val stranger = 2L

    @Test
    fun `생성 후 곡을 추가하고 순서를 바꾼다`() {
        val p = service.create(owner, "내 애창곡")
        service.addSong(owner, p.id!!, "s1")
        service.addSong(owner, p.id!!, "s2")
        service.addSong(owner, p.id!!, "s3")
        val reordered = service.reorder(owner, p.id!!, listOf("s3", "s2", "s1"))

        reordered.songIds shouldBe listOf("s3", "s2", "s1")
    }

    @Test
    fun `소유자별 목록만 조회된다`() {
        service.create(owner, "A")
        service.create(owner, "B")
        service.create(stranger, "남의것")

        service.list(owner) shouldHaveSize 2
    }

    @Test
    fun `남의 플레이리스트를 조작하면 403`() {
        val p = service.create(owner, "비공개")

        shouldThrow<ForbiddenException> { service.get(stranger, p.id!!) }
        shouldThrow<ForbiddenException> { service.addSong(stranger, p.id!!, "s1") }
        shouldThrow<ForbiddenException> { service.rename(stranger, p.id!!, "해킹") }
        shouldThrow<ForbiddenException> { service.delete(stranger, p.id!!) }
    }

    @Test
    fun `없는 플레이리스트는 404`() {
        shouldThrow<PlaylistNotFoundException> { service.get(owner, 999L) }
    }

    @Test
    fun `삭제하면 목록에서 사라진다`() {
        val p = service.create(owner, "지울것")
        service.delete(owner, p.id!!)
        service.list(owner) shouldHaveSize 0
    }
}
