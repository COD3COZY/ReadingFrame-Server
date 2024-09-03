package com.codecozy.server.repository;

import com.codecozy.server.entity.MemberKakao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberKakaoRepository extends JpaRepository<MemberKakao, Long> {
    // 이메일 값으로 유저 찾기
    MemberKakao findByEmail(String email);
}
