package com.hyobin.neomusic.storage.adapter.inbound.web

import com.hyobin.neomusic.storage.application.port.outbound.FileStoragePort
import com.hyobin.neomusic.storage.application.port.outbound.SignedUrlPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 입력 어댑터: 서명 URL로 파일을 다운로드한다.
 *
 * GET /files/{key}?expires=..&sig=..
 * 서명이 유효할 때만(위조·만료 아님) 파일을 내려준다. 공개 엔드포인트지만
 * 서명 없이는 어떤 파일도 받을 수 없어, 키를 몰래 조합해 받아가는 걸 막는다.
 */
@RestController
class FileController(
    private val fileStorage: FileStoragePort,
    private val signedUrl: SignedUrlPort,
) {
    @GetMapping("/files/{*key}")
    fun download(
        @PathVariable key: String,
        @RequestParam expires: Long,
        @RequestParam sig: String,
    ): ResponseEntity<ByteArray> {
        val cleanKey = key.removePrefix("/")

        if (!signedUrl.verify(cleanKey, expires, sig)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()   // 위조되었거나 만료됨
        }

        val bytes = fileStorage.load(cleanKey)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok()
            .contentType(contentTypeOf(cleanKey))
            .body(bytes)
    }

    /** 확장자로 대략적인 Content-Type 을 정한다. */
    private fun contentTypeOf(key: String): MediaType = when (key.substringAfterLast('.', "").lowercase()) {
        "m4a", "mp4" -> MediaType.parseMediaType("audio/mp4")
        "mp3" -> MediaType.parseMediaType("audio/mpeg")
        "png" -> MediaType.IMAGE_PNG
        "jpg", "jpeg" -> MediaType.IMAGE_JPEG
        else -> MediaType.APPLICATION_OCTET_STREAM
    }
}
