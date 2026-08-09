import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"          // @Component 등 스프링 빈 클래스를 자동으로 open 처리
    kotlin("plugin.jpa") version "1.9.25"             // @Entity에 no-arg 생성자 + open 자동 부여
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.hyobin"
version = "0.1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // 웹 / 영속성 / 검증
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Kotlin 지원 (JSON 직렬화, 리플렉션)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // 개발/테스트용 인메모리 DB
    runtimeOnly("com.h2database:h2")

    // 테스트: Kotest(코틀린 테스트) + MockK(모킹) + ArchUnit(아키텍처 규칙 검증)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"          // 자바 null 어노테이션을 엄격히 해석 → Kotlin null 안전성 강화
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
