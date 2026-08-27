package com.hyobin.neomusic.catalog.application

import com.hyobin.neomusic.catalog.domain.Song

/**
 * 커서 기반으로 잘라 온 곡 묶음.
 *
 * 도메인 Song 은 동기화 버전을 갖지 않는다(동기화는 인프라 관심사라 도메인에서 뺐다).
 * 그래서 "다음 요청에 쓸 커서"는 영속성 어댑터가 이 객체에 담아 돌려준다.
 */
data class SongPage(
    val songs: List<Song>,
    /** 이 묶음 마지막 곡의 동기화 버전. 비어 있으면 null. */
    val lastVersion: Long?,
    /** 아직 남은 곡이 있는가. */
    val hasMore: Boolean,
)
