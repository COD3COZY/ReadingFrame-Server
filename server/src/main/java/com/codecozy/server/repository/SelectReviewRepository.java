package com.codecozy.server.repository;

import com.codecozy.server.composite_key.SelectReviewKey;
import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.SelectReview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SelectReviewRepository extends JpaRepository<SelectReview, SelectReviewKey> {
    SelectReview findByBookRecord(BookRecord bookRecord);
    List<SelectReview> findAllByBookRecordBook(Book book);
}
