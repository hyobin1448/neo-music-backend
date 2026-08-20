package com.hyobin.neomusic.playlist.application.port.inbound

import com.hyobin.neomusic.playlist.domain.Playlist

/**
 * 입력 포트: 플레이리스트 유스케이스(사용자용).
 *
 * 모든 조작은 actorId(요청한 회원)를 받아 소유권을 검증한다.
 * 남의 플레이리스트를 건드리면 실패한다(403).
 * 액션이 한 애그리거트에 응집돼 있어 하나의 포트로 묶는다.
 */
interface PlaylistUseCase {
    fun create(ownerId: Long, name: String): Playlist
    fun rename(actorId: Long, playlistId: Long, newName: String): Playlist
    fun delete(actorId: Long, playlistId: Long)

    fun addSong(actorId: Long, playlistId: Long, songId: String): Playlist
    fun removeSong(actorId: Long, playlistId: Long, songId: String): Playlist
    fun reorder(actorId: Long, playlistId: Long, orderedSongIds: List<String>): Playlist

    fun list(ownerId: Long): List<Playlist>
    fun get(actorId: Long, playlistId: Long): Playlist
}
