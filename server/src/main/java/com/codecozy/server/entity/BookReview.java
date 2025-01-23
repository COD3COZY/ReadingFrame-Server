package com.codecozy.server.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "BOOK_REVIEW")
public class BookReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "member_id", referencedColumnName = "member_id"),
            @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    })
    private BookRecord bookRecord;

    @Column(name = "review_text", length = 200, nullable = false)
    private String reviewText;

    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;

    @OneToOne(mappedBy = "bookReview", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private BookReviewReaction bookReviewReaction;

    @OneToMany(mappedBy = "bookReview", cascade = CascadeType.REMOVE)
    private List<BookReviewReviewer> bookReviewReviewers;

    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public static BookReview create(BookRecord bookRecord, String reviewText) {
        return BookReview.builder()
                .bookRecord(bookRecord)
                .reviewText(reviewText)
                .reviewDate(LocalDate.now())
                .build();
    }
}
