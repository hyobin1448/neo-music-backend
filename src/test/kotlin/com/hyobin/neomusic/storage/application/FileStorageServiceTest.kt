package com.hyobin.neomusic.storage.application

import com.hyobin.neomusic.storage.application.port.inbound.StoreFileCommand
import com.hyobin.neomusic.storage.application.port.outbound.FileStoragePort
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import io.mockk.mockk
import io.mockk.verify
import java.security.MessageDigest

class FileStorageServiceTest : StringSpec({

    val port = mockk<FileStoragePort>(relaxed = true)
    val service = FileStorageService(port)

    "업로드하면 prefix/uuid.ext 키를 만들고 체크섬을 계산해 저장한다" {
        val content = "hello-audio".toByteArray()

        val result = service.store(StoreFileCommand("songs", "ko.M4A", content, "audio/mp4"))

        result.key shouldStartWith "songs/"
        result.key shouldEndWith ".m4a"                       // 확장자는 소문자로
        result.bytes shouldBe content.size.toLong()

        val expectedHex = MessageDigest.getInstance("SHA-256").digest(content)
            .joinToString("") { "%02x".format(it) }
        result.checksum shouldBe "sha256:$expectedHex"

        verify { port.store(result.key, content) }            // 생성된 키로 저장했는가
    }

    "prefix 가 비면 misc 로, 확장자 없으면 확장자 없이 저장한다" {
        val result = service.store(StoreFileCommand("  ", null, "x".toByteArray(), null))
        result.key shouldStartWith "misc/"
    }
})
