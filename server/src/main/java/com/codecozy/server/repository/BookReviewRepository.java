package com.codecozy.server.repository;

import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.BookReview;
import com.codecozy.server.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookReviewRepository extends JpaRepository<BookReview, Long> {
    BookReview findByMemberAndBook(Member member, Book book);
}
