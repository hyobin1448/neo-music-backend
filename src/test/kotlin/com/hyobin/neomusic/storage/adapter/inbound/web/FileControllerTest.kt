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
