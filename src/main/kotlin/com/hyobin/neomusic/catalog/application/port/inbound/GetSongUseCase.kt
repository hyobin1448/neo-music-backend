package com.hyobin.neomusic.catalog.application.port.inbound

import com.hyobin.neomusic.catalog.domain.Song
import com.hyobin.neomusic.catalog.domain.SongId

/**
 * 입력 포트: 곡 하나 조회.
 *
 * 존재 이유는 '조회'보다 '서명 URL 재발급'에 가깝다.
 * 카탈로그 응답의 파일 URL 은 발급 시점부터 짧게만(기본 600초) 유효해서,
 * 첫 실행 전체 다운로드가 길어지면 뒤쪽 곡의 URL 이 이미 만료돼 있다.
 * 이때 곡 하나만 다시 조립하면 서명이 새로 찍힌다 — 카탈로그 페이지를 통째로 다시 받을 필요가 없다.
 *
 * 삭제된 곡은 없는 곡과 똑같이 취급한다(파일을 다시 받을 이유가 없으므로).
 */
interface GetSongUseCase {
    /** @throws com.hyobin.neomusic.catalog.domain.SongNotFoundException 없거나 삭제된 곡일 때. */
    fun getSong(id: SongId): Song
}
