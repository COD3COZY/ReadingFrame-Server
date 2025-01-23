package com.codecozy.server.entity;

import com.codecozy.server.composite_key.BookReviewReviewerKey;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", referencedColumnName = "comment_id")
    private BookReview bookReview;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", referencedColumnName = "member_id")
    private Member member;

    @Column(name = "is_reaction", nullable = false)
    private boolean isReaction;

    @Column(name = "reaction_code", nullable = false)
    private int reactionCode;

    @Column(name = "is_report", nullable = false)
    private boolean isReport;

    @Column(name = "report_type", nullable = false)
    private int reportType;

    public void setIsReactionReverse() { isReaction = !isReaction; }
    public void setReactionCode(int reactionCode) { this.reactionCode = reactionCode; }

    public void setIsReportReverse() { isReport = !isReport; }
    public void setReportType(int reportType) { this.reportType = reportType; }

    public static BookReviewReviewer create(BookReview bookReview, Member member) {
        return BookReviewReviewer.builder()
                .bookReview(bookReview)
                .member(member)
                .isReaction(false)
                .reactionCode(0)
                .build();
    }
}
