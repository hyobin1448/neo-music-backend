package com.hyobin.neomusic.catalog.adapter.inbound.web

import com.hyobin.neomusic.catalog.application.CatalogSnapshot
import com.hyobin.neomusic.catalog.domain.Lyric
import com.hyobin.neomusic.catalog.domain.Song
import com.hyobin.neomusic.catalog.domain.Track

/**
 * 카탈로그 동기화 응답 DTO (HTTP/JSON 표현).
 *
 * 참고: 지금은 파일을 '저장 키'로 그대로 노출한다.
 * StoragePort를 붙이는 단계에서 이 키들을 signed URL로 바꿔 내려주게 된다.
 */
data class CatalogResponse(
    val version: Long,
    val changed: List<SongResponse>,
    val deleted: List<String>,
) {
    companion object {
        fun from(snapshot: CatalogSnapshot): CatalogResponse = CatalogResponse(
            version = snapshot.version,
            changed = snapshot.changed.map { SongResponse.from(it) },
            deleted = snapshot.deleted.map { it.value },
        )
    }
}

data class SongResponse(
    val id: String,
    val title: String,
    val artist: String,
    val coverKey: String?,
    val displayOrder: Int,
    val tracks: List<TrackResponse>,
    val lyrics: List<LyricResponse>,
) {
    companion object {
        fun from(song: Song): SongResponse = SongResponse(
            id = song.id.value,
            title = song.title,
            artist = song.artist,
            coverKey = song.coverKey?.value,
            displayOrder = song.displayOrder,
            tracks = song.tracks.map { TrackResponse.from(it) },
            lyrics = song.lyrics.map { LyricResponse.from(it) },
        )
    }
}

data class TrackResponse(
    val lang: String,
    val label: String,
    val audioKey: String,
    val durationMs: Int,
    val bytes: Long,
    val checksum: String,
) {
    companion object {
        fun from(track: Track): TrackResponse = TrackResponse(
            lang = track.lang.code,
            label = track.label,
            audioKey = track.audioKey.value,
            durationMs = track.durationMs,
            bytes = track.bytes,
            checksum = track.checksum.value,
        )
    }
}

data class LyricResponse(
    val lang: String,
    val type: String,
    val imageKeys: List<String>,
) {
    companion object {
        fun from(lyric: Lyric): LyricResponse = LyricResponse(
            lang = lyric.lang.code,
            type = lyric.type.name,
            imageKeys = lyric.imageKeys.map { it.value },
        )
    }
}
