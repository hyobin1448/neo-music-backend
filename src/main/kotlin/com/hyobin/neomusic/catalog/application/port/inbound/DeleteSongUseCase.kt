package com.hyobin.neomusic.catalog.application.port.inbound

import com.hyobin.neomusic.catalog.domain.SongId

/**
 * 입력 포트: 곡 삭제 (관리자용).
 * 물리 삭제가 아니라 소프트 삭제(tombstone) → 앱에도 삭제가 전파된다.
 */
interface DeleteSongUseCase {
    fun delete(id: SongId)
}
