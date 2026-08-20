package com.hyobin.neomusic.playlist.application.port.outbound

import com.hyobin.neomusic.playlist.domain.Playlist

/** 출력 포트: 플레이리스트 저장(신규/갱신). */
interface SavePlaylistPort {
    fun save(playlist: Playlist): Playlist
}

/** 출력 포트: 플레이리스트 조회. */
interface LoadPlaylistPort {
    fun findById(id: Long): Playlist?

    /** 소유자의 플레이리스트 목록(생성 순). */
    fun findByOwner(ownerId: Long): List<Playlist>
}

/** 출력 포트: 플레이리스트 삭제. */
interface DeletePlaylistPort {
    fun delete(id: Long)
}
