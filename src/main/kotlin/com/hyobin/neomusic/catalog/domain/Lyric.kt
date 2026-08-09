package com.hyobin.neomusic.catalog.domain

/**
 * 언어별 가사. Song 애그리거트의 구성요소.
 *
 * 1차(IMAGE): imageKeys에 가사 이미지 저장 키들(여러 페이지)이 들어간다.
 * 2차(TEXT) : 지금은 imageKeys가 비어 있어도 되고, 나중에 텍스트 줄/코드 필드를 추가해 확장한다.
 */
class Lyric(
    val lang: Lang,
    val type: LyricType,
    val imageKeys: List<StorageKey>,
) {
    init {
        if (type == LyricType.IMAGE) {
            require(imageKeys.isNotEmpty()) { "IMAGE 가사는 최소 한 장의 이미지가 필요합니다." }
        }
    }
}
