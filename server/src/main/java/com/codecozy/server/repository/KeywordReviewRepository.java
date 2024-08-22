package com.codecozy.server.repository;

import com.codecozy.server.composite_key.KeywordReviewKey;
import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.KeywordReview;
import com.codecozy.server.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeywordReviewRepository extends JpaRepository<KeywordReview, KeywordReviewKey> {
    KeywordReview findByMemberAndBook(Member member, Book book);
    List<KeywordReview> findAllByBook(Book book);
}
