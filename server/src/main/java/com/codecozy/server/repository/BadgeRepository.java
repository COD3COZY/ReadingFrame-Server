package com.codecozy.server.repository;

import com.codecozy.server.composite_key.BadgeKey;
import com.codecozy.server.entity.Badge;
import com.codecozy.server.entity.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, BadgeKey> {
    // 특정 유저의 모든 뱃지 가져오기
    List<Badge> findAllByMember(Member member);
}
