package com.hyobin.neomusic

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan   // @ConfigurationProperties 클래스(AdminProperties 등) 자동 등록
class NeoMusicApplication

fun main(args: Array<String>) {
    runApplication<NeoMusicApplication>(*args)
}
