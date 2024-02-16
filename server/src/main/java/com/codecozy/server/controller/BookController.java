package com.codecozy.server.controller;

import com.codecozy.server.dto.request.BookCreateRequest;
import com.codecozy.server.dto.request.ReviewCreateRequest;
import com.codecozy.server.entity.Book;
import com.codecozy.server.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/book")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @PostMapping("/create/{isbn}")
    public ResponseEntity createBook(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody ReviewCreateRequest request) {
        return ResponseEntity.ok(bookService.createBook(token, isbn, request));
    }

    @PostMapping("/report")
    public ResponseEntity reportComment(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody int reportType) {
        return ResponseEntity.ok(bookService.reportComment(token, isbn, reportType));
    }
}
