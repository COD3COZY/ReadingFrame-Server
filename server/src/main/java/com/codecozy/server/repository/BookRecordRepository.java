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

    // FIXME: 최신 10개를 가져오도록 변경 필요 (DB에 create date column 추가 필요?)
    // 특정 유저의 특정 독서 상태의 독서 노트 중, 10개만 가져오기
    List<BookRecord> findTop10ByMemberAndReadingStatus(Member member, int readingStatus);

    // 특정 유저의 특정 독서 상태, 특정 숨김 여부를 갖고 있는 모든 독서 노트 가져오기
    List<BookRecord> findAllByMemberAndReadingStatusAndIsHidden(Member member, int readingStatus, boolean isHidden);

    // FIXME: 최신 10개를 가져오도록 변경 필요
    // 특정 유저의 특정 독서 상태, 특정 숨김 여부를 갖고 있는 독서 노트 중, 10개만 가져오기
    List<BookRecord> findTop10ByMemberAndReadingStatusAndIsHidden(Member member, int readingStatus, boolean isHidden);

    Long countByLocationList(LocationList locationList);
}
