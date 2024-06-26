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

    @ManyToOne
    @JoinColumn(name = "member_id", referencedColumnName = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    private Book book;

    @Column(name = "review_text", length = 200, nullable = false)
    private String reviewText;

    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate;

    @OneToOne(mappedBy = "bookReview", cascade = CascadeType.REMOVE)
    private BookReviewReaction bookReviewReaction;

    @OneToMany(mappedBy = "bookReview", cascade = CascadeType.REMOVE)
    private List<BookReviewReviewer> bookReviewReviewers;

    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public static BookReview create(Member member, Book book, String reviewText) {
        return BookReview.builder()
                .member(member)
                .book(book)
                .reviewText(reviewText)
                .reviewDate(LocalDate.now())
                .build();
    }
}
