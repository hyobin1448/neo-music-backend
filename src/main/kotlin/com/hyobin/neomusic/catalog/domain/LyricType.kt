package com.hyobin.neomusic.catalog.domain

/**
 * 가사 표현 방식.
 * - IMAGE: 1차. 가사를 이미지로 제공
 * - TEXT : 2차. 줄 단위 텍스트(추후 기타 코드/타임스탬프까지 확장)
 */
enum class LyricType {
    IMAGE,
    TEXT,
}
