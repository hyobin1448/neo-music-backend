package com.hyobin.neomusic.common.config

import com.hyobin.neomusic.auth.application.AuthenticatedUser
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.utils.SpringDocUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Swagger UI(/swagger-ui.html) 와 OpenAPI 문서(/v3/api-docs) 설정.
 *
 * 우측 상단 Authorize 버튼에 로그인 토큰(accessToken)을 넣으면
 * 관리자 전용 API도 브라우저에서 바로 호출해볼 수 있다.
 */
@Configuration
class OpenApiConfig {

    init {
        // AuthenticatedUser 는 토큰에서 주입되는 값 — 요청 파라미터가 아니므로 문서에서 숨긴다
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(AuthenticatedUser::class.java)
    }

    @Bean
    fun neoMusicOpenApi(): OpenAPI {
        val bearerJwt = "bearer-jwt"
        return OpenAPI()
            .info(
                Info()
                    .title("Neo Music API")
                    .version("v0.1.0")
                    .description(
                        "다국어 오디오 · 가사/기타코드 플레이어의 백엔드. " +
                            "헥사고날 아키텍처 + DDD 로 구성. " +
                            "관리자 API 는 Authorize 에 로그인 토큰을 넣고 호출하세요.",
                    ),
            )
            .components(
                Components().addSecuritySchemes(
                    bearerJwt,
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("POST /auth/login 으로 받은 accessToken 값을 그대로 입력"),
                ),
            )
    }
}
