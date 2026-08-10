package com.hyobin.neomusic.auth.adapter.outbound.persistence

import com.hyobin.neomusic.auth.domain.Member
import com.hyobin.neomusic.auth.domain.Nickname
import com.hyobin.neomusic.auth.domain.PasswordHash
import com.hyobin.neomusic.auth.domain.Role
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(MemberPersistenceAdapter::class)
class MemberPersistenceAdapterTest @Autowired constructor(
    private val adapter: MemberPersistenceAdapter,
    private val em: TestEntityManager,
) {

    @Test
    fun `회원을 저장하면 id가 발급되고 닉네임으로 조회된다`() {
        val member = Member.create(Nickname.of("할머니"), PasswordHash("hashed"))

        val saved = adapter.save(member)
        em.flush(); em.clear()

        saved.id.shouldNotBeNull()
        val found = adapter.findByNickname(Nickname.of("할머니"))!!
        found.nickname shouldBe Nickname.of("할머니")
        found.role shouldBe Role.USER
        found.failedAttempts shouldBe 0
        found.lockedUntil shouldBe null
    }

    @Test
    fun `닉네임 존재 여부를 확인할 수 있다`() {
        adapter.save(Member.create(Nickname.of("영수"), PasswordHash("h")))
        em.flush(); em.clear()

        adapter.existsByNickname(Nickname.of("영수")) shouldBe true
        adapter.existsByNickname(Nickname.of("없는사람")) shouldBe false
    }
}
