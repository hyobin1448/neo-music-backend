package com.hyobin.neomusic.common.web

import com.hyobin.neomusic.auth.adapter.inbound.web.JwtAuthenticationFilter
import com.hyobin.neomusic.auth.application.port.outbound.TokenParserPort
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * JWT 인증 필터를 서블릿 필터 체인에 등록한다.
 */
@Configuration
class JwtFilterConfig {
    @Bean
    fun jwtAuthenticationFilterRegistration(
        tokenParser: TokenParserPort,
    ): FilterRegistrationBean<JwtAuthenticationFilter> {
        val registration = FilterRegistrationBean(JwtAuthenticationFilter(tokenParser))
        registration.addUrlPatterns("/*")
        registration.order = 1
        return registration
    }
}
