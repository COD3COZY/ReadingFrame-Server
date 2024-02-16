package com.codecozy.server.repository;

import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.Bookmark;
import com.codecozy.server.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Bookmark findByMemberAndBookAndUuid(Member member, Book book, String uuid);
}
