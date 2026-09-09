package com.hyobin.neomusic.catalog.adapter.outbound.persistence

import com.hyobin.neomusic.catalog.application.port.outbound.CatalogVersionPort
import org.springframework.stereotype.Repository

/**
 * 전역 버전 포트의 JPA 구현.
 *
 * 델타 동기화의 커서가 이 값이므로, **버전이 겹치거나 순서가 뒤집히면 클라이언트가 변경분을 놓친다.**
 * 그래서 발급은 행을 잠근 채로 한다([CatalogVersionJpaRepository.findAndLock]).
 * 잠금이 커밋까지 유지되므로 발급 순서와 커밋 순서가 같아진다.
 */
@Repository
class CatalogVersionPersistenceAdapter(
    private val repository: CatalogVersionJpaRepository,
) : CatalogVersionPort {

    override fun current(): Long =
        repository.findById(CatalogVersionJpaEntity.SINGLETON_ID)
            .map { it.version }
            .orElse(0)

    /**
     * 다음 버전을 발급한다. 반드시 트랜잭션 안에서 호출해야 한다(그래야 잠금이 유지된다).
     *
     * 행이 없으면 만들고 진행한다. 실제로는 [CatalogVersionInitializer] 가 기동 시 넣어두므로
     * 이 경로는 슬라이스 테스트처럼 초기화가 돌지 않은 환경을 위한 대비다.
     */
    override fun next(): Long {
        val entity = repository.findAndLock(CatalogVersionJpaEntity.SINGLETON_ID)
            ?: repository.saveAndFlush(CatalogVersionJpaEntity())
        entity.version += 1
        return repository.saveAndFlush(entity).version
    }
}
