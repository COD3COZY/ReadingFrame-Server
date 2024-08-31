package com.codecozy.server.repository;

import com.codecozy.server.composite_key.BookReviewReviewerKey;
import com.codecozy.server.entity.BookReview;
import com.codecozy.server.entity.BookReviewReviewer;
import com.codecozy.server.entity.Member;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookReviewReviewerRepository extends JpaRepository<BookReviewReviewer, BookReviewReviewerKey> {
    BookReviewReviewer findByBookReview(BookReview bookReview);
    List<BookReviewReviewer> findAllByBookReview(BookReview bookReview);
    BookReviewReviewer findByBookReviewAndMember(BookReview bookReview, Member member);

    // 해당 comment_id에 등록된 모든 reviewer 정보 지우기
    @Transactional
    void deleteAllByBookReview(BookReview bookReview);
}
