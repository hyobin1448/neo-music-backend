package com.hyobin.neomusic.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

/**
 * 시간 소스를 빈으로 제공한다.
 * 서비스가 Clock을 주입받으면 테스트에서 고정 시각(Clock.fixed)을 넣어 시간 의존 로직을 검증할 수 있다.
 */
@Configuration
class TimeConfig {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
