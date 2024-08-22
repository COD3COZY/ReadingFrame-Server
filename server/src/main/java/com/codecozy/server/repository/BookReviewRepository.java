package com.codecozy.server.repository;

import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.BookReview;
import com.codecozy.server.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookReviewRepository extends JpaRepository<BookReview, Long> {
    BookReview findByMemberAndBook(Member member, Book book);
    int countByBook(Book book);
    List<BookReview> findAllByBook(Book book);
    List<BookReview> findAllByBookOrderByReviewDateDesc(Book book);
}
