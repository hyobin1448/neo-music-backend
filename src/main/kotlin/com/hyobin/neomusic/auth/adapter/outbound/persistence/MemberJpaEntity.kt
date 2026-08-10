package com.hyobin.neomusic.auth.adapter.outbound.persistence

import com.hyobin.neomusic.auth.domain.Role
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = "member",
    uniqueConstraints = [UniqueConstraint(name = "uk_member_nickname", columnNames = ["nickname"])],
)
class MemberJpaEntity(
    @Column(nullable = false, length = 20)
    var nickname: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var role: Role,

    @Column(name = "failed_attempts", nullable = false)
    var failedAttempts: Int,

    @Column(name = "locked_until")
    var lockedUntil: Instant?,
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
