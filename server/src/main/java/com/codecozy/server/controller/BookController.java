package com.codecozy.server.controller;

import com.codecozy.server.dto.request.*;
import com.codecozy.server.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/book")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    // 책 등록 API
    @PostMapping("/create/{isbn}")
    public ResponseEntity createBook(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody ReadingBookCreateRequest request) {
        return ResponseEntity.ok(bookService.createBook(token, isbn, request));
    }

    // 신고하기 API
    @PostMapping("/report/{isbn}")
    public ResponseEntity reportComment(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody ReportCommentRequest request) {
        return ResponseEntity.ok(bookService.reportComment(token, isbn, request));
    }

    // 한줄평 반응 추가 API
    @PostMapping("/reaction/{isbn}")
    public ResponseEntity reactionComment(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody ReactionCommentRequest request) {
        return ResponseEntity.ok(bookService.reactionComment(token, isbn, request));
    }

    // 리뷰 작성 API
    @PostMapping("/review/{isbn}")
    public ResponseEntity createReview(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody ReviewCreateRequest request) {
        return ResponseEntity.ok(bookService.createReview(token, isbn, request));
    }

    // 책별 대표 위치 등록 API
    @PostMapping("/mainLocation/{isbn}")
    public ResponseEntity addMainLocation(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody LocationCreateRequest request) {
        return ResponseEntity.ok(bookService.addMainLocation(token, isbn, request));
    }

    // 인물사전 등록 API
    @PostMapping("/personalDictionary/{isbn}")
    public ResponseEntity addpersonalDictionary(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody PersonalDictionaryRequest request) {
        return ResponseEntity.ok(bookService.addpersonalDictionary(token, isbn, request));
    }
}
