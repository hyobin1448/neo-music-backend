package com.hyobin.neomusic.storage.application.port.inbound

/**
 * 입력 포트: 파일을 저장소에 올리고, 곡 등록에 쓸 키·체크섬을 돌려준다.
 * (관리자가 업로드 → 반환된 key/checksum 을 곡 등록 요청에 넣는 흐름)
 */
interface StoreFileUseCase {
    fun store(command: StoreFileCommand): StoredFileResult
}

class StoreFileCommand(
    val prefix: String,               // 키 앞부분 분류 (예: "songs", "covers", "lyrics")
    val originalFilename: String?,    // 확장자 추출용
    val content: ByteArray,
    val contentType: String?,
)

data class StoredFileResult(
    val key: String,
    val checksum: String,             // "sha256:<hex>"
    val bytes: Long,
    val contentType: String?,
)
