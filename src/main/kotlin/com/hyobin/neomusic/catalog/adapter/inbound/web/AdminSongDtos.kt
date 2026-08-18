package com.hyobin.neomusic.catalog.adapter.inbound.web

import com.hyobin.neomusic.catalog.domain.Checksum
import com.hyobin.neomusic.catalog.domain.Lang
import com.hyobin.neomusic.catalog.domain.Lyric
import com.hyobin.neomusic.catalog.domain.LyricType
import com.hyobin.neomusic.catalog.domain.Song
import com.hyobin.neomusic.catalog.domain.SongId
import com.hyobin.neomusic.catalog.domain.StorageKey
import com.hyobin.neomusic.catalog.domain.Track
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

/**
 * 관리자 곡 등록 요청 DTO. (HTTP/JSON → 도메인 Song 변환은 여기서)
 *
 * 기본 형식 검증(@NotBlank 등)만 여기서 막고,
 * 진짜 업무 규칙(언어 중복 금지, IMAGE 가사엔 이미지 필수 등)은 도메인 Song.create 가 강제한다.
 */
data class RegisterSongRequest(
    @field:NotBlank val id: String,
    @field:NotBlank val title: String,
    @field:NotBlank val artist: String,
    val coverKey: String? = null,
    val displayOrder: Int = 0,
    @field:Valid val tracks: List<TrackRequest> = emptyList(),
    @field:Valid val lyrics: List<LyricRequest> = emptyList(),
) {
    fun toDomain(): Song = Song.create(
        id = SongId(id),
        title = title,
        artist = artist,
        coverKey = coverKey?.let { StorageKey(it) },
        displayOrder = displayOrder,
        tracks = tracks.map { it.toDomain() },
        lyrics = lyrics.map { it.toDomain() },
    )
}

data class TrackRequest(
    @field:NotBlank val lang: String,
    @field:NotBlank val label: String,
    @field:NotBlank val audioKey: String,
    val durationMs: Int,
    val bytes: Long,
    @field:NotBlank val checksum: String,
) {
    fun toDomain(): Track = Track(
        lang = Lang.of(lang),
        label = label,
        audioKey = StorageKey(audioKey),
        durationMs = durationMs,
        bytes = bytes,
        checksum = Checksum(checksum),
    )
}

data class LyricRequest(
    @field:NotBlank val lang: String,
    @field:NotBlank val type: String,          // "IMAGE" | "TEXT"
    val imageKeys: List<String> = emptyList(),
) {
    fun toDomain(): Lyric = Lyric(
        lang = Lang.of(lang),
        type = LyricType.valueOf(type.uppercase()),   // 잘못된 값 → IllegalArgumentException → 400
        imageKeys = imageKeys.map { StorageKey(it) },
    )
}
