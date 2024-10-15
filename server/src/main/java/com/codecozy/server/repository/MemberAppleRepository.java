package com.codecozy.server.repository;

import com.codecozy.server.entity.MemberApple;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberAppleRepository extends JpaRepository<MemberApple, Long> {
    MemberApple findByUserIdentifier(String userIdentifier);
}
