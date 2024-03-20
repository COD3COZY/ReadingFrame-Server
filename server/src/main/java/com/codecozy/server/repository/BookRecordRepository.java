package com.codecozy.server.repository;

import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.LocationList;
import com.codecozy.server.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRecordRepository extends JpaRepository<BookRecord, String> {
    // 특정 유저의 특정 책의 독서기록 가져오기
    BookRecord findByMemberAndBook(Member member, Book book);

    // 특정 유저의 모든 독서 노트 가져오기
    List<BookRecord> findAllByMember(Member member);

    // 특정 유저의 특정 독서 상태의 모든 독서 노트 가져오기
    List<BookRecord> findAllByMemberAndReadingStatus(Member member, int readingStatus);

    Long countByLocationList(LocationList locationList);
}
