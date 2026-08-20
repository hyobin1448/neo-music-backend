package com.hyobin.neomusic.storage.adapter.outbound.local

import com.hyobin.neomusic.storage.application.port.outbound.FileStoragePort
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * 출력 어댑터: 로컬 파일시스템에 파일을 저장하는 FileStoragePort 구현.
 *
 * 키를 기준 디렉터리 아래 상대 경로로 매핑한다.
 * "../" 같은 경로 탈출(path traversal)을 막아 기준 디렉터리 밖으로 못 나가게 한다.
 */
@Component
class LocalFileStorageAdapter(
    properties: LocalStorageProperties,
) : FileStoragePort {

    // 기준 디렉터리를 절대 경로로 정규화해 기준점으로 삼는다
    private val baseDir: Path = Path.of(properties.baseDir).toAbsolutePath().normalize()

    override fun store(key: String, content: ByteArray) {
        val target = resolve(key)
        Files.createDirectories(target.parent)
        // 임시 파일에 쓴 뒤 원자적으로 옮겨, 중간에 끊겨도 반쪽 파일이 남지 않게 한다
        val tmp = Files.createTempFile(target.parent, ".tmp-", null)
        Files.write(tmp, content)
        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING)
    }

    override fun load(key: String): ByteArray? {
        val target = resolve(key)
        return if (Files.exists(target)) Files.readAllBytes(target) else null
    }

    override fun exists(key: String): Boolean = Files.exists(resolve(key))

    override fun delete(key: String) {
        Files.deleteIfExists(resolve(key))
    }

    /** 키를 기준 디렉터리 내부 경로로 해석한다. 밖으로 벗어나면 거부. */
    private fun resolve(key: String): Path {
        require(key.isNotBlank()) { "저장 키는 비어 있을 수 없습니다." }
        val resolved = baseDir.resolve(key).normalize()
        require(resolved.startsWith(baseDir)) { "잘못된 저장 키입니다: $key" }
        return resolved
    }
}
