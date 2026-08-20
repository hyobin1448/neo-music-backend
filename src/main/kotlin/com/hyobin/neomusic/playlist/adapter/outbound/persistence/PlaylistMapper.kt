package com.hyobin.neomusic.playlist.adapter.outbound.persistence

import com.hyobin.neomusic.playlist.domain.Playlist
import com.hyobin.neomusic.playlist.domain.PlaylistName

fun PlaylistJpaEntity.toDomain(): Playlist = Playlist.reconstitute(
    id = id!!,
    ownerId = ownerId,
    name = PlaylistName.of(name),
    songIds = songIds.toList(),
)
