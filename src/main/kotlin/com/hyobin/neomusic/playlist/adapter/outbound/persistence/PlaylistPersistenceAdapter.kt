package com.hyobin.neomusic.playlist.adapter.outbound.persistence

import com.hyobin.neomusic.playlist.application.port.outbound.DeletePlaylistPort
import com.hyobin.neomusic.playlist.application.port.outbound.LoadPlaylistPort
import com.hyobin.neomusic.playlist.application.port.outbound.SavePlaylistPort
import com.hyobin.neomusic.playlist.domain.Playlist
import org.springframework.stereotype.Repository

/**
 * 영속성 어댑터: 플레이리스트 저장/조회/삭제를 JPA로 구현한다.
 */
@Repository
class PlaylistPersistenceAdapter(
    private val repository: PlaylistJpaRepository,
) : SavePlaylistPort, LoadPlaylistPort, DeletePlaylistPort {

    override fun save(playlist: Playlist): Playlist {
        // 기존이면 불러와 갱신(createdAt 유지), 없으면 새로 생성
        val entity = playlist.id?.let { repository.findById(it).orElse(null) }
            ?: PlaylistJpaEntity(ownerId = playlist.ownerId, name = playlist.name.value)

        entity.ownerId = playlist.ownerId
        entity.name = playlist.name.value
        entity.songIds = playlist.songIds.toMutableList()   // 순서 그대로 통째 교체

        return repository.save(entity).toDomain()
    }

    override fun findById(id: Long): Playlist? =
        repository.findById(id).map { it.toDomain() }.orElse(null)

    override fun findByOwner(ownerId: Long): List<Playlist> =
        repository.findByOwnerIdOrderByIdAsc(ownerId).map { it.toDomain() }

    override fun delete(id: Long) {
        repository.deleteById(id)
    }
}
