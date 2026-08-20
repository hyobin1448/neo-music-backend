package com.hyobin.neomusic.playlist.adapter.inbound.web

import com.hyobin.neomusic.auth.application.AuthenticatedUser
import com.hyobin.neomusic.playlist.application.port.inbound.PlaylistUseCase
import com.hyobin.neomusic.playlist.domain.Playlist
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 사용자용 플레이리스트 API.
 *
 * AuthenticatedUser 파라미터로 '인증 필수'(토큰 없으면 401)이며,
 * 요청자(memberId)를 소유자/행위자로 넘겨 서비스가 소유권을 검증한다(남의 것 403).
 */
@RestController
@RequestMapping("/playlists")
class PlaylistController(
    private val playlistUseCase: PlaylistUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(user: AuthenticatedUser, @Valid @RequestBody request: CreatePlaylistRequest): PlaylistResponse =
        PlaylistResponse.from(playlistUseCase.create(user.memberId, request.name))

    @GetMapping
    fun list(user: AuthenticatedUser): List<PlaylistResponse> =
        playlistUseCase.list(user.memberId).map { PlaylistResponse.from(it) }

    @GetMapping("/{id}")
    fun get(user: AuthenticatedUser, @PathVariable id: Long): PlaylistResponse =
        PlaylistResponse.from(playlistUseCase.get(user.memberId, id))

    @PutMapping("/{id}")
    fun rename(user: AuthenticatedUser, @PathVariable id: Long, @Valid @RequestBody request: RenamePlaylistRequest): PlaylistResponse =
        PlaylistResponse.from(playlistUseCase.rename(user.memberId, id, request.name))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(user: AuthenticatedUser, @PathVariable id: Long) {
        playlistUseCase.delete(user.memberId, id)
    }

    @PostMapping("/{id}/songs")
    fun addSong(user: AuthenticatedUser, @PathVariable id: Long, @Valid @RequestBody request: AddSongRequest): PlaylistResponse =
        PlaylistResponse.from(playlistUseCase.addSong(user.memberId, id, request.songId))

    @DeleteMapping("/{id}/songs/{songId}")
    fun removeSong(user: AuthenticatedUser, @PathVariable id: Long, @PathVariable songId: String): PlaylistResponse =
        PlaylistResponse.from(playlistUseCase.removeSong(user.memberId, id, songId))

    @PutMapping("/{id}/songs")
    fun reorder(user: AuthenticatedUser, @PathVariable id: Long, @Valid @RequestBody request: ReorderRequest): PlaylistResponse =
        PlaylistResponse.from(playlistUseCase.reorder(user.memberId, id, request.songIds))
}

data class CreatePlaylistRequest(@field:NotBlank val name: String)
data class RenamePlaylistRequest(@field:NotBlank val name: String)
data class AddSongRequest(@field:NotBlank val songId: String)
data class ReorderRequest(@field:NotEmpty val songIds: List<String>)

data class PlaylistResponse(
    val id: Long,
    val name: String,
    val songIds: List<String>,
) {
    companion object {
        fun from(p: Playlist): PlaylistResponse = PlaylistResponse(p.id!!, p.name.value, p.songIds)
    }
}
