package com.codecozy.server.cache;

import com.codecozy.server.entity.Member;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MemberCacheManager {
    private final Cache<Long, Member> memberCache;

    // 캐시 만료 시간과 크기 설정
    public MemberCacheManager() {
        this.memberCache = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(200)
                .build();
    }

    // 캐시 데이터 추가
    public void put(Long memberId, Member member) {
        memberCache.put(memberId, member);
    }

    // 캐시 데이터 조회
    public Member get(Long memberId) {
        return memberCache.getIfPresent(memberId);
    }

    // 캐시 데이터 삭제
    public void remove(Long memberId) {
        memberCache.invalidate(memberId);
    }

    // 캐시 전체 비우기
    public void clearCache() {
        memberCache.invalidateAll();
    }
}
