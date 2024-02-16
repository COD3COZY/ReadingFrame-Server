package com.codecozy.server.entity;

import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;

@Entity
@Getter
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

    @OneToOne(mappedBy = "bookReview", cascade = CascadeType.REMOVE)
    private BookReviewReaction bookReviewReaction;

    @OneToMany(mappedBy = "bookReview", cascade = CascadeType.REMOVE)
    private List<BookReviewReviewer> bookReviewReviewers;
}
