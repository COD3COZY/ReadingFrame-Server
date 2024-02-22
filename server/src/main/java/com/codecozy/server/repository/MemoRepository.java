package com.codecozy.server.repository;

import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.Member;
import com.codecozy.server.entity.Memo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemoRepository extends JpaRepository<Memo, Long> {
    Memo findByMemberAndBookAndUuid(Member member, Book book, String uuid);
    List<Memo> findAllByMemberAndBook(Member member, Book book);

    // 특정 유저의 최근 3개의 메모 가져오기
    List<Memo> findTop3ByMemberAndBookOrderByDateDesc(Member member, Book book);
}
