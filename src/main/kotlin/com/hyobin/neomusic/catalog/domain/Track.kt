package com.hyobin.neomusic.catalog.domain

/**
 * 언어별 오디오 트랙. Song 애그리거트에 속하는 구성요소다.
 * (Track만 따로 존재하거나 수정되지 않고, 항상 소속 Song을 통해 다뤄진다)
 */
class Track(
    val lang: Lang,
    val label: String,          // 화면 표시용 이름 (예: "한국어", "English")
    val audioKey: StorageKey,   // 오디오 파일 저장 키
    val durationMs: Int,        // 재생 길이(ms)
    val bytes: Long,            // 파일 크기
    val checksum: Checksum,     // 파일 무결성 (재다운로드 스킵 판단)
) {
    init {
        require(label.isNotBlank()) { "트랙 label은 비어 있을 수 없습니다." }
        require(durationMs >= 0) { "durationMs는 0 이상이어야 합니다: $durationMs" }
        require(bytes >= 0) { "bytes는 0 이상이어야 합니다: $bytes" }
    }
}
