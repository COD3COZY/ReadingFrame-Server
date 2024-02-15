package com.codecozy.server.entity;

import com.codecozy.server.composite_key.BookReviewKey;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@IdClass(BookReviewKey.class)
@Table(name = "BOOK_REVIEW")
public class BookReview {
    @Id
    @ManyToOne
    @JoinColumn(name = "member_id", referencedColumnName = "member_id")
    private Member member;

    @Id
    @ManyToOne
    @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    private Book book;

    @Column(length = 200, nullable = false)
    private String review_text;

    @Column(name = "heart_count", nullable = false)
    private int heartCount;

    @Column(name = "good_count", nullable = false)
    private int goodCount;

    @Column(name = "wow_count", nullable = false)
    private int wowCount;

    @Column(name = "sad_count", nullable = false)
    private int sadCount;

    @Column(name = "angry_count", nullable = false)
    private int angryCount;

    @Column(name = "report_hateful_count", nullable = false)
    private int reportHatefulCount;

    @Column(name = "report_spam_count", nullable = false)
    private int reportSpamCount;
}
