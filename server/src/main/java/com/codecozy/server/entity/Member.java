package com.codecozy.server.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    private List<Badge> badges;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    private List<BookRecord> bookRecords;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    private List<MemberLocation> memberLocations;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    private List<BookReviewReviewer> bookReviewReviewers;

    public static Member create(String nickname, String profile) {
        return Member.builder()
                .nickname(nickname)
                .profile(profile)
                .build();
    }

    public void modifyNickname(String nickname) {
        this.nickname = nickname;
    }

    public void modifyProfileImg(String profileCode) {
        this.profile = profileCode;
    }
}
