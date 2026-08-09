package com.hyobin.neomusic.catalog.adapter.outbound.persistence

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.Instant

/**
 * DB 테이블용 곡 엔티티.
 * 도메인 Song과 필드가 비슷하지만 일부러 분리한다:
 * - 여기엔 저장 관심사(lastModifiedVersion, createdAt/updatedAt)가 들어간다
 * - 도메인은 이런 저장/동기화 사정을 몰라도 된다
 */
@Entity
@Table(name = "song")
class SongJpaEntity(
    @Id
    @Column(length = 64)
    val id: String,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    var artist: String,

    @Column(name = "cover_key")
    var coverKey: String?,

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int,

    @Column(name = "last_modified_version", nullable = false)
    var lastModifiedVersion: Long,

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean,
) {
    // 자식들은 곡과 생명주기를 함께한다 (곡 저장 시 같이 저장, 곡에서 빠지면 삭제)
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "song_id")
    var tracks: MutableList<TrackJpaEntity> = mutableListOf()

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "song_id")
    var lyrics: MutableList<LyricJpaEntity> = mutableListOf()

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
