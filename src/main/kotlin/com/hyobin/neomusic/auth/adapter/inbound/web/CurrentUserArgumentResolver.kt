package com.hyobin.neomusic.auth.adapter.inbound.web

import com.hyobin.neomusic.auth.application.AuthenticatedUser
import com.hyobin.neomusic.auth.domain.UnauthenticatedException
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * 컨트롤러 메서드의 AuthenticatedUser 파라미터를 채워준다.
 * 필터가 심어둔 사용자 속성을 꺼내되, 없으면 = 미인증 → 401 예외.
 * 즉, 컨트롤러가 AuthenticatedUser를 받으면 그 엔드포인트는 '인증 필수'가 된다.
 */
class CurrentUserArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.parameterType == AuthenticatedUser::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any {
        val user = webRequest.getAttribute(
            JwtAuthenticationFilter.CURRENT_USER_ATTR,
            RequestAttributes.SCOPE_REQUEST,
        ) as? AuthenticatedUser
        return user ?: throw UnauthenticatedException()
    }
}
