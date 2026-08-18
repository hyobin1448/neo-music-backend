package com.hyobin.neomusic.common.web

import com.hyobin.neomusic.auth.domain.AccountLockedException
import com.hyobin.neomusic.auth.domain.ForbiddenException
import com.hyobin.neomusic.auth.domain.InvalidCredentialsException
import com.hyobin.neomusic.auth.domain.MemberNotFoundException
import com.hyobin.neomusic.auth.domain.NicknameAlreadyExistsException
import com.hyobin.neomusic.auth.domain.UnauthenticatedException
import com.hyobin.neomusic.catalog.domain.SongAlreadyExistsException
import com.hyobin.neomusic.catalog.domain.SongNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 예외 → HTTP 상태코드 매핑을 한 곳에서 처리한다.
 * (컨트롤러는 정상 흐름에만 집중하고, 에러 표현은 여기서 일관되게)
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NicknameAlreadyExistsException::class)
    fun handleConflict(e: NicknameAlreadyExistsException): ProblemDetail =
        problem(HttpStatus.CONFLICT, e.message)

    @ExceptionHandler(SongAlreadyExistsException::class)
    fun handleSongConflict(e: SongAlreadyExistsException): ProblemDetail =
        problem(HttpStatus.CONFLICT, e.message)

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleUnauthorized(e: InvalidCredentialsException): ProblemDetail =
        problem(HttpStatus.UNAUTHORIZED, e.message)

    @ExceptionHandler(AccountLockedException::class)
    fun handleLocked(e: AccountLockedException): ProblemDetail =
        problem(HttpStatus.LOCKED, e.message)

    @ExceptionHandler(UnauthenticatedException::class)
    fun handleUnauthenticated(e: UnauthenticatedException): ProblemDetail =
        problem(HttpStatus.UNAUTHORIZED, e.message)

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbidden(e: ForbiddenException): ProblemDetail =
        problem(HttpStatus.FORBIDDEN, e.message)

    @ExceptionHandler(SongNotFoundException::class)
    fun handleNotFound(e: SongNotFoundException): ProblemDetail =
        problem(HttpStatus.NOT_FOUND, e.message)

    @ExceptionHandler(MemberNotFoundException::class)
    fun handleMemberNotFound(e: MemberNotFoundException): ProblemDetail =
        problem(HttpStatus.NOT_FOUND, e.message)

    // 도메인 값 객체 검증 실패(빈 값, 길이 등) → 400
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ProblemDetail =
        problem(HttpStatus.BAD_REQUEST, e.message)

    // @Valid 요청 바디 검증 실패 → 400
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ProblemDetail {
        val detail = e.bindingResult.fieldErrors.joinToString(", ") {
            "${it.field}: ${it.defaultMessage}"
        }
        return problem(HttpStatus.BAD_REQUEST, detail)
    }

    private fun problem(status: HttpStatus, detail: String?): ProblemDetail =
        ProblemDetail.forStatusAndDetail(status, detail ?: status.reasonPhrase)
}
