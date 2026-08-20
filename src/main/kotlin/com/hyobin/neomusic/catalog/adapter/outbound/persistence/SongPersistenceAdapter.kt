package com.hyobin.neomusic.catalog.adapter.outbound.persistence

import com.hyobin.neomusic.catalog.application.port.outbound.LoadSongPort
import com.hyobin.neomusic.catalog.application.port.outbound.SaveSongPort
import com.hyobin.neomusic.catalog.domain.Song
import com.hyobin.neomusic.catalog.domain.SongId
import org.springframework.stereotype.Repository

/**
 * 영속성 어댑터: 출력 포트(Save/Load)를 JPA로 실제 구현한다.
 * application은 SaveSongPort/LoadSongPort만 알고, 이 클래스의 존재는 모른다.
 */
@Repository
class SongPersistenceAdapter(
    private val repository: SongJpaRepository,
) : SaveSongPort, LoadSongPort {

    override fun save(song: Song, version: Long): Song {
        // 기존 곡이면 불러와 갱신, 없으면 새로 생성 → 같은 id로 여러 번 저장해도 중복되지 않음(UPSERT)
        val entity = repository.findById(song.id.value).orElseGet {
            SongJpaEntity(
                id = song.id.value,
                title = song.title,
                artist = song.artist,
                coverKey = song.coverKey?.value,
                displayOrder = song.displayOrder,
                lastModifiedVersion = version,
                isDeleted = song.isDeleted,
            )
        }

        // 도메인 상태를 엔티티에 반영 (createdAt은 건드리지 않아 최초 값 유지)
        entity.title = song.title
        entity.artist = song.artist
        entity.coverKey = song.coverKey?.value
        entity.displayOrder = song.displayOrder
        entity.isDeleted = song.isDeleted
        entity.lastModifiedVersion = version

        // 자식(트랙/가사)은 통째로 교체 — orphanRemoval=true 라 빠진 자식은 삭제된다
        entity.tracks.clear()
        entity.tracks.addAll(song.tracks.map { it.toEntity() })
        entity.lyrics.clear()
        entity.lyrics.addAll(song.lyrics.map { it.toEntity() })

        return repository.save(entity).toDomain()
    }

    override fun findById(id: SongId): Song? =
        repository.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun findAll(): List<Song> =
        repository.findAll().map { it.toDomain() }

    override fun findAllActive(): List<Song> =
        repository.findByIsDeletedFalseOrderByDisplayOrderAsc().map { it.toDomain() }

    override fun findChangedSince(version: Long): List<Song> =
        repository.findByLastModifiedVersionGreaterThan(version).map { it.toDomain() }

    override fun searchActive(query: String): List<Song> =
        repository.searchActive(escapeLike(query)).map { it.toDomain() }

    /** LIKE 메타문자를 리터럴로 취급하도록 이스케이프('!'를 escape 문자로 사용). */
    private fun escapeLike(raw: String): String =
        raw.replace("!", "!!").replace("%", "!%").replace("_", "!_")
}
