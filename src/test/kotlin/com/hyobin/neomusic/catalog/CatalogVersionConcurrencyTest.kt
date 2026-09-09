package com.hyobin.neomusic.catalog

import com.hyobin.neomusic.catalog.application.port.outbound.CatalogVersionPort
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * 전역 버전은 델타 동기화의 **커서**다.
 * 같은 값이 두 번 발급되면 클라이언트가 `since` 를 넘길 때 한쪽 변경분을 통째로 건너뛴다.
 *
 * 발급이 락 없는 읽고-더하고-쓰기였을 때 이 테스트는 실패한다(중복 발급).
 * 지금은 행을 잠그고 발급하므로([CatalogVersionJpaRepository.findAndLock]) 동시 호출에서도 값이 겹치지 않는다.
 *
 * 주의: 동시성을 보려면 **실제로 커밋해야** 하므로 다른 테스트처럼 `@Transactional` 롤백에 기댈 수 없다.
 * 대신 끝나고 카운터를 직접 되돌려, 같은 컨텍스트를 쓰는 테스트들이 이 테스트의 흔적을 보지 않게 한다.
 */
@SpringBootTest
class CatalogVersionConcurrencyTest {

    @Autowired
    private lateinit var versionPort: CatalogVersionPort

    @Autowired
    private lateinit var tx: TransactionTemplate

    @Autowired
    private lateinit var jdbc: JdbcTemplate

    private var before: Long = 0

    @AfterEach
    fun restoreCounter() {
        jdbc.update("update catalog_version set version = ? where id = 1", before)
    }

    @Test
    fun `동시에 발급해도 버전이 겹치지 않는다`() {
        before = versionPort.current()

        val threads = 8
        val issued = ConcurrentLinkedQueue<Long>()
        val start = CountDownLatch(1)
        val done = CountDownLatch(threads)
        val pool = Executors.newFixedThreadPool(threads)

        repeat(threads) {
            pool.submit {
                try {
                    start.await()
                    // 각 스레드가 자기 트랜잭션에서 발급 → 커밋까지 락이 유지되어야 한다
                    tx.execute { issued += versionPort.next() }
                } finally {
                    done.countDown()
                }
            }
        }
        start.countDown()
        done.await(30, TimeUnit.SECONDS) shouldBe true
        pool.shutdown()

        // 중복이 있으면 distinct 크기가 줄어든다 = 동기화가 변경분을 건너뛴다는 뜻
        issued shouldHaveSize threads
        issued.distinct() shouldHaveSize threads
        issued.sorted() shouldBe (before + 1..before + threads).toList()
        versionPort.current() shouldBe before + threads
    }
}
