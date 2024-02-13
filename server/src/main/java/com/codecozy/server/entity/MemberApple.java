package com.codecozy.server.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.lang.reflect.Member;

@Entity
@Getter
@Table(name = "member_apple")
public class MemberApple {
    @Id
    @OneToOne
    @JoinColumn(name = "memberId", referencedColumnName = "memberId")
    private Member member;

    @Column(length = 250, nullable = false)
    private String key;
}
