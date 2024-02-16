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

    //@OneToOne(mappedBy = "member", cascade = CascadeType.REMOVE)
    //@PrimaryKeyJoinColumn(name = "member_apple")
    //private MemberApple memberApple;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    private List<Badge> badges;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    private List<Bookmark> bookmarks;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    @Column(name = "book_records")
    private List<BookRecord> bookRecords;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    @Column(name = "book_reviews")
    private List<BookReview> bookReviews;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    @Column(name = "keyword_reviews")
    private List<KeywordReview> keywordReviews;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    @Column(name = "member_locations")
    private List<MemberLocation> memberLocations;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    private List<Memo> memos;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    @Column(name = "personal_dictionaries")
    private List<PersonalDictionary> personalDictionaries;

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
