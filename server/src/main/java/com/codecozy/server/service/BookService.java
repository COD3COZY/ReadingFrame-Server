package com.codecozy.server.service;

import com.codecozy.server.context.StatusCode;
import com.codecozy.server.dto.request.BookCreateRequest;
import com.codecozy.server.dto.response.DefaultResponse;
import com.codecozy.server.entity.Book;
import com.codecozy.server.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BookService {

    private final BookRepository bookRepository;

    // 모든 사용자가 등록한 책
    public ResponseEntity<DefaultResponse> createBook(BookCreateRequest request) {
        Book book = Book.create(request.isbn(), request.cover(), request.title(), request.author(), request.category(),
                request.totalPage());
        bookRepository.save(book);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 사용자의 독서노트
//    public BookRecord createBookRecord(BookCreateRequest bookRecord) {
//        BookRecord bookRecordToCreate = new BookRecord();
//        BeanUtils.copyProperties(bookRecord, bookRecordToCreate);
//        return bookRecordRepository.save(bookRecordToCreate);
//    }
}
