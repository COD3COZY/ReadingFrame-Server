package com.codecozy.server.service;

import com.codecozy.server.dto.request.BookDTO;
import com.codecozy.server.entity.Book;
import com.codecozy.server.repository.BookRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLEngineResult;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public String create(BookDTO bookDTO) {
        Book book = Book.create(bookDTO.getIsbn());
        bookRepository.save(book);
        book = bookRepository.findByIsbn(bookDTO.getIsbn());

        return "성공";
    }


}
