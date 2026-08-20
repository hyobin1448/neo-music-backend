package com.hyobin.neomusic.playlist.adapter.outbound.persistence

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OrderColumn
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.time.Instant

/**
 * 플레이리스트 JPA 엔티티.
 *
 * 곡 목록은 @ElementCollection + @OrderColumn 으로 저장해 '순서'를 보존한다.
 * (@OrderColumn 이 position 컬럼에 인덱스를 저장 → 조회 시 그 순서로 복원)
 */
@Entity
@Table(name = "playlist")
class PlaylistJpaEntity(
    @Column(name = "owner_id", nullable = false)
    var ownerId: Long,

    @Column(nullable = false, length = 50)
    var name: String,

    @ElementCollection
    @CollectionTable(
        name = "playlist_song",
        joinColumns = [JoinColumn(name = "playlist_id")],
    )
    @OrderColumn(name = "position")
    @Column(name = "song_id", nullable = false)
    @Fetch(FetchMode.SUBSELECT)   // 소유자 목록 조회 시 곡 목록을 한 번의 서브셀렉트로 로드 (N+1 방지)
    var songIds: MutableList<String> = mutableListOf(),
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()

    @PrePersist
    fun onCreate() {
        val now = Instant.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }
}
