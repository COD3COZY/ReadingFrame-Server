package com.codecozy.server.controller;

import com.codecozy.server.dto.request.BookCreateRequest;
import com.codecozy.server.entity.Book;
import com.codecozy.server.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

//    @PostMapping("/create")
//    public ResponseEntity<Book> createBook(@PathVariable String isbn, @RequestBody BookCreateRequest request) {
//        return ResponseEntity.ok(bookService.createBook(isbn, request));
//    }

    @PostMapping("/create")
    public ResponseEntity createBook(@RequestBody BookCreateRequest request) {
        return ResponseEntity.ok(bookService.createBook(request));
    }
}
