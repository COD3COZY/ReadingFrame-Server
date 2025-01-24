package com.codecozy.server.repository;

import com.codecozy.server.composite_key.BadgeKey;
import com.codecozy.server.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, BadgeKey> {
}
