package com.hyobin.neomusic

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

/**
 * 스모크 테스트: 스프링 컨텍스트가 정상적으로 로드되는지 확인한다.
 * (DataSource, JPA, 빈 구성이 깨지면 여기서 실패한다)
 */
@SpringBootTest
class NeoMusicApplicationTests {

    @Test
    fun contextLoads() {
        // 컨텍스트 로딩 자체가 검증 대상 — 본문 없음
    }
}
