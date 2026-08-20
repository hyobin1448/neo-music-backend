package com.hyobin.neomusic.storage.adapter.inbound.web

import com.hyobin.neomusic.auth.application.AuthenticatedUser
import com.hyobin.neomusic.storage.application.port.inbound.StoreFileCommand
import com.hyobin.neomusic.storage.application.port.inbound.StoreFileUseCase
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

/**
 * 관리자용 파일 업로드.
 *
 * POST /admin/files (multipart)  — 관리자 전용(401/403)
 *   file   : 업로드할 파일
 *   prefix : 키 분류 (songs/covers/lyrics 등, 기본 misc)
 * 반환: 생성된 저장 key 와 checksum → 곡 등록 요청에 그대로 넣어 쓴다.
 */
@RestController
@RequestMapping("/admin/files")
class AdminFileController(
    private val storeFileUseCase: StoreFileUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun upload(
        user: AuthenticatedUser,
        @RequestParam file: MultipartFile,
        @RequestParam(defaultValue = "misc") prefix: String,
    ): FileUploadResponse {
        user.requireAdmin()
        require(!file.isEmpty) { "빈 파일은 업로드할 수 없습니다." }

        val result = storeFileUseCase.store(
            StoreFileCommand(
                prefix = prefix,
                originalFilename = file.originalFilename,
                content = file.bytes,
                contentType = file.contentType,
            ),
        )
        return FileUploadResponse(result.key, result.checksum, result.bytes, result.contentType)
    }
}

data class FileUploadResponse(
    val key: String,
    val checksum: String,
    val bytes: Long,
    val contentType: String?,
)
