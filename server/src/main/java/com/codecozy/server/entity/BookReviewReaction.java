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
    @Column(name = "reaction_id")
    private Long reactionId;

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

    public void setReactionCountUp(int reactionCode) {
        if (reactionCode == 0) heartCount += 1;
        else if (reactionCode == 1) goodCount += 1;
        else if (reactionCode == 2) wowCount += 1;
        else if (reactionCode == 3) sadCount += 1;
        else if (reactionCode == 4) angryCount += 1;
    }

    public void setReportCountUp(int reportType) {
        if (reportType == 0) reportHatefulCount += 1;
        else if (reportType == 1) reportSpamCount += 1;
    }

    public void setReactionCountDown(int reactionCode) {
        if (reactionCode == 0) heartCount -= 1;
        else if (reactionCode == 1) goodCount -= 1;
        else if (reactionCode == 2) wowCount -= 1;
        else if (reactionCode == 3) sadCount -= 1;
        else if (reactionCode == 4) angryCount -= 1;
    }

    public static BookReviewReaction create() {
        return BookReviewReaction.builder()
                .heartCount(0)
                .goodCount(0)
                .wowCount(0)
                .sadCount(0)
                .angryCount(0)
                .reportHatefulCount(0)
                .reportSpamCount(0)
                .build();
    }
}
