package com.codecozy.server.service;

import com.codecozy.server.dto.request.BookCreateRequest;
import com.codecozy.server.entity.Book;
import com.codecozy.server.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BookService {

    private final BookRepository bookRepository;

    // 책 등록
    public Book createBook(BookCreateRequest book) {
        Book bookToCreate = new Book();
        BeanUtils.copyProperties(book, bookToCreate);
        return bookRepository.save(bookToCreate);
    }


}
