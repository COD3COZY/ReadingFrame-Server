package com.codecozy.server.entity;

import com.codecozy.server.composite_key.BadgeKey;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@IdClass(BadgeKey.class)
@Table(name = "BADGE")
public class Badge {
    @Id
    @ManyToOne
    @JoinColumn(name = "member_id", referencedColumnName = "member_id")
    private Member member;

    @Id
    @Column(name = "badge_code", nullable = false)
    private int badgeCode;

    @Column(length = 10, nullable = false)
    private String date;
}

