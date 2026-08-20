package com.hyobin.neomusic.admin.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

/**
 * 관리자 웹 페이지(셸)를 렌더한다.
 *
 * 인증은 stateless JWT 라, 페이지는 껍데기만 서버가 내려주고
 * 실제 로그인/조회/등록은 브라우저 JS 가 기존 REST API 를 토큰과 함께 호출한다.
 */
@Controller
class AdminWebController {

    @GetMapping("/admin")
    fun index(): String = "admin/index"
}
