package com.codecozy.server.repository;

import com.codecozy.server.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, String> {
    // ISBN 값으로 책 찾기
    Book findByIsbn(String isbn);
}
