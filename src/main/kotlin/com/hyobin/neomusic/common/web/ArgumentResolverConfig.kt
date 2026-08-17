package com.hyobin.neomusic.common.web

import com.hyobin.neomusic.auth.adapter.inbound.web.CurrentUserArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * 컨트롤러가 AuthenticatedUser 파라미터를 받을 수 있도록 리졸버를 등록한다.
 */
@Configuration
class ArgumentResolverConfig : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(CurrentUserArgumentResolver())
    }
}
