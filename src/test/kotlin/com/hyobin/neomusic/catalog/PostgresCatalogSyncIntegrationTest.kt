package com.hyobin.neomusic.catalog

import com.hyobin.neomusic.catalog.application.CatalogService
import com.hyobin.neomusic.catalog.domain.Checksum
import com.hyobin.neomusic.catalog.domain.Lang
import com.hyobin.neomusic.catalog.domain.Song
import com.hyobin.neomusic.catalog.domain.SongId
import com.hyobin.neomusic.catalog.domain.StorageKey
import com.hyobin.neomusic.catalog.domain.Track
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import javax.sql.DataSource

/**
 * 실제 PostgreSQL 에서 매핑과 델타 동기화를 검증하는 통합 테스트.
 *
 * 나머지 테스트는 H2(인메모리)로 빠르게 돌지만, H2 는 타입·예약어·제약조건을
 * 느슨하게 받아준다. "H2 에서는 되는데 운영 DB 에서는 깨지는" 문제를 잡으려면
 * 핵심 시나리오만큼은 진짜 DB 에서 확인해야 한다.
 *
 *   로컬:  docker compose up -d  후 ./gradlew test
 *   CI:    postgres 서비스 컨테이너에서 자동 실행 (.github/workflows/ci.yml)
 *
 * DB 가 없으면 실패가 아니라 건너뛴다 — DB 없이 클론한 사람의 빌드를 깨지 않기 위해.
 */
@SpringBootTest
@ActiveProfiles("postgres")
@EnabledIf(
    value = "com.hyobin.neomusic.support.PostgresAvailability#isReachable",
    disabledReason = "PostgreSQL 이 떠 있지 않음 — `docker compose up -d` 후 다시 실행",
)
class PostgresCatalogSyncIntegrationTest @Autowired constructor(
    private val catalog: CatalogService,
    private val dataSource: DataSource,
) {

    /** 실행할 때마다 다른 id 를 쓴다 — postgres 프로필은 데이터가 남아 있기 때문. */
    private val songId = "pg_sync_${System.nanoTime()}"

    @Test
    fun `H2 가 아니라 실제 PostgreSQL 위에서 스키마가 생성된다`() {
        dataSource.connection.use { conn ->
            conn.metaData.databaseProductName shouldBe "PostgreSQL"

            val tables = mutableSetOf<String>()
            conn.metaData.getTables(null, "public", "%", arrayOf("TABLE")).use { rs ->
                while (rs.next()) tables += rs.getString("TABLE_NAME").lowercase()
            }
            listOf("song", "track", "lyric", "catalog_version", "member", "playlist")
                .forEach { tables shouldContain it }
        }
    }

    @Test
    fun `등록-수정-삭제가 델타 동기화로 전파된다`() {
        // 이 테스트 시작 시점의 버전을 기준점으로 잡는다 (기존 데이터는 무시)
        val baseVersion = catalog.getCatalog(null).version

        // ── 등록 → since 조회에 잡힌다
        catalog.register(song("아리랑", order = 1))
        val afterRegister = catalog.getCatalog(baseVersion)
        afterRegister.changed.map { it.id.value } shouldContain songId
        afterRegister.deleted.map { it.value } shouldNotContain songId

        // ── 수정 → 새 버전으로 다시 잡힌다
        catalog.update(song("아리랑 (개정)", order = 2))
        val afterUpdate = catalog.getCatalog(afterRegister.version)
        afterUpdate.changed.single { it.id.value == songId }.title shouldBe "아리랑 (개정)"

        // ── 삭제 → 물리 삭제가 아니라 tombstone 으로 전파된다
        catalog.delete(SongId(songId))
        val afterDelete = catalog.getCatalog(afterUpdate.version)
        afterDelete.deleted.map { it.value } shouldContain songId
        afterDelete.changed.map { it.id.value } shouldNotContain songId

        // ── 삭제된 곡은 신규 클라이언트의 첫 동기화에는 아예 내려가지 않는다
        catalog.getCatalog(null).changed.map { it.id.value } shouldNotContain songId
    }

    private fun song(title: String, order: Int): Song =
        Song.create(
            id = SongId(songId),
            title = title,
            artist = "전통",
            coverKey = StorageKey("covers/$songId.jpg"),
            displayOrder = order,
        ).apply {
            addTrack(
                Track(
                    Lang.of("ko"), "한국어",
                    StorageKey("songs/$songId/ko.m4a"),
                    200_000, 3_000_000, Checksum("sha256:$songId"),
                ),
            )
        }
}
