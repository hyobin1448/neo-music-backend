package com.hyobin.neomusic.auth.domain

import java.time.Instant

/** 이미 존재하는 닉네임으로 가입 시도. */
class NicknameAlreadyExistsException(nickname: Nickname) :
    RuntimeException("이미 사용 중인 닉네임입니다: ${nickname.value}")

/** 닉네임 없음 또는 비밀번호 불일치. (어느 쪽인지 구분해 알려주지 않는다 — 보안) */
class InvalidCredentialsException :
    RuntimeException("닉네임 또는 비밀번호가 올바르지 않습니다.")

/** 로그인 실패 누적으로 잠긴 계정. */
class AccountLockedException(until: Instant) :
    RuntimeException("계정이 잠겼습니다. 잠시 후 다시 시도해 주세요. (해제 예정: $until)")

/** 인증이 필요한데 토큰이 없거나 유효하지 않음. (401) */
class UnauthenticatedException :
    RuntimeException("인증이 필요합니다.")

/** 인증은 됐지만 권한이 부족함(예: 관리자 전용). (403) */
class ForbiddenException :
    RuntimeException("이 작업을 수행할 권한이 없습니다.")
