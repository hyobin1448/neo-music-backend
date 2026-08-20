package com.hyobin.neomusic.catalog.adapter.outbound.persistence

import com.hyobin.neomusic.catalog.domain.LyricType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OrderColumn
import jakarta.persistence.Table
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode

/**
 * DB 테이블용 가사 엔티티.
 * 가사 이미지 키 목록은 별도 테이블(lyric_image)에 순서를 보존해 저장한다(@ElementCollection).
 */
@Entity
@Table(name = "lyric")
class LyricJpaEntity(
    @Column(nullable = false, length = 8)
    var lang: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var type: LyricType,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ElementCollection
    @CollectionTable(name = "lyric_image", joinColumns = [JoinColumn(name = "lyric_id")])
    @OrderColumn(name = "idx")
    @Column(name = "image_key", nullable = false)
    @Fetch(FetchMode.SUBSELECT)
    var imageKeys: MutableList<String> = mutableListOf()
}
