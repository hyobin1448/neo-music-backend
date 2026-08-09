package com.hyobin.neomusic.catalog.adapter.outbound.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * DB 테이블용 오디오 트랙 엔티티. (도메인 Track과 별개 — 이건 순전히 저장 목적)
 * song_id 외래키는 부모(SongJpaEntity)의 @OneToMany + @JoinColumn이 만들어 준다.
 */
@Entity
@Table(name = "track")
class TrackJpaEntity(
    @Column(nullable = false, length = 8)
    var lang: String,

    @Column(nullable = false)
    var label: String,

    @Column(name = "audio_key", nullable = false)
    var audioKey: String,

    @Column(name = "duration_ms", nullable = false)
    var durationMs: Int,

    @Column(nullable = false)
    var bytes: Long,

    @Column(nullable = false)
    var checksum: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}
