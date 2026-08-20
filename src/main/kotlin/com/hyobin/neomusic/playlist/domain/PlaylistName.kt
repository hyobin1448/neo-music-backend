package com.hyobin.neomusic.playlist.domain

/**
 * 플레이리스트 이름 값 객체.
 * 공백 정규화 + 길이 제한을 생성 시점에 강제한다.
 */
@JvmInline
value class PlaylistName private constructor(val value: String) {
    companion object {
        const val MAX_LENGTH = 50

        fun of(raw: String): PlaylistName {
            val trimmed = raw.trim()
            require(trimmed.isNotBlank()) { "플레이리스트 이름은 비어 있을 수 없습니다." }
            require(trimmed.length <= MAX_LENGTH) { "플레이리스트 이름은 ${MAX_LENGTH}자 이하여야 합니다." }
            return PlaylistName(trimmed)
        }
    }
}
