package com.hyobin.neomusic.catalog.domain

/**
 * 곡 — 애그리거트 루트(Aggregate Root).
 *
 * Track/Lyric은 Song을 통해서만 추가·조회된다. Song이 "한 곡의 규칙"을 책임진다.
 * 예: 한 곡에 같은 언어의 오디오가 둘일 수 없다 (애그리거트 불변식).
 *
 * 프레임워크(Spring/JPA)에 전혀 의존하지 않는 순수 Kotlin 클래스다.
 */
class Song private constructor(
    val id: SongId,
    title: String,
    artist: String,
    coverKey: StorageKey?,
    displayOrder: Int,
    tracks: List<Track>,
    lyrics: List<Lyric>,
    isDeleted: Boolean,
) {
    var title: String = title
        private set
    var artist: String = artist
        private set
    var coverKey: StorageKey? = coverKey
        private set
    var displayOrder: Int = displayOrder
        private set
    var isDeleted: Boolean = isDeleted
        private set

    // 내부 목록은 감추고(캡슐화), 바깥엔 읽기 전용 복사본만 노출한다.
    private val _tracks: MutableList<Track> = tracks.toMutableList()
    private val _lyrics: MutableList<Lyric> = lyrics.toMutableList()
    val tracks: List<Track> get() = _tracks.toList()
    val lyrics: List<Lyric> get() = _lyrics.toList()

    /** 오디오 추가. 같은 언어가 이미 있으면 거부(불변식). */
    fun addTrack(track: Track) {
        require(_tracks.none { it.lang == track.lang }) {
            "이미 '${track.lang.code}' 언어의 오디오가 있습니다."
        }
        _tracks.add(track)
    }

    /** 가사 추가. 같은 언어가 이미 있으면 거부. */
    fun addLyric(lyric: Lyric) {
        require(_lyrics.none { it.lang == lyric.lang }) {
            "이미 '${lyric.lang.code}' 언어의 가사가 있습니다."
        }
        _lyrics.add(lyric)
    }

    fun rename(title: String, artist: String) {
        require(title.isNotBlank()) { "제목은 비어 있을 수 없습니다." }
        require(artist.isNotBlank()) { "아티스트는 비어 있을 수 없습니다." }
        this.title = title
        this.artist = artist
    }

    fun changeOrder(order: Int) {
        this.displayOrder = order
    }

    fun changeCover(coverKey: StorageKey?) {
        this.coverKey = coverKey
    }

    /** 소프트 삭제(tombstone). 물리 삭제 대신 표시만 남겨 앱에 '삭제됨'을 전파한다. */
    fun markDeleted() {
        this.isDeleted = true
    }

    companion object {
        /** 신규 곡 생성. 삭제 안 됨 상태로 시작. */
        fun create(
            id: SongId,
            title: String,
            artist: String,
            coverKey: StorageKey? = null,
            displayOrder: Int = 0,
            tracks: List<Track> = emptyList(),
            lyrics: List<Lyric> = emptyList(),
        ): Song {
            require(title.isNotBlank()) { "제목은 비어 있을 수 없습니다." }
            require(artist.isNotBlank()) { "아티스트는 비어 있을 수 없습니다." }
            requireNoDuplicateLang(tracks.map { it.lang }, "오디오")
            requireNoDuplicateLang(lyrics.map { it.lang }, "가사")
            return Song(id, title, artist, coverKey, displayOrder, tracks, lyrics, isDeleted = false)
        }

        /** 저장소에서 읽은 데이터를 도메인 객체로 복원할 때 사용(검증 없이 그대로 재구성). */
        fun reconstitute(
            id: SongId,
            title: String,
            artist: String,
            coverKey: StorageKey?,
            displayOrder: Int,
            tracks: List<Track>,
            lyrics: List<Lyric>,
            isDeleted: Boolean,
        ): Song = Song(id, title, artist, coverKey, displayOrder, tracks, lyrics, isDeleted)

        private fun requireNoDuplicateLang(langs: List<Lang>, what: String) {
            require(langs.size == langs.toSet().size) { "$what 언어가 중복됩니다: $langs" }
        }
    }
}
