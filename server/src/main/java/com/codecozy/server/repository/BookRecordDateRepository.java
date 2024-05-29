package com.codecozy.server.repository;

import com.codecozy.server.composite_key.BookRecordDateKey;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.BookRecordDate;
import com.codecozy.server.entity.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRecordDateRepository extends JpaRepository<BookRecordDate, BookRecordDateKey> {

    // 사용자가 숨기지 않고, 최근에 기록을 작성한 특정 독서상태의 책 리스트 최대 10개 불러오기
    @Query("SELECT br FROM BookRecordDate d LEFT JOIN BookRecord br ON d.bookRecord = br"
            + " WHERE br.member = :member AND br.readingStatus = :readingStatus AND br.isHidden = false"
            + " ORDER BY d.lastDate DESC LIMIT 10")
    List<BookRecord> getMainReadingBooks(@Param("member") Member member, @Param("readingStatus") int readingStatus);
}
