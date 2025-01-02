package com.codecozy.server.repository;

import com.codecozy.server.composite_key.BookmarkKey;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.Bookmark;
import com.codecozy.server.entity.LocationList;
import com.codecozy.server.entity.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, BookmarkKey> {
    // 특정 독서노트에서 uuid로 책갈피 찾기
    Bookmark findByBookRecordAndUuid(BookRecord bookRecord, String uuid);

    // 특정 유저의 모든 책갈피 가져오기
    List<Bookmark> findAllByBookRecordMember(Member member);

    // 특정 독서노트에 쓴 모든 책갈피 가져오기
    List<Bookmark> findAllByBookRecord(BookRecord bookRecord);

    Long countByLocationList(LocationList locationList);

    // 특정 유저의 책갈피 중 locationId 값으로 검색하기
    List<Bookmark> findAllByBookRecordMemberAndLocationList(Member member, LocationList locationList);

    // 특정 독서노트의 최근 책갈피 3개 가져오기
    List<Bookmark> findTop3ByBookRecordOrderByDateDesc(BookRecord bookRecord);
}
