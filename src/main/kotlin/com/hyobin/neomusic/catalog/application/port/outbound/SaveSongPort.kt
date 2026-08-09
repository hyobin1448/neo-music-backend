package com.hyobin.neomusic.catalog.application.port.outbound

import com.hyobin.neomusic.catalog.domain.Song

/**
 * 출력 포트: "곡을 저장한다"는 능력을 application이 바깥(어댑터)에 요구하는 계약.
 * application은 이 인터페이스만 알고, 실제 구현(JPA/DB)은 adapter가 담당한다.
 */
interface SaveSongPort {
    /**
     * 곡을 저장한다(신규 삽입 또는 갱신 = UPSERT).
     * @param version 이 변경이 반영된 전역 카탈로그 버전 (델타 동기화용 스탬프)
     */
    fun save(song: Song, version: Long): Song
}
