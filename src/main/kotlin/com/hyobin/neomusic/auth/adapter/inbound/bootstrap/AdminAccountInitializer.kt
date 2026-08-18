package com.hyobin.neomusic.auth.adapter.inbound.bootstrap

import com.hyobin.neomusic.auth.application.port.outbound.LoadMemberPort
import com.hyobin.neomusic.auth.application.port.outbound.PasswordEncoderPort
import com.hyobin.neomusic.auth.application.port.outbound.SaveMemberPort
import com.hyobin.neomusic.auth.domain.Member
import com.hyobin.neomusic.auth.domain.Nickname
import com.hyobin.neomusic.auth.domain.Role
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

/**
 * 서버 기동 시 관리자 계정이 없으면 설정값으로 1개 생성한다.
 * 이미 존재하면 아무것도 하지 않으므로, 재시작해도 중복 생성되지 않는다(멱등).
 *
 * 이건 애플리케이션 외부에서 들어오는 "기동 트리거"라 inbound 어댑터로 둔다.
 * 실제 저장/해싱은 기존 출력 포트를 그대로 재사용한다(도메인 규칙 일관성 유지).
 */
@Component
class AdminAccountInitializer(
    private val loadMemberPort: LoadMemberPort,
    private val saveMemberPort: SaveMemberPort,
    private val passwordEncoder: PasswordEncoderPort,
    private val properties: AdminProperties,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments?) {
        if (!properties.enabled) {
            log.info("관리자 부트스트랩 비활성화(neomusic.admin.enabled=false) — 건너뜀")
            return
        }

        val nickname = Nickname.of(properties.nickname)
        if (loadMemberPort.existsByNickname(nickname)) {
            log.info("관리자 계정이 이미 존재합니다: {}", nickname.value)
            return
        }

        val admin = Member.create(
            nickname = nickname,
            passwordHash = passwordEncoder.encode(properties.password),
            role = Role.ADMIN,
        )
        saveMemberPort.save(admin)
        log.warn(
            "관리자 계정을 생성했습니다: '{}' — 운영에서는 반드시 비밀번호를 변경하세요.",
            nickname.value,
        )
    }
}
