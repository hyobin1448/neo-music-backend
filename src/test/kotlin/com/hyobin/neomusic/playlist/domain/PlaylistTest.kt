package com.hyobin.neomusic.playlist.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PlaylistTest : StringSpec({

    fun newPlaylist() = Playlist.create(ownerId = 1, name = PlaylistName.of("내 애창곡"))

    "곡을 추가하면 순서대로 쌓인다" {
        val p = newPlaylist()
        p.addSong("s1")
        p.addSong("s2")
        p.songIds shouldBe listOf("s1", "s2")
    }

    "같은 곡을 중복으로 추가하면 거부한다" {
        val p = newPlaylist()
        p.addSong("s1")
        shouldThrow<IllegalArgumentException> { p.addSong("s1") }
    }

    "곡 제거는 멱등이다(없어도 예외 없음)" {
        val p = newPlaylist()
        p.addSong("s1")
        p.removeSong("s1")
        p.removeSong("s1")   // 이미 없음 → 조용히 무시
        p.songIds shouldBe emptyList()
    }

    "순서를 재배치한다" {
        val p = newPlaylist()
        p.addSong("s1"); p.addSong("s2"); p.addSong("s3")
        p.reorder(listOf("s3", "s1", "s2"))
        p.songIds shouldBe listOf("s3", "s1", "s2")
    }

    "재배치 목록이 기존 구성과 다르면 거부한다" {
        val p = newPlaylist()
        p.addSong("s1"); p.addSong("s2")
        shouldThrow<IllegalArgumentException> { p.reorder(listOf("s1", "s9")) }   // s2 빠지고 s9 등장
        shouldThrow<IllegalArgumentException> { p.reorder(listOf("s1")) }          // 개수 불일치
    }

    "소유자 판별" {
        val p = Playlist.create(ownerId = 7, name = PlaylistName.of("x"))
        p.ownedBy(7) shouldBe true
        p.ownedBy(8) shouldBe false
    }

    "이름이 비었거나 너무 길면 거부한다" {
        shouldThrow<IllegalArgumentException> { PlaylistName.of("   ") }
        shouldThrow<IllegalArgumentException> { PlaylistName.of("a".repeat(51)) }
    }
})
