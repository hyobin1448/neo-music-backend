package com.hyobin.neomusic.catalog.adapter.inbound.web

/**
 * 카탈로그 동기화 응답 DTO (HTTP/JSON 표현).
 *
 * 파일은 저장 키가 아니라 '서명된 다운로드 URL'로 내려간다.
 * (조립은 CatalogResponseAssembler 가 SignedUrlPort 로 수행)
 * checksum 은 앱이 "파일이 바뀌었는지" 판단해 재다운로드를 스킵하는 데 쓴다.
 */
data class CatalogResponse(
    val version: Long,
    val changed: List<SongResponse>,
    val deleted: List<String>,
)

data class SongResponse(
    val id: String,
    val title: String,
    val artist: String,
    val coverUrl: String?,
    val displayOrder: Int,
    val tracks: List<TrackResponse>,
    val lyrics: List<LyricResponse>,
)

data class TrackResponse(
    val lang: String,
    val label: String,
    val audioUrl: String,
    val durationMs: Int,
    val bytes: Long,
    val checksum: String,
)

data class LyricResponse(
    val lang: String,
    val type: String,
    val imageUrls: List<String>,
)
