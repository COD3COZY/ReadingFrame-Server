package com.codecozy.server.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.lang.reflect.Member;

@Entity
@Getter
@Table(name = "book_review")
public class BookReview {
    @Id
    @ManyToOne
    @JoinColumn(name = "memberId", referencedColumnName = "memberId")
    private Member member;

    @Id
    @ManyToOne
    @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    private Book book;

    @Column(length = 200, nullable = false)
    private String text;

    @Column(nullable = false)
    private int heartCount;

    @Column(nullable = false)
    private int goodCount;

    @Column(nullable = false)
    private int wowCount;

    @Column(nullable = false)
    private int sadCount;

    @Column(nullable = false)
    private int angryCount;

    @Column(nullable = false)
    private int reportHatefulCount;

    @Column(nullable = false)
    private int reportSpamCount;
}
