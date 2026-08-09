package com.hyobin.neomusic.catalog.domain

/** 존재하지 않는 곡을 조회·변경하려 할 때. */
class SongNotFoundException(id: SongId) :
    RuntimeException("곡을 찾을 수 없습니다: ${id.value}")
