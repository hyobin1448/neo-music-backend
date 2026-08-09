package com.hyobin.neomusic.catalog.domain

/**
 * 곡 식별자 값 객체(Value Object).
 *
 * 원시 String을 그대로 쓰지 않고 타입으로 감싼다.
 * - 함수 시그니처에서 "이건 그냥 문자열이 아니라 곡 ID"임이 드러난다 (실수로 다른 문자열을 넘기는 걸 컴파일러가 막아줌)
 * - 잘못된 값(빈 문자열)이 생성 시점에 걸러진다 → 이후 모든 코드는 유효한 값만 다룬다
 *
 * @JvmInline value class = 런타임엔 그냥 String으로 취급돼 성능 부담이 없다 (박싱 최소화)
 */
@JvmInline
value class SongId(val value: String) {
    init {
        require(value.isNotBlank()) { "SongId는 비어 있을 수 없습니다." }
    }
}
