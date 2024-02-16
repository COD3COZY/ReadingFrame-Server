package com.codecozy.server.repository;

import com.codecozy.server.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, String> {
    // ISBN 값으로 책 찾기
    Book findByIsbn(String isbn);
}
