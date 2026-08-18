package com.hyobin.neomusic.catalog.adapter.inbound.web

import com.hyobin.neomusic.auth.application.AuthenticatedUser
import com.hyobin.neomusic.catalog.application.port.inbound.RegisterSongUseCase
import com.hyobin.neomusic.catalog.application.port.inbound.UpdateSongUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 관리자용 곡 관리 엔드포인트.
 *
 * 파라미터로 AuthenticatedUser 를 받으므로 '인증 필수'(토큰 없으면 401),
 * 메서드 안에서 requireAdmin() 으로 '관리자 전용'(USER면 403)을 강제한다.
 *
 * POST /admin/songs      → 곡 신규 등록(201). 이미 있으면 409.
 * PUT  /admin/songs/{id} → 곡 수정(200). 없으면 404.
 * 둘 다 전역 버전이 올라가 델타 동기화로 앱에 전파된다.
 */
@RestController
@RequestMapping("/admin/songs")
class AdminSongController(
    private val registerSongUseCase: RegisterSongUseCase,
    private val updateSongUseCase: UpdateSongUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        user: AuthenticatedUser,
        @Valid @RequestBody request: RegisterSongRequest,
    ): SongResponse {
        user.requireAdmin()
        val saved = registerSongUseCase.register(request.toDomain())
        return SongResponse.from(saved)
    }

    @PutMapping("/{id}")
    fun update(
        user: AuthenticatedUser,
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateSongRequest,
    ): SongResponse {
        user.requireAdmin()
        val saved = updateSongUseCase.update(request.toDomain(id))
        return SongResponse.from(saved)
    }
}
