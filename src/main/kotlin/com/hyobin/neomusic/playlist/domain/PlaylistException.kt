package com.hyobin.neomusic.playlist.domain

/** 존재하지 않는 플레이리스트에 접근·변경하려 할 때. (404) */
class PlaylistNotFoundException(id: Long) :
    RuntimeException("플레이리스트를 찾을 수 없습니다: $id")
