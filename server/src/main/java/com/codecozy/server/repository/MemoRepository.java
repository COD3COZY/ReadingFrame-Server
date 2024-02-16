package com.codecozy.server.repository;

import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.Member;
import com.codecozy.server.entity.Memo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoRepository extends JpaRepository<Memo, Long> {
    Memo findByMemberAndBookAndUuid(Member member, Book book, String uuid);
}
