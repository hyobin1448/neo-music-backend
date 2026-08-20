package com.hyobin.neomusic.storage.adapter.outbound.security

import com.hyobin.neomusic.storage.application.port.outbound.SignedUrlPort
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.time.Clock
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 출력 어댑터: HMAC-SHA256 기반 서명 URL 구현.
 *
 * 서명 대상 = "키:만료시각". 서버 비밀키로 HMAC을 만들어 sig 로 붙인다.
 * 시각은 주입된 Clock 에서 얻어 테스트가 쉽다.
 */
@Component
class HmacSignedUrlAdapter(
    private val properties: SignedUrlProperties,
    private val clock: Clock,
) : SignedUrlPort {

    override fun sign(key: String): String {
        val expires = clock.instant().epochSecond + properties.validSeconds
        val sig = hmac(key, expires)
        return "/files/$key?expires=$expires&sig=$sig"
    }

    override fun verify(key: String, expires: Long, sig: String): Boolean {
        if (clock.instant().epochSecond > expires) return false        // 만료
        val expected = hmac(key, expires)
        // 타이밍 공격 방지를 위한 상수 시간 비교
        return MessageDigest.isEqual(expected.toByteArray(), sig.toByteArray())
    }

    private fun hmac(key: String, expires: Long): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(properties.secret.toByteArray(), "HmacSHA256"))
        val raw = mac.doFinal("$key:$expires".toByteArray())
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw)
    }
}
