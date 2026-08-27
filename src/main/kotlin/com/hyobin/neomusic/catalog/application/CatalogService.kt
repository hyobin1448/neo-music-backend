package com.hyobin.neomusic.catalog.application

import com.hyobin.neomusic.catalog.application.port.inbound.DeleteSongUseCase
import com.hyobin.neomusic.catalog.application.port.inbound.GetCatalogUseCase
import com.hyobin.neomusic.catalog.application.port.inbound.RegisterSongUseCase
import com.hyobin.neomusic.catalog.application.port.inbound.SearchSongsUseCase
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
) : RegisterSongUseCase, UpdateSongUseCase, DeleteSongUseCase, GetCatalogUseCase, SearchSongsUseCase {

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

    /** 곡 검색. 검색어가 비면 조회하지 않고 빈 결과. */
    @Transactional(readOnly = true)
    override fun search(query: String): List<Song> {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return emptyList()
        return loadPort.searchActive(trimmed)
    }

    /**
     * 카탈로그 동기화 조회 (커서 기반, 한 번에 limit 건까지).
     *
     * 곡이 늘어도 한 응답이 무한정 커지지 않도록 자른다. 남은 게 있으면 hasMore=true 와
     * 함께 nextSince 를 내려주고, 클라는 그 값으로 다시 요청해 이어받는다.
     */
    @Transactional(readOnly = true)
    override fun getCatalog(since: Long?, limit: Int): CatalogSnapshot {
        val capped = limit.coerceIn(1, GetCatalogUseCase.MAX_LIMIT)
        val currentVersion = versionPort.current()

        // 첫 동기화: 살아있는 곡만 (삭제 곡은 애초에 보낼 필요 없음)
        // 델타: since 이후 바뀐 곡을 '변경'과 '삭제'로 가른다
        val page = if (since == null) {
            loadPort.findActivePage(afterVersion = 0, limit = capped)
        } else {
            loadPort.findChangedPage(afterVersion = since, limit = capped)
        }

        return CatalogSnapshot(
            version = currentVersion,
            changed = page.songs.filterNot { it.isDeleted },
            deleted = page.songs.filter { it.isDeleted }.map { it.id },
            hasMore = page.hasMore,
            // 덜 받았으면 이어받을 지점(이 페이지 마지막 곡의 버전)을, 다 받았으면 현재 버전을 준다.
            nextSince = if (page.hasMore) page.lastVersion ?: currentVersion else currentVersion,
        )
    }
}
