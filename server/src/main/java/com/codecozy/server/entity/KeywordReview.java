package com.codecozy.server.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.lang.reflect.Member;

@Entity
@Getter
@Table(name = "keyword_review")
public class KeywordReview {
    @Id
    @ManyToOne
    @JoinColumn(name = "memberId", referencedColumnName = "memberId")
    private Member member;

    @Id
    @ManyToOne
    @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    private Book book;

    @Column(length = 14, nullable = false)
    private String selectReviewCode;
}
