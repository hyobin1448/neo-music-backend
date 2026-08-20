package com.hyobin.neomusic.catalog.adapter.inbound.web

import com.hyobin.neomusic.catalog.application.CatalogSnapshot
import com.hyobin.neomusic.catalog.domain.Song
import com.hyobin.neomusic.catalog.domain.StorageKey
import com.hyobin.neomusic.storage.application.port.outbound.SignedUrlPort
import org.springframework.stereotype.Component

/**
 * 도메인(Song) → 응답 DTO 변환기.
 *
 * 저장 키를 그대로 노출하지 않고 SignedUrlPort 로 '서명된 다운로드 URL'로 바꾼다.
 * 정적 매퍼 대신 컴포넌트로 둔 이유 = 서명(비밀키·시각)이 필요해서다.
 */
@Component
class CatalogResponseAssembler(
    private val signedUrl: SignedUrlPort,
) {
    fun toResponse(snapshot: CatalogSnapshot): CatalogResponse = CatalogResponse(
        version = snapshot.version,
        changed = snapshot.changed.map { toSongResponse(it) },
        deleted = snapshot.deleted.map { it.value },
    )

    fun toSongResponse(song: Song): SongResponse = SongResponse(
        id = song.id.value,
        title = song.title,
        artist = song.artist,
        coverUrl = urlOf(song.coverKey),
        displayOrder = song.displayOrder,
        tracks = song.tracks.map { track ->
            TrackResponse(
                lang = track.lang.code,
                label = track.label,
                audioUrl = signedUrl.sign(track.audioKey.value),
                durationMs = track.durationMs,
                bytes = track.bytes,
                checksum = track.checksum.value,
            )
        },
        lyrics = song.lyrics.map { lyric ->
            LyricResponse(
                lang = lyric.lang.code,
                type = lyric.type.name,
                imageUrls = lyric.imageKeys.map { signedUrl.sign(it.value) },
            )
        },
    )

    private fun urlOf(key: StorageKey?): String? = key?.let { signedUrl.sign(it.value) }
}
