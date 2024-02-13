package com.codecozy.server.entity;


import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "member")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long memberId;

    @Column(length = 20, nullable = false)
    private String nickname;

    @Column(length = 2, nullable = false)
    private String profile;

    @OneToOne(mappedBy = "user")
    private MemberApple memberApple;
}
