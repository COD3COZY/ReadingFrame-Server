package com.codecozy.server.cache;

import com.codecozy.server.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MemberCacheManagerTest {

    private MemberCacheManager memberCacheManager;

    private Member member;
    private Long memberId;

    @BeforeEach
    void setup() {
        memberCacheManager = new MemberCacheManager();

        member = Member.create("다은", "10");
        memberId = member.getMemberId();
    }

    @Test
    @DisplayName("캐시에 데이터 삽입 및 조회 테스트")
    void cachePutAndGetTest() {
        // when
        memberCacheManager.put(memberId, member);
        Member cachedMember = memberCacheManager.get(memberId);

        // then
        assertThat(cachedMember).isNotNull();
        assertThat(cachedMember.getMemberId()).isEqualTo(memberId);
        assertThat(cachedMember.getNickname()).isEqualTo("다은");
        assertThat(cachedMember.getProfile()).isEqualTo("10");
    }

    @Test
    @DisplayName("캐시에 존재하지 않는 데이터 조회")
    void cacheNotExistTest() {
        // when
        Member member = memberCacheManager.get(100L);

        // then
        assertThat(member).isNull();
    }

    @Test
    @DisplayName("캐시 데이터 삭제 테스트")
    void cacheRemoveTest() {
        // given
        memberCacheManager.put(memberId, member);

        // when
        memberCacheManager.remove(memberId);
        Member cachedMember = memberCacheManager.get(memberId);

        // then
        assertThat(cachedMember).isNull();
    }

    @Test
    @DisplayName("캐시 전체 비우기 테스트")
    void cacheClearTest() {
        // given
        Member member = Member.create("강아지", "23");
        Long memberId = member.getMemberId();
        Member member2 = Member.create("고양이", "32");
        Long member2Id = member2.getMemberId();

        memberCacheManager.put(memberId, member);
        memberCacheManager.put(member2Id, member2);

        // when
        memberCacheManager.clearCache();

        // then
        assertThat(memberCacheManager.get(memberId)).isNull();
        assertThat(memberCacheManager.get(member2Id)).isNull();
    }
}
