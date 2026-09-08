package com.hyobin.neomusic.storage.adapter.inbound.web

import com.hyobin.neomusic.storage.application.port.outbound.FileStoragePort
import com.hyobin.neomusic.storage.application.port.outbound.SignedUrlPort
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(FileController::class)
class FileControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var fileStorage: FileStoragePort

    @MockBean
    lateinit var signedUrl: SignedUrlPort

    private val key = "songs/s1/ko.m4a"

    /** 이어받기 검증용 — 구간을 잘라도 어느 바이트인지 구분되도록 위치마다 값이 다르게. */
    private val audio = ByteArray(2000) { (it % 251).toByte() }

    @Test
    fun `서명이 유효하고 파일이 있으면 200과 내용을 반환한다`() {
        given(signedUrl.verify(key, 100L, "goodsig")).willReturn(true)
        given(fileStorage.load(key)).willReturn("audio-bytes".toByteArray())

        mockMvc.get("/files/$key") {
            param("expires", "100")
            param("sig", "goodsig")
        }.andExpect {
            status { isOk() }
            content { bytes("audio-bytes".toByteArray()) }
        }
    }

    // ── 이어받기(Range) ──────────────────────────────────────────

    @Test
    fun `Range 없는 요청은 200과 전체 내용을 반환하고 이어받기 가능함을 알린다`() {
        given(signedUrl.verify(key, 100L, "goodsig")).willReturn(true)
        given(fileStorage.load(key)).willReturn(audio)

        mockMvc.get("/files/$key") {
            param("expires", "100")
            param("sig", "goodsig")
        }.andExpect {
            status { isOk() }
            header { string("Accept-Ranges", "bytes") }
            content { bytes(audio) }
        }
    }

    @Test
    fun `Range 요청은 206과 해당 구간의 바이트만 반환한다`() {
        given(signedUrl.verify(key, 100L, "goodsig")).willReturn(true)
        given(fileStorage.load(key)).willReturn(audio)

        mockMvc.get("/files/$key") {
            param("expires", "100")
            param("sig", "goodsig")
            header("Range", "bytes=0-999")
        }.andExpect {
            status { isPartialContent() }
            header { string("Content-Range", "bytes 0-999/2000") }
            header { longValue("Content-Length", 1000) }
            content { bytes(audio.copyOfRange(0, 1000)) }
        }
    }

    @Test
    fun `받다 만 지점부터의 Range 요청은 그 뒤 구간을 이어서 반환한다`() {
        given(signedUrl.verify(key, 100L, "goodsig")).willReturn(true)
        given(fileStorage.load(key)).willReturn(audio)

        // 앱이 .part 파일 크기(1500)를 보고 "그 다음부터 끝까지" 요청하는 상황
        mockMvc.get("/files/$key") {
            param("expires", "100")
            param("sig", "goodsig")
            header("Range", "bytes=1500-")
        }.andExpect {
            status { isPartialContent() }
            header { string("Content-Range", "bytes 1500-1999/2000") }
            content { bytes(audio.copyOfRange(1500, 2000)) }
        }
    }

    @Test
    fun `파일 크기를 벗어난 Range 는 416을 반환한다`() {
        given(signedUrl.verify(key, 100L, "goodsig")).willReturn(true)
        given(fileStorage.load(key)).willReturn(audio)

        mockMvc.get("/files/$key") {
            param("expires", "100")
            param("sig", "goodsig")
            header("Range", "bytes=5000-5999")
        }.andExpect {
            status { isRequestedRangeNotSatisfiable() }
            header { string("Content-Range", "bytes */2000") }
        }
    }

    @Test
    fun `Range 를 붙여도 서명이 위조되면 403을 반환한다`() {
        given(signedUrl.verify(key, 100L, "badsig")).willReturn(false)

        mockMvc.get("/files/$key") {
            param("expires", "100")
            param("sig", "badsig")
            header("Range", "bytes=0-999")
        }.andExpect {
            status { isForbidden() }   // 앱은 이 403 을 "만료 → 재발급" 신호로 쓴다
        }
    }

    @Test
    fun `서명이 위조되면 403을 반환한다`() {
        given(signedUrl.verify(key, 100L, "badsig")).willReturn(false)

        mockMvc.get("/files/$key") {
            param("expires", "100")
            param("sig", "badsig")
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `서명은 유효하지만 파일이 없으면 404를 반환한다`() {
        given(signedUrl.verify(key, 100L, "goodsig")).willReturn(true)
        given(fileStorage.load(key)).willReturn(null)

        mockMvc.get("/files/$key") {
            param("expires", "100")
            param("sig", "goodsig")
        }.andExpect {
            status { isNotFound() }
        }
    }
}
