package com.hyobin.neomusic.storage.adapter.outbound.local

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 로컬 파일 저장소 설정. (application.yml 의 neomusic.storage.local.*)
 */
@ConfigurationProperties(prefix = "neomusic.storage.local")
data class LocalStorageProperties(
    /** 파일이 저장될 기준 디렉터리. */
    val baseDir: String = "./storage",
)
