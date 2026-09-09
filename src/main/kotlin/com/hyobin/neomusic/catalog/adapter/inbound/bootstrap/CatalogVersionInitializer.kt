package com.hyobin.neomusic.catalog.adapter.inbound.bootstrap

import com.hyobin.neomusic.catalog.adapter.outbound.persistence.CatalogVersionJpaEntity
import com.hyobin.neomusic.catalog.adapter.outbound.persistence.CatalogVersionJpaRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 서버 기동 시 전역 버전 행이 없으면 0으로 하나 만든다(멱등).
 *
 * 버전 발급은 이 행을 잠그고 진행하는데(`SELECT ... FOR UPDATE`), **없는 행은 잠글 수 없다.**
 * 행 생성을 발급 시점에 맡기면 최초 동시 요청에서 둘 다 잠금을 얻지 못한 채 삽입을 시도하게 되므로,
 * 기동 시점에 미리 만들어 그 경로 자체를 없앤다.
 *
 * `postgres` 프로필은 Flyway 마이그레이션이 같은 행을 먼저 넣으므로 여기서는 아무것도 하지 않는다.
 */
@Component
class CatalogVersionInitializer(
    private val repository: CatalogVersionJpaRepository,
) : ApplicationRunner {

    @Transactional
    override fun run(args: ApplicationArguments?) {
        if (!repository.existsById(CatalogVersionJpaEntity.SINGLETON_ID)) {
            repository.save(CatalogVersionJpaEntity())
        }
    }
}
