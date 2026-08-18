package com.hyobin.neomusic.catalog.application.port.inbound

import com.hyobin.neomusic.catalog.domain.Song

/**
 * 입력 포트: 기존 곡 수정 (관리자용).
 * 대상 곡이 없으면 실패한다(생성은 RegisterSongUseCase 로 분리).
 * 전체 교체(full replace) 방식 — 넘어온 Song 상태로 통째로 갈아끼운다.
 */
interface UpdateSongUseCase {
    fun update(song: Song): Song
}
