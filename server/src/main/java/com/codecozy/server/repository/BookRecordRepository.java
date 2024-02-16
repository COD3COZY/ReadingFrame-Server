package com.codecozy.server.repository;

import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRecordRepository extends JpaRepository<BookRecord, String> {
    BookRecord findByMemberAndBook(Member member, Book book);
}
