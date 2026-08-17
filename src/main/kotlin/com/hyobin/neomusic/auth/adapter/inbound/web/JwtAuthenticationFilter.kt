package com.hyobin.neomusic.auth.adapter.inbound.web

import com.hyobin.neomusic.auth.application.port.outbound.TokenParserPort
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 모든 요청에서 'Authorization: Bearer <토큰>'을 검사한다.
 * 유효하면 사용자 정보를 요청 속성에 심어둔다(뒤의 리졸버가 꺼내 씀).
 * 여기서 막지는 않는다 — "인증 필요/권한" 판단은 각 엔드포인트에서 한다.
 */
class JwtAuthenticationFilter(
    private val tokenParser: TokenParserPort,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            val token = header.removePrefix(BEARER_PREFIX).trim()
            tokenParser.parse(token)?.let { user ->
                request.setAttribute(CURRENT_USER_ATTR, user)
            }
        }
        filterChain.doFilter(request, response)
    }

    companion object {
        const val BEARER_PREFIX = "Bearer "
        const val CURRENT_USER_ATTR = "neomusic.currentUser"
    }
}
