package com.hyobin.neomusic.catalog.application

import com.hyobin.neomusic.catalog.application.port.inbound.DeleteSongUseCase
import com.hyobin.neomusic.catalog.application.port.inbound.GetCatalogUseCase
import com.hyobin.neomusic.catalog.application.port.inbound.RegisterSongUseCase
import com.hyobin.neomusic.catalog.application.port.inbound.UpdateSongUseCase
import com.hyobin.neomusic.catalog.application.port.outbound.CatalogVersionPort
import com.hyobin.neomusic.catalog.application.port.outbound.LoadSongPort
import com.hyobin.neomusic.catalog.application.port.outbound.SaveSongPort
import com.hyobin.neomusic.catalog.domain.Song
import com.hyobin.neomusic.catalog.domain.SongAlreadyExistsException
import com.hyobin.neomusic.catalog.domain.SongId
import com.hyobin.neomusic.catalog.domain.SongNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 카탈로그 유스케이스 구현. 포트들을 조합해 "무엇을 할지"를 orchestration 한다.
 * (기술 세부 = 어댑터에, 규칙 = 도메인에. 서비스는 흐름만 조율한다)
 */
@Service
class CatalogService(
    private val savePort: SaveSongPort,
    private val loadPort: LoadSongPort,
    private val versionPort: CatalogVersionPort,
) : RegisterSongUseCase, UpdateSongUseCase, DeleteSongUseCase, GetCatalogUseCase {

    /** 곡 신규 등록: 같은 id가 이미 있으면 거부. 전역 버전을 올려 스탬프해 저장. */
    @Transactional
    override fun register(song: Song): Song {
        if (loadPort.findById(song.id) != null) throw SongAlreadyExistsException(song.id)
        val version = versionPort.next()
        return savePort.save(song, version)
    }

    /** 곡 수정: 대상이 없으면 거부. 넘어온 상태로 전체 교체하고 새 버전 스탬프. */
    @Transactional
    override fun update(song: Song): Song {
        loadPort.findById(song.id) ?: throw SongNotFoundException(song.id)
        val version = versionPort.next()
        return savePort.save(song, version)
    }

    /** 곡 삭제: 소프트 삭제 표시 + 새 버전 스탬프 → 델타로 삭제가 전파됨. */
    @Transactional
    override fun delete(id: SongId) {
        val song = loadPort.findById(id) ?: throw SongNotFoundException(id)
        song.markDeleted()
        val version = versionPort.next()
        savePort.save(song, version)
    }

    /** 카탈로그 동기화 조회. */
    @Transactional(readOnly = true)
    override fun getCatalog(since: Long?): CatalogSnapshot {
        val currentVersion = versionPort.current()

        if (since == null) {
            // 첫 동기화: 살아있는 곡 전체만 (삭제 곡은 애초에 보낼 필요 없음)
            return CatalogSnapshot(
                version = currentVersion,
                changed = loadPort.findAllActive(),
                deleted = emptyList(),
            )
        }

        // 델타: since 이후 바뀐 곡들을 '변경'과 '삭제'로 가른다
        val changedOrDeleted = loadPort.findChangedSince(since)
        return CatalogSnapshot(
            version = currentVersion,
            changed = changedOrDeleted.filterNot { it.isDeleted },
            deleted = changedOrDeleted.filter { it.isDeleted }.map { it.id },
        )
    }
}
