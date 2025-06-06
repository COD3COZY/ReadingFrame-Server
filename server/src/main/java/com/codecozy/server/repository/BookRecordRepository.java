package com.codecozy.server.repository;

import com.codecozy.server.composite_key.BookRecordKey;
import com.codecozy.server.dto.etc.CreateBookAction;
import com.codecozy.server.dto.etc.NoteWithReview;
import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.LocationInfo;
import com.codecozy.server.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRecordRepository extends JpaRepository<BookRecord, BookRecordKey> {
    // 특정 유저의 특정 책의 독서기록 가져오기
    BookRecord findByMemberAndBook(Member member, Book book);

    // 특정 유저의 특정 독서 상태의 모든 독서 노트 가져오기
    List<BookRecord> findAllByMemberAndReadingStatus(Member member, int readingStatus);

    // 특정 유저의 특정 독서 상태의 독서 노트 중, 10개만 가져오기
    List<BookRecord> findTop10ByMemberAndReadingStatusOrderByCreateDateDesc(Member member, int readingStatus);

    // (뱃지 검사용) 특정 유저의 몇몇 독서 상태를 갖고 있는 독서 노트를 모두 가져오기
    List<CreateBookAction> findAllByMemberAndReadingStatusIn(Member member, List<Integer> readingStatusList);

    // (뱃지 검사용) 특정 유저의 몇몇 독서 상태를 갖고 있고, 리뷰가 하나라도 등록된 독서노트들 모두 가져오기
    @Query("SELECT new com.codecozy.server.dto.etc.NoteWithReview(b.keyword, b.selectReview, b.bookReview)"
            + " FROM BookRecord b"
            + " LEFT JOIN b.selectReview sr"
            + " LEFT JOIN b.bookReview br"
            + " WHERE b.member = :member AND b.readingStatus IN :readingStatusList"
            + " AND (b.keyword IS NOT NULL OR sr IS NOT NULL OR br IS NOT NULL)")
    List<NoteWithReview> getBookRecordWithReviews(@Param("member") Member member,
                                                  @Param("readingStatusList") List<Integer> readingStatusList);

    // 특정 유저의 특정 독서 상태, 특정 숨김 여부를 갖고 있는 모든 독서 노트 가져오기
    List<BookRecord> findAllByMemberAndReadingStatusAndIsHidden(Member member, int readingStatus, boolean isHidden);

    // 특정 유저의 특정 문자열이 이름에 포함된 모든 독서 노트 가져오기
    List<BookRecord> findAllByMemberAndBookTitleContains(Member member, String title);

    // 특정 유저의 독서노트 중 locationId 값으로 검색하기
    List<BookRecord> findAllByMemberAndLocationInfo(Member member, LocationInfo locationInfo);

    // 사용자가 숨기지 않고, 최근에 기록을 작성한 특정 독서상태의 책 리스트 최대 10개 불러오기
    @Query("SELECT br FROM BookRecord br"
            + " WHERE br.member = :member AND br.readingStatus = :readingStatus AND br.isHidden = false"
            + " ORDER BY br.lastEditDate DESC LIMIT 10")
    List<BookRecord> getMainReadingBooks(@Param("member") Member member, @Param("readingStatus") int readingStatus);

    Long countByLocationInfo(LocationInfo locationInfo);
}
