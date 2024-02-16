package com.codecozy.server.repository;

import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.KeywordReview;
import com.codecozy.server.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordReviewRepository extends JpaRepository<KeywordReview, Long> {
    KeywordReview findByMemberAndBook(Member member, Book book);
}
