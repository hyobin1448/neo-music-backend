package com.hyobin.neomusic.catalog.application

import com.hyobin.neomusic.catalog.domain.Song
import com.hyobin.neomusic.catalog.domain.SongId

/**
 * 카탈로그 동기화 결과 (한 페이지).
 *
 * @param version   응답 시점의 전역 버전 (참고값)
 * @param changed   신규·변경된 곡 (삭제 아님)
 * @param deleted   삭제된 곡 id 목록 (tombstone 전파)
 * @param hasMore   아직 못 받은 변경분이 남아 있는가
 * @param nextSince **클라가 다음 요청에 보내야 할 값.**
 *                  덜 받았으면 이 페이지 마지막 곡의 버전, 다 받았으면 현재 전역 버전.
 *                  `version` 을 그대로 다음 since 로 쓰면 안 된다 — 아직 안 받은
 *                  페이지를 통째로 건너뛰게 된다.
 */
data class CatalogSnapshot(
    val version: Long,
    val changed: List<Song>,
    val deleted: List<SongId>,
    val hasMore: Boolean,
    val nextSince: Long,
)
