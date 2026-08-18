package com.hyobin.neomusic.catalog.domain

/** 존재하지 않는 곡을 조회·변경하려 할 때. */
class SongNotFoundException(id: SongId) :
    RuntimeException("곡을 찾을 수 없습니다: ${id.value}")

/** 이미 존재하는 id로 곡을 새로 등록하려 할 때. (수정은 PUT 을 쓸 것) */
class SongAlreadyExistsException(id: SongId) :
    RuntimeException("이미 존재하는 곡 id 입니다: ${id.value}")
