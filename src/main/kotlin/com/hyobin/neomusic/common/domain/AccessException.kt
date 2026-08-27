package com.hyobin.neomusic.common.domain

/**
 * 인증·인가 실패는 특정 바운디드 컨텍스트의 개념이 아니라 모든 컨텍스트에 걸치는 횡단 관심사다.
 * auth 안에 두면 playlist·catalog가 남의 도메인을 직접 참조하게 되므로 공용 위치에 둔다.
 * (ArchitectureTest 의 "컨텍스트는 다른 컨텍스트의 domain 을 참조하지 않는다" 규칙과 연결)
 */

/** 인증이 필요한데 토큰이 없거나 유효하지 않음. (401) */
class UnauthenticatedException :
    RuntimeException("인증이 필요합니다.")

/** 인증은 됐지만 권한이 부족함(예: 관리자 전용, 소유자 아님). (403) */
class ForbiddenException :
    RuntimeException("이 작업을 수행할 권한이 없습니다.")
