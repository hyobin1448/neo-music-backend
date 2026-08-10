package com.hyobin.neomusic.auth.domain

import java.time.Duration
import java.time.Instant

/**
 * 회원 애그리거트 루트.
 *
 * 비밀번호 원문은 다루지 않고 해시만 보관한다.
 * 로그인 실패 잠금(무차별 대입 방어) 규칙을 도메인이 직접 강제한다.
 * 시간은 바깥에서 주입받는다(now 파라미터) — 도메인이 시계에 의존하지 않아 테스트가 쉽다.
 */
class Member private constructor(
    val id: Long?,                 // null = 아직 저장 안 된 신규 회원
    nickname: Nickname,
    passwordHash: PasswordHash,
    val role: Role,
    failedAttempts: Int,
    lockedUntil: Instant?,
) {
    var nickname: Nickname = nickname
        private set
    var passwordHash: PasswordHash = passwordHash
        private set
    var failedAttempts: Int = failedAttempts
        private set
    var lockedUntil: Instant? = lockedUntil
        private set

    /** 지금 잠겨 있는가? */
    fun isLocked(now: Instant): Boolean =
        lockedUntil?.let { now.isBefore(it) } ?: false

    /** 로그인 실패 기록. 연속 실패가 한계에 도달하면 일정 시간 잠근다. */
    fun recordLoginFailure(now: Instant) {
        failedAttempts += 1
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            lockedUntil = now.plus(LOCK_DURATION)
            failedAttempts = 0
        }
    }

    /** 로그인 성공. 실패 카운트와 잠금을 해제한다. */
    fun recordLoginSuccess() {
        failedAttempts = 0
        lockedUntil = null
    }

    /** 비밀번호 변경(관리자 초기화 포함). 잠금도 함께 푼다. */
    fun changePassword(newHash: PasswordHash) {
        passwordHash = newHash
        failedAttempts = 0
        lockedUntil = null
    }

    companion object {
        const val MAX_FAILED_ATTEMPTS = 5
        val LOCK_DURATION: Duration = Duration.ofMinutes(15)

        /** 신규 회원 생성. 기본 권한은 USER. */
        fun create(nickname: Nickname, passwordHash: PasswordHash, role: Role = Role.USER): Member =
            Member(id = null, nickname = nickname, passwordHash = passwordHash, role = role, failedAttempts = 0, lockedUntil = null)

        /** 저장소에서 복원. */
        fun reconstitute(
            id: Long,
            nickname: Nickname,
            passwordHash: PasswordHash,
            role: Role,
            failedAttempts: Int,
            lockedUntil: Instant?,
        ): Member = Member(id, nickname, passwordHash, role, failedAttempts, lockedUntil)
    }
}
