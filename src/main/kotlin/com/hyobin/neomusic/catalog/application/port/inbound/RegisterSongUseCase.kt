package com.hyobin.neomusic.catalog.application.port.inbound

import com.hyobin.neomusic.catalog.domain.Song

/**
 * 입력 포트: 곡 신규 등록 (관리자용).
 * 완성된 도메인 Song을 받아 전역 버전을 올리고 저장한다.
 * 같은 id가 이미 있으면 실패한다(수정은 UpdateSongUseCase 로 분리).
 * (요청 DTO → 도메인 변환은 관리자 웹/REST 어댑터가 담당)
 */
interface RegisterSongUseCase {
    fun register(song: Song): Song
}
