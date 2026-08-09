package com.hyobin.neomusic.catalog.adapter.outbound.persistence

import com.hyobin.neomusic.catalog.domain.Checksum
import com.hyobin.neomusic.catalog.domain.Lang
import com.hyobin.neomusic.catalog.domain.Lyric
import com.hyobin.neomusic.catalog.domain.Song
import com.hyobin.neomusic.catalog.domain.SongId
import com.hyobin.neomusic.catalog.domain.StorageKey
import com.hyobin.neomusic.catalog.domain.Track

/**
 * 도메인 ↔ JPA 엔티티 변환.
 * 이 매퍼 덕분에 도메인은 JPA를, JPA는 도메인 규칙을 서로 신경 쓸 필요가 없다.
 */

// --- JPA 엔티티 → 도메인 (DB에서 읽어온 걸 도메인 객체로 복원) ---

fun SongJpaEntity.toDomain(): Song = Song.reconstitute(
    id = SongId(id),
    title = title,
    artist = artist,
    coverKey = coverKey?.let { StorageKey(it) },
    displayOrder = displayOrder,
    tracks = tracks.map { it.toDomain() },
    lyrics = lyrics.map { it.toDomain() },
    isDeleted = isDeleted,
)

fun TrackJpaEntity.toDomain(): Track = Track(
    lang = Lang.of(lang),
    label = label,
    audioKey = StorageKey(audioKey),
    durationMs = durationMs,
    bytes = bytes,
    checksum = Checksum(checksum),
)

fun LyricJpaEntity.toDomain(): Lyric = Lyric(
    lang = Lang.of(lang),
    type = type,
    imageKeys = imageKeys.map { StorageKey(it) },
)

// --- 도메인 자식 → JPA 엔티티 (저장 시 사용) ---

fun Track.toEntity(): TrackJpaEntity = TrackJpaEntity(
    lang = lang.code,
    label = label,
    audioKey = audioKey.value,
    durationMs = durationMs,
    bytes = bytes,
    checksum = checksum.value,
)

fun Lyric.toEntity(): LyricJpaEntity = LyricJpaEntity(
    lang = lang.code,
    type = type,
).also { it.imageKeys = imageKeys.map { key -> key.value }.toMutableList() }
