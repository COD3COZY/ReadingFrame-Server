package com.codecozy.server.entity;

import com.codecozy.server.composite_key.KeywordReviewKey;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Builder
@IdClass(KeywordReviewKey.class)
@Table(name = "KEYWORD_REVIEW")
public class KeywordReview {
    @Id
    @ManyToOne
    @JoinColumn(name = "member_id", referencedColumnName = "member_id")
    private Member member;

    @Id
    @ManyToOne
    @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    private Book book;

    @Column(name = "select_review_code", length = 14, nullable = false)
    private String selectReviewCode;

    public static KeywordReview create(Member member, Book book, String selectReviewCode) {
        return KeywordReview.builder()
                .member(member)
                .book(book)
                .selectReviewCode(selectReviewCode)
                .build();
    }
}
