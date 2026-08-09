package com.hyobin.neomusic.catalog.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

/**
 * Song 애그리거트의 도메인 규칙(불변식)을 검증한다.
 * 프레임워크 없이 순수 객체만 테스트하므로 아주 빠르게 돌아간다.
 */
class SongTest : StringSpec({

    fun track(lang: String) = Track(
        lang = Lang.of(lang),
        label = lang.uppercase(),
        audioKey = StorageKey("songs/s1/$lang.m4a"),
        durationMs = 200_000,
        bytes = 3_000_000,
        checksum = Checksum("sha256:abc"),
    )

    "곡을 생성하면 삭제 상태가 아니다" {
        val song = Song.create(SongId("song_001"), "제목", "아티스트")
        song.isDeleted shouldBe false
    }

    "제목이 비어 있으면 곡을 생성할 수 없다" {
        shouldThrow<IllegalArgumentException> {
            Song.create(SongId("song_001"), "  ", "아티스트")
        }
    }

    "같은 언어의 오디오를 두 번 추가할 수 없다" {
        val song = Song.create(SongId("song_001"), "제목", "아티스트")
        song.addTrack(track("ko"))
        shouldThrow<IllegalArgumentException> {
            song.addTrack(track("ko"))
        }
    }

    "다른 언어의 오디오는 여러 개 추가할 수 있다" {
        val song = Song.create(SongId("song_001"), "제목", "아티스트")
        song.addTrack(track("ko"))
        song.addTrack(track("en"))
        song.tracks shouldHaveSize 2
    }

    "생성 시 오디오 언어가 중복되면 실패한다" {
        shouldThrow<IllegalArgumentException> {
            Song.create(
                SongId("song_001"), "제목", "아티스트",
                tracks = listOf(track("ko"), track("ko")),
            )
        }
    }

    "IMAGE 가사는 이미지가 최소 한 장 있어야 한다" {
        shouldThrow<IllegalArgumentException> {
            Lyric(Lang.of("ko"), LyricType.IMAGE, imageKeys = emptyList())
        }
    }

    "markDeleted를 호출하면 삭제 표시가 된다" {
        val song = Song.create(SongId("song_001"), "제목", "아티스트")
        song.markDeleted()
        song.isDeleted shouldBe true
    }

    "언어 코드는 대소문자 무관하게 같은 값이다" {
        Lang.of("KO") shouldBe Lang.of("ko")
    }
})
