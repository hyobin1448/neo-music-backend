package com.hyobin.neomusic.playlist.domain

/**
 * 플레이리스트 — 애그리거트 루트.
 *
 * 한 회원(owner)이 여러 개를 가질 수 있고, 각 플레이리스트는 곡 id를 '순서 있게' 담는다.
 * 곡 자체(제목·오디오)는 카탈로그의 관심사라, 여기서는 곡 id(문자열)만 참조해
 * 카탈로그에 강하게 결합되지 않는다.
 *
 * 규칙:
 * - 같은 곡을 중복으로 담지 않는다.
 * - 순서 변경은 기존 곡 구성을 그대로 둔 채 순서만 바꾼다(추가·삭제 금지).
 */
class Playlist private constructor(
    val id: Long?,              // null = 아직 저장 안 됨
    val ownerId: Long,
    name: PlaylistName,
    songIds: List<String>,
) {
    var name: PlaylistName = name
        private set

    private val _songIds: MutableList<String> = songIds.toMutableList()
    val songIds: List<String> get() = _songIds.toList()

    fun rename(newName: PlaylistName) {
        name = newName
    }

    /** 맨 뒤에 곡을 추가한다. 이미 있으면 거부(중복 금지). */
    fun addSong(songId: String) {
        val id = songId.trim()
        require(id.isNotBlank()) { "곡 id는 비어 있을 수 없습니다." }
        require(_songIds.none { it == id }) { "이미 플레이리스트에 있는 곡입니다: $id" }
        _songIds.add(id)
    }

    /** 곡을 제거한다. 없으면 조용히 무시(멱등). */
    fun removeSong(songId: String) {
        _songIds.remove(songId.trim())
    }

    /** 순서를 재배치한다. 넘긴 목록은 기존 곡 구성과 '완전히 같은 집합'이어야 한다. */
    fun reorder(orderedSongIds: List<String>) {
        val incoming = orderedSongIds.map { it.trim() }
        require(incoming.size == _songIds.size && incoming.toSet() == _songIds.toSet()) {
            "재정렬 목록이 기존 곡 구성과 일치하지 않습니다."
        }
        _songIds.clear()
        _songIds.addAll(incoming)
    }

    /** 이 플레이리스트가 해당 회원 소유인가? */
    fun ownedBy(memberId: Long): Boolean = ownerId == memberId

    companion object {
        /** 신규 생성 — 빈 목록으로 시작. */
        fun create(ownerId: Long, name: PlaylistName): Playlist =
            Playlist(id = null, ownerId = ownerId, name = name, songIds = emptyList())

        /** 저장소에서 복원. */
        fun reconstitute(id: Long, ownerId: Long, name: PlaylistName, songIds: List<String>): Playlist =
            Playlist(id, ownerId, name, songIds)
    }
}
