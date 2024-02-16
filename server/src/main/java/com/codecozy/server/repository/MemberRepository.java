package com.codecozy.server.repository;

import com.codecozy.server.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 고유 아이디값으로 유저 찾기
    Member findByMemberId(Long memberId);

    // 닉네임으로 유저 찾기
    Member findByNickname(String nickname);
}
