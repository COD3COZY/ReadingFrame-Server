package com.codecozy.server.repository;

import com.codecozy.server.entity.BookReview;
import com.codecozy.server.entity.BookReviewReaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookReviewReactionRepository extends JpaRepository<BookReviewReaction, BookReview> {
    BookReviewReaction findByBookReview(BookReview bookReview);
}