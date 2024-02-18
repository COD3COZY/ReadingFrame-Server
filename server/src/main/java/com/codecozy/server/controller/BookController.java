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
        return bookService.createBook(token, isbn, request);
    }

    // 신고하기 API
    @PostMapping("/report/{isbn}")
    public ResponseEntity reportComment(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody ReportCommentRequest request) {
        return bookService.reportComment(token, isbn, request);
    }

    // 한줄평 반응 추가 API
    @PostMapping("/reaction/{isbn}")
    public ResponseEntity reactionComment(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody ReactionCommentRequest request) {
        return bookService.reactionComment(token, isbn, request);
    }

    // 리뷰 작성 API
    @PostMapping("/review/{isbn}")
    public ResponseEntity createReview(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody ReviewCreateRequest request) {
        return bookService.createReview(token, isbn, request);
    }

    // 책별 대표 위치 등록 API
    @PostMapping("/mainLocation/{isbn}")
    public ResponseEntity addMainLocation(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody LocationRequest request) {
        return bookService.addMainLocation(token, isbn, request);
    }

    // 책별 대표 위치 변경 API
    @PatchMapping("/patchMainLocation/{isbn}")
    public ResponseEntity patchMainLocation(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody LocationRequest request) {
        return bookService.patchMainLocation(token, isbn, request);
    }

    // 책별 대표 위치 삭제 API
    @DeleteMapping("/deleteMainLocation/{isbn}")
    public ResponseEntity deleteMainLocation(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn) {
        return bookService.deleteMainLocation(token, isbn);
    }

    // 인물사전 등록 API
    @PostMapping("/personalDictionary/{isbn}")
    public ResponseEntity addpersonalDictionary(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody PersonalDictionaryRequest request) {
        return bookService.addpersonalDictionary(token, isbn, request);
    }

    // 메모 등록 API
    @PostMapping("/memo/{isbn}")
    public ResponseEntity addMemo(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody MemoRequest request) {
        return bookService.addMemo(token, isbn, request);
    }

    // 책갈피 등록 API
    @PostMapping("/bookmark/{isbn}")
    public ResponseEntity addBookmark(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody BookmarkRequest request) {
        return bookService.addBookmark(token, isbn, request);
    }

    // 전체 위치 조회 API
    @GetMapping("/getAllLocation")
    public ResponseEntity getAllLocation(@RequestHeader("xAuthToken") String token, @RequestBody GetAllLocationRequest request) {
        return bookService.getAllLocation(token, request);
    }

    // 최근 등록 위치 조회 API
    @GetMapping("/getRecentLocation")
    public ResponseEntity getRecentLocation(@RequestHeader("xAuthToken") String token) {
        return bookService.getRecentLocation(token);
    }

    // 최근 등록 위치 삭제 API
    @DeleteMapping("/deleteRecentLocation")
    public ResponseEntity deleteRecentLocation(@RequestHeader("xAuthToken") String token, @RequestBody deleteRecentLocationRequest request) {
        return bookService.deleteRecentLocation(token, request);
    }
}
