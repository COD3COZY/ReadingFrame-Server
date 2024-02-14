package com.codecozy.server.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Entity
@Getter
@EqualsAndHashCode
@Table(name = "MEMBER_APPLE")
public class MemberApple {
    @Id
    @OneToOne
    @JoinColumn(name = "member_id", referencedColumnName = "member_id")
    private Member member;

    @Column(length = 250, nullable = false)
    private String apple_id;
}
