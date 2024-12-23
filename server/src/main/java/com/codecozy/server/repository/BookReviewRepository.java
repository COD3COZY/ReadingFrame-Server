package com.codecozy.server.repository;

import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.BookReview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookReviewRepository extends JpaRepository<BookReview, Long> {
    BookReview findByBookRecord(BookRecord bookRecord);
    int countByBookRecordBook(Book book);
    List<BookReview> findAllByBookRecordBook(Book book);
    List<BookReview> findAllByBookRecordBookOrderByReviewDateDesc(Book book);
    BookReview findByCommentId(Long commentId);
}
