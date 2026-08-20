package com.hyobin.neomusic.storage.adapter.outbound.local

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files

class LocalFileStorageAdapterTest : StringSpec({

    // 테스트용 임시 기준 디렉터리
    val tempBase = Files.createTempDirectory("neomusic-storage-test")
    val adapter = LocalFileStorageAdapter(LocalStorageProperties(baseDir = tempBase.toString()))

    "저장한 파일을 그대로 읽어온다" {
        val content = "hello-audio".toByteArray()
        adapter.store("songs/song_001/ko.m4a", content)

        adapter.load("songs/song_001/ko.m4a") shouldBe content
        adapter.exists("songs/song_001/ko.m4a") shouldBe true
    }

    "없는 파일을 읽으면 null 이다" {
        adapter.load("nope/missing.m4a") shouldBe null
        adapter.exists("nope/missing.m4a") shouldBe false
    }

    "같은 키로 저장하면 덮어쓴다" {
        adapter.store("cover.png", "old".toByteArray())
        adapter.store("cover.png", "new".toByteArray())
        adapter.load("cover.png") shouldBe "new".toByteArray()
    }

    "삭제하면 더 이상 존재하지 않는다" {
        adapter.store("temp.txt", "x".toByteArray())
        adapter.delete("temp.txt")
        adapter.exists("temp.txt") shouldBe false
    }

    "경로 탈출(../) 키는 거부한다" {
        shouldThrow<IllegalArgumentException> {
            adapter.store("../escape.txt", "x".toByteArray())
        }
    }
})
