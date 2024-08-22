package com.codecozy.server.repository;

import com.codecozy.server.composite_key.BookmarkKey;
import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.Bookmark;
import com.codecozy.server.entity.LocationList;
import com.codecozy.server.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, BookmarkKey> {
    Bookmark findByMemberAndBookAndUuid(Member member, Book book, String uuid);
    // 특정 유저의 모든 책갈피 가져오기
    List<Bookmark> findAllByMember(Member member);

    List<Bookmark> findAllByMemberAndBook(Member member, Book book);
    Long countByLocationList(LocationList locationList);

    // 특정 유저의 최근 책갈피 3개 가져오기
    List<Bookmark> findTop3ByMemberAndBookOrderByDateDesc(Member member, Book book);
}
