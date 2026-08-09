package com.hyobin.neomusic.catalog.application.port.outbound

/**
 * 출력 포트: 전역 카탈로그 버전 관리.
 */
interface CatalogVersionPort {
    /** 현재 버전을 읽는다. */
    fun current(): Long

    /** 버전을 1 증가시키고, 증가된 새 값을 반환한다. (곡 변경 시 호출) */
    fun next(): Long
}
