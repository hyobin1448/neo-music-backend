package com.hyobin.neomusic.storage.application

import com.hyobin.neomusic.storage.application.port.inbound.StoreFileCommand
import com.hyobin.neomusic.storage.application.port.inbound.StoreFileUseCase
import com.hyobin.neomusic.storage.application.port.inbound.StoredFileResult
import com.hyobin.neomusic.storage.application.port.outbound.FileStoragePort
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.util.UUID

/**
 * 파일 업로드 유스케이스 구현.
 * 키를 "{prefix}/{uuid}.{ext}" 로 생성해 충돌을 피하고,
 * 무결성 확인용 sha256 체크섬을 계산한 뒤 저장소에 넘긴다.
 */
@Service
class FileStorageService(
    private val fileStorage: FileStoragePort,
) : StoreFileUseCase {

    override fun store(command: StoreFileCommand): StoredFileResult {
        val prefix = command.prefix.trim().trim('/').ifBlank { "misc" }
        val ext = command.originalFilename
            ?.substringAfterLast('.', "")
            ?.takeIf { it.isNotBlank() }
        val key = buildString {
            append(prefix).append('/').append(UUID.randomUUID())
            if (ext != null) append('.').append(ext.lowercase())
        }

        fileStorage.store(key, command.content)

        return StoredFileResult(
            key = key,
            checksum = "sha256:" + sha256Hex(command.content),
            bytes = command.content.size.toLong(),
            contentType = command.contentType,
        )
    }

    private fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it) }
}
