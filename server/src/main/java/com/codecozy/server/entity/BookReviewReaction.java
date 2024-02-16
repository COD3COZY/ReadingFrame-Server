package com.codecozy.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "BOOK_REVIEW_REACTION")
public class BookReviewReaction {
    @Id
    @OneToOne
    @JoinColumn(name = "comment_id", referencedColumnName = "comment_id")
    private BookReview bookReview;

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
