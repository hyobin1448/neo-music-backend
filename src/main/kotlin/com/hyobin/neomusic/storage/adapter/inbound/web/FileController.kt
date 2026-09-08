package com.hyobin.neomusic.storage.adapter.inbound.web

import com.hyobin.neomusic.storage.application.port.outbound.FileStoragePort
import com.hyobin.neomusic.storage.application.port.outbound.SignedUrlPort
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
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
 *
 * 이어받기(Range) 지원: 곡 파일이 수 MB 라 느린 회선에서 다운로드가 끊기면
 * 처음부터 다시 받는 손해가 크다. 본문을 ByteArray 가 아니라 Resource 로 돌려주면
 * 스프링(HttpEntityMethodProcessor + ResourceRegionHttpMessageConverter)이
 * Accept-Ranges / 206 Partial Content / Content-Range / 416 을 알아서 처리한다.
 * 단 이 처리는 상태가 200 일 때만 걸리므로, 아래 403·404 는 Range 여부와 무관하게 그대로 나간다.
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
    ): ResponseEntity<Resource> {
        val cleanKey = key.removePrefix("/")

        // 서명 검증이 언제나 먼저다. Range 요청이라고 해서 건너뛰지 않는다.
        if (!signedUrl.verify(cleanKey, expires, sig)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()   // 위조되었거나 만료됨
        }

        val bytes = fileStorage.load(cleanKey)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok()
            .contentType(contentTypeOf(cleanKey))
            .body(ByteArrayResource(bytes))
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
