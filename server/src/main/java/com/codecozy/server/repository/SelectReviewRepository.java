package com.codecozy.server.repository;

import com.codecozy.server.composite_key.SelectReviewKey;
import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.SelectReview;
import com.codecozy.server.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SelectReviewRepository extends JpaRepository<SelectReview, SelectReviewKey> {
    SelectReview findByMemberAndBook(Member member, Book book);
    List<SelectReview> findAllByBook(Book book);
}
