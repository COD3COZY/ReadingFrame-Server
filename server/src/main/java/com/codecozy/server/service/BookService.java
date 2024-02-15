package com.codecozy.server.service;

import com.codecozy.server.dto.request.BookCreateRequest;
import com.codecozy.server.entity.Book;
import com.codecozy.server.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BookService {

    private final BookRepository bookRepository;

    // 모든 사용자가 등록한 책
    public Book createBook(BookCreateRequest book) {
        Book bookToCreate = new Book();
        BeanUtils.copyProperties(book, bookToCreate);
        bookToCreate.setCover(book.getBookInformation().get(0));
        bookToCreate.setTitle(book.getBookInformation().get(1));
        bookToCreate.setAuthor(book.getBookInformation().get(2));
        bookToCreate.setCategory(book.getBookInformation().get(3));
        bookToCreate.setTotalPage(Integer.parseInt(book.getBookInformation().get(4)));
        return bookRepository.save(bookToCreate);
    }

    // 사용자의 독서노트
//    public BookRecord createBookRecord(BookCreateRequest bookRecord) {
//        BookRecord bookRecordToCreate = new BookRecord();
//        BeanUtils.copyProperties(bookRecord, bookRecordToCreate);
//        return bookRecordRepository.save(bookRecordToCreate);
//    }
}
