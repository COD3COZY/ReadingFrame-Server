package com.codecozy.server.entity;


import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "MEMBER")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private long memberId;

    @Column(length = 20, nullable = false)
    private String nickname;

    @Column(length = 2, nullable = false)
    private String profile;

    @OneToOne(mappedBy = "member")
    @Column(name = "member_apple")
    private MemberApple memberApple;
}
