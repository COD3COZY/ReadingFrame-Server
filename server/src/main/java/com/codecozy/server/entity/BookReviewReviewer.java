package com.codecozy.server.entity;

import com.codecozy.server.composite_key.BookReviewReviewerKey;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(BookReviewReviewerKey.class)
@Table(name = "BOOK_REVIEW_REVIEWER")
public class BookReviewReviewer {
    @Id
    @ManyToOne
    @JoinColumn(name = "comment_id", referencedColumnName = "comment_id")
    private BookReview bookReview;

    @Id
    @ManyToOne
    @JoinColumn(name = "reviewer_id", referencedColumnName = "member_id")
    private Member member;

    @Column(name = "is_heart", nullable = false)
    private boolean isHeart;

    @Column(name = "is_good", nullable = false)
    private boolean isGood;

    @Column(name = "is_wow", nullable = false)
    private boolean isWow;

    @Column(name = "is_sad", nullable = false)
    private boolean isSad;

    @Column(name = "is_angry", nullable = false)
    private boolean isAngry;

    @Column(name = "is_report_hateful", nullable = false)
    private boolean isReportHateful;

    @Column(name = "is_report_spam", nullable = false)
    private boolean isReportSpam;

    public void setIsHeartReverse() { isHeart = !isHeart; }
    public void setIsGoodReverse() { isGood = !isGood; }
    public void setIsWowReverse() { isWow = !isWow; }
    public void setIsSadReverse() { isSad = !isSad; }
    public void setIsAngryReverse() { isAngry = !isAngry; }
    public void setIsReportHatefulReverse() { isReportHateful = !isReportHateful; }
    public void setIsReportSpamReverse() { isReportSpam = !isReportSpam; }


    public static BookReviewReviewer create(BookReview bookReview, Member member) {
        return BookReviewReviewer.builder()
                .bookReview(bookReview)
                .member(member)
                .isHeart(false)
                .isGood(false)
                .isWow(false)
                .isSad(false)
                .isAngry(false)
                .isReportHateful(false)
                .isReportSpam(false)
                .build();
    }
}
