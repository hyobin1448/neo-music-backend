package com.hyobin.neomusic.storage.adapter.outbound.security

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class HmacSignedUrlAdapterTest : StringSpec({

    val now = Instant.parse("2026-01-01T00:00:00Z")
    val props = SignedUrlProperties(secret = "test-secret-123", validSeconds = 600)
    val adapter = HmacSignedUrlAdapter(props, Clock.fixed(now, ZoneOffset.UTC))

    // "/files/{key}?expires=E&sig=S" 에서 expires/sig 추출
    fun parse(url: String): Pair<Long, String> {
        val query = url.substringAfter('?')
        val expires = query.substringAfter("expires=").substringBefore("&").toLong()
        val sig = query.substringAfter("sig=")
        return expires to sig
    }

    "서명한 URL은 검증을 통과한다" {
        val key = "songs/s1/ko.m4a"
        val (expires, sig) = parse(adapter.sign(key))
        adapter.verify(key, expires, sig) shouldBe true
    }

    "위조된 서명은 거부한다" {
        val key = "songs/s1/ko.m4a"
        val (expires, _) = parse(adapter.sign(key))
        adapter.verify(key, expires, "tampered-sig") shouldBe false
    }

    "다른 키로는 검증에 실패한다" {
        val (expires, sig) = parse(adapter.sign("songs/s1/ko.m4a"))
        adapter.verify("songs/other/ko.m4a", expires, sig) shouldBe false
    }

    "만료된 서명은 거부한다" {
        val key = "songs/s1/ko.m4a"
        val (expires, sig) = parse(adapter.sign(key))   // 만료 = now + 600초
        // 만료 1초 뒤 시점의 검증기
        val later = HmacSignedUrlAdapter(props, Clock.fixed(now.plusSeconds(601), ZoneOffset.UTC))
        later.verify(key, expires, sig) shouldBe false
    }
})
