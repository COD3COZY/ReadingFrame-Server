package com.codecozy.server.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "BOOK_REVIEW_REACTION")
public class BookReviewReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long bookReviewReactionId;

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

    public void setHeartCount() { heartCount += 1; }

    public void setGoodCount() { goodCount += 1; }

    public void setWowCount() { wowCount += 1; }

    public void setSadCount() { sadCount += 1; }

    public void setAngryCount() { angryCount += 1; }

    public void setReportHatefulCount() { reportHatefulCount += 1; }

    public void setReportSpamCountCount() { reportSpamCount += 1; }

    public static BookReviewReaction create(BookReview bookReview, int heartCount, int goodCount, int wowCount, int sadCount, int angryCount, int reportHatefulCount, int reportSpamCount) {
        return BookReviewReaction.builder()
                .bookReview(bookReview)
                .heartCount(heartCount)
                .goodCount(goodCount)
                .wowCount(wowCount)
                .sadCount(sadCount)
                .angryCount(angryCount)
                .reportHatefulCount(reportHatefulCount)
                .reportSpamCount(reportSpamCount)
                .build();
    }
}
