package com.codecozy.server.repository;

import com.codecozy.server.composite_key.BookReviewReviewerKey;
import com.codecozy.server.entity.BookReview;
import com.codecozy.server.entity.BookReviewReviewer;
import com.codecozy.server.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookReviewReviewerRepository extends JpaRepository<BookReviewReviewer, BookReviewReviewerKey> {
    BookReviewReviewer findByBookReview(BookReview bookReview);
    BookReviewReviewer findByBookReviewAndMember(BookReview bookReview, Member member);
}
