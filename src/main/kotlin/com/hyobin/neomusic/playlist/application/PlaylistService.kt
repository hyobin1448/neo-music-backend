package com.hyobin.neomusic.playlist.application

import com.hyobin.neomusic.auth.domain.ForbiddenException
import com.hyobin.neomusic.playlist.application.port.inbound.PlaylistUseCase
import com.hyobin.neomusic.playlist.application.port.outbound.DeletePlaylistPort
import com.hyobin.neomusic.playlist.application.port.outbound.LoadPlaylistPort
import com.hyobin.neomusic.playlist.application.port.outbound.SavePlaylistPort
import com.hyobin.neomusic.playlist.domain.Playlist
import com.hyobin.neomusic.playlist.domain.PlaylistName
import com.hyobin.neomusic.playlist.domain.PlaylistNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 플레이리스트 유스케이스 구현.
 * 소유권 검증은 loadOwned() 한 곳에 모아, 모든 조작이 반드시 거치게 한다.
 */
@Service
class PlaylistService(
    private val savePort: SavePlaylistPort,
    private val loadPort: LoadPlaylistPort,
    private val deletePort: DeletePlaylistPort,
) : PlaylistUseCase {

    @Transactional
    override fun create(ownerId: Long, name: String): Playlist =
        savePort.save(Playlist.create(ownerId, PlaylistName.of(name)))

    @Transactional
    override fun rename(actorId: Long, playlistId: Long, newName: String): Playlist {
        val playlist = loadOwned(actorId, playlistId)
        playlist.rename(PlaylistName.of(newName))
        return savePort.save(playlist)
    }

    @Transactional
    override fun delete(actorId: Long, playlistId: Long) {
        loadOwned(actorId, playlistId)     // 존재·소유 확인 후 삭제
        deletePort.delete(playlistId)
    }

    @Transactional
    override fun addSong(actorId: Long, playlistId: Long, songId: String): Playlist {
        val playlist = loadOwned(actorId, playlistId)
        playlist.addSong(songId)
        return savePort.save(playlist)
    }

    @Transactional
    override fun removeSong(actorId: Long, playlistId: Long, songId: String): Playlist {
        val playlist = loadOwned(actorId, playlistId)
        playlist.removeSong(songId)
        return savePort.save(playlist)
    }

    @Transactional
    override fun reorder(actorId: Long, playlistId: Long, orderedSongIds: List<String>): Playlist {
        val playlist = loadOwned(actorId, playlistId)
        playlist.reorder(orderedSongIds)
        return savePort.save(playlist)
    }

    @Transactional(readOnly = true)
    override fun list(ownerId: Long): List<Playlist> = loadPort.findByOwner(ownerId)

    @Transactional(readOnly = true)
    override fun get(actorId: Long, playlistId: Long): Playlist = loadOwned(actorId, playlistId)

    /** 플레이리스트를 불러오되, 없으면 404 / 남의 것이면 403. */
    private fun loadOwned(actorId: Long, playlistId: Long): Playlist {
        val playlist = loadPort.findById(playlistId) ?: throw PlaylistNotFoundException(playlistId)
        if (!playlist.ownedBy(actorId)) throw ForbiddenException()
        return playlist
    }
}
