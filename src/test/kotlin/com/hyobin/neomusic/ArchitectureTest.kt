package com.hyobin.neomusic

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition
import org.junit.jupiter.api.Test

/**
 * 헥사고날 아키텍처 규칙을 문서가 아니라 테스트로 강제한다.
 *
 * README 에 "의존성은 항상 안쪽을 향한다"고 적어두는 것만으로는 시간이 지나면 무너진다.
 * 규칙을 깨는 import 가 들어오면 이 테스트가 CI 에서 실패해 머지를 막는다.
 *
 * 계층: adapter → application → domain  (화살표 반대 방향 의존은 금지)
 */
class ArchitectureTest {

    private val classes: JavaClasses = ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages(ROOT)

    // ── 1. 도메인은 기술을 모른다 ────────────────────────────────

    @Test
    fun `domain 은 프레임워크에 의존하지 않는다`() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework..",   // 스프링
                "jakarta..",               // JPA / 서블릿 / validation
                "io.jsonwebtoken..",       // JWT
                "com.fasterxml.jackson..", // JSON 직렬화
            )
            .because("도메인 규칙은 순수 Kotlin 이어야 프레임워크를 갈아끼워도 살아남는다")
            .check(classes)
    }

    @Test
    fun `domain 은 바깥 계층을 모른다`() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..application..", "..adapter..")
            .because("의존성은 안쪽(domain)을 향해야 한다 — 도메인이 바깥을 알면 방향이 뒤집힌다")
            .check(classes)
    }

    // ── 2. 유스케이스는 구현 기술을 모른다 ──────────────────────────

    @Test
    fun `application 은 adapter 를 모른다`() {
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..adapter..")
            .because("유스케이스는 포트(인터페이스)에만 의존하고, 구현은 런타임에 주입받는다")
            .check(classes)
    }

    @Test
    fun `application 은 영속성과 웹 기술을 모른다`() {
        // @Service / @Transactional 은 의도적으로 허용한다.
        // 유스케이스를 빈으로 등록하고 트랜잭션 경계를 선언하는 용도로만 쓰며,
        // 그 외 스프링 기능(web, data, context)이 새어 들어오는 것은 막는다.
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "jakarta.persistence..",
                "org.springframework.data..",
                "org.springframework.web..",
                "org.springframework.http..",
            )
            .because("유스케이스가 JPA·HTTP 를 알면 저장소나 전송 방식을 바꿀 때 함께 무너진다")
            .check(classes)
    }

    // ── 3. 기술 세부사항은 어댑터 안에 갇혀 있다 ────────────────────

    @Test
    fun `JPA 엔티티는 outbound persistence 어댑터 안에만 존재한다`() {
        classes()
            .that().areAnnotatedWith("jakarta.persistence.Entity")
            .should().resideInAPackage("..adapter.outbound.persistence..")
            .because("도메인 모델과 DB 테이블 매핑은 분리한다 — 스키마가 도메인을 끌고 다니지 않도록")
            .check(classes)
    }

    // ── 4. 바운디드 컨텍스트 간 경계 ───────────────────────────────

    @Test
    fun `컨텍스트는 다른 컨텍스트의 내부를 직접 참조하지 않는다`() {
        CONTEXTS.forEach { context ->
            val othersInternals = (CONTEXTS - context)
                .flatMap { listOf("$ROOT.$it.domain..", "$ROOT.$it.adapter..") }
                .toTypedArray()

            noClasses()
                .that().resideInAPackage("$ROOT.$context..")
                .should().dependOnClassesThat().resideInAnyPackage(*othersInternals)
                .because("컨텍스트끼리는 공개된 application 계층으로만 대화한다 (내부 모델·어댑터는 비공개)")
                .check(classes)
        }
    }

    @Test
    fun `컨텍스트 간 순환 의존이 없다`() {
        val contextClasses = ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(*CONTEXTS.map { "$ROOT.$it" }.toTypedArray())

        SlicesRuleDefinition.slices()
            .matching("$ROOT.(*)..")
            .should().beFreeOfCycles()
            .because("순환이 생기면 컨텍스트를 따로 배포하거나 떼어낼 수 없다")
            .check(contextClasses)
    }

    private companion object {
        const val ROOT = "com.hyobin.neomusic"
        val CONTEXTS = listOf("auth", "catalog", "playlist", "storage")
    }
}
