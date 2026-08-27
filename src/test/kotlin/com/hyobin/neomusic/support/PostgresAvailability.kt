package com.hyobin.neomusic.support

import java.net.InetSocketAddress
import java.net.Socket

/**
 * PostgreSQL 통합 테스트를 돌릴 수 있는 환경인지 판단한다.
 *
 * 실제 DB 검증은 중요하지만, DB 없이 클론한 사람의 `./gradlew test` 까지
 * 깨뜨릴 이유는 없다. 그래서 접속이 안 되면 테스트를 '실패'가 아니라 '건너뜀'으로 둔다.
 * (CI 에서는 postgres 서비스 컨테이너가 항상 떠 있으므로 반드시 실행된다)
 */
object PostgresAvailability {

    private val host: String = System.getenv("NEOMUSIC_DB_HOST") ?: "localhost"
    private val port: Int = System.getenv("NEOMUSIC_DB_PORT")?.toIntOrNull() ?: 5433

    @JvmStatic
    fun isReachable(): Boolean =
        runCatching {
            Socket().use { it.connect(InetSocketAddress(host, port), TIMEOUT_MS) }
        }.isSuccess

    private const val TIMEOUT_MS = 700
}
