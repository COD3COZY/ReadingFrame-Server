package com.codecozy.server.controller;

import com.codecozy.server.dto.request.ReactionCommentRequest;
import com.codecozy.server.dto.request.ReportCommentRequest;
import com.codecozy.server.dto.request.ReadingBookCreateRequest;
import com.codecozy.server.dto.request.ReviewCreateRequest;
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
    public ResponseEntity createBook(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody ReadingBookCreateRequest request) {
        return ResponseEntity.ok(bookService.createBook(token, isbn, request));
    }

    @PostMapping("/report/{isbn}")
    public ResponseEntity reportComment(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody ReportCommentRequest request) {
        return ResponseEntity.ok(bookService.reportComment(token, isbn, request));
    }

    @PostMapping("/reaction/{isbn}")
    public ResponseEntity reactionComment(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody ReactionCommentRequest request) {
        return ResponseEntity.ok(bookService.reactionComment(token, isbn, request));
    }

    @PostMapping("/review/{isbn}")
    public ResponseEntity createReview(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody ReviewCreateRequest request) {
        return ResponseEntity.ok(bookService.createReview(token, isbn, request));
    }
}
