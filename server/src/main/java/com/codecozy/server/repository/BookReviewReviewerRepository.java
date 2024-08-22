package com.codecozy.server.repository;

import com.codecozy.server.composite_key.BookReviewReviewerKey;
import com.codecozy.server.entity.BookReview;
import com.codecozy.server.entity.BookReviewReviewer;
import com.codecozy.server.entity.Member;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookReviewReviewerRepository extends JpaRepository<BookReviewReviewer, BookReviewReviewerKey> {
    // FIXME: findByBookReview 메소드의 반환 타입 List<BookReviewReviewer>로 수정 필요
    // FIXME: 위 수정사항에 따라 findByBookReview 메소드와 연관된 코드들 수정 필요
    BookReviewReviewer findByBookReview(BookReview bookReview);
    BookReviewReviewer findByBookReviewAndMember(BookReview bookReview, Member member);

    // 해당 comment_id에 등록된 모든 reviewer 정보 지우기
    @Transactional
    void deleteAllByBookReview(BookReview bookReview);
}
