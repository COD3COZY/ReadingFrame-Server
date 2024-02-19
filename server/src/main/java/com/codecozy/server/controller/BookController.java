package com.codecozy.server.controller;

import com.codecozy.server.dto.request.*;
import com.codecozy.server.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    // 리뷰 작성 API
    @PostMapping("/review/{isbn}")
    public ResponseEntity createReview(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody ReviewCreateRequest request) {
        return bookService.createReview(token, isbn, request);
    }

    // 리뷰 전체 삭제 API
    @DeleteMapping("/deleteReview/{isbn}")
    public ResponseEntity deleteReview(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn) {
        return bookService.deleteReview(token, isbn);
    }

    // 한줄평 삭제 API
    @DeleteMapping("/deleteComment/{isbn}")
    public ResponseEntity deleteComment(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn) {
        return bookService.deleteComment(token, isbn);
    }

    // 한줄평 반응 추가 API
    @PostMapping("/reaction/{isbn}")
    public ResponseEntity reactionComment(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody CommentReactionRequest request) {
        return bookService.reactionComment(token, isbn, request);
    }

    // 한줄평 반응 수정 API
    @PatchMapping("/modifyReaction/{isbn}")
    public ResponseEntity modifyReaction(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody CommentReactionRequest request) {
        return bookService.modifyReaction(token, isbn, request);
    }

    // 한줄평 반응 삭제 API
    @DeleteMapping("/deleteReaction/{isbn}")
    public ResponseEntity deleteReaction(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody DeleteReactionRequest request) {
        return bookService.deleteReaction(token, isbn, request);
    }

    // 신고하기 API
    @PostMapping("/report/{isbn}")
    public ResponseEntity reportComment(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody ReportCommentRequest request) {
        return bookService.reportComment(token, isbn, request);
    }

    // 읽고싶은 책 등록 API
    @PostMapping("/wantToRead/{isbn}")
    public ResponseEntity wantToRead(@RequestHeader ("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody BookCreateRequest request) {
        return bookService.wantToRead(token, isbn, request);
    }

    // 읽기 시작한 날짜 변경 API
    @PatchMapping("/modifyStartDate/{isbn}")
    public ResponseEntity modifyStartDate(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody Map<String, String> startDateMap) {
        return bookService.modifyStartDate(token, isbn, startDateMap.get("startDate"));
    }

    // 마지막 읽은 날짜 변경 API
    @PatchMapping("/modifyRecentDate/{isbn}")
    public ResponseEntity modifyRecentDate(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody Map<String, String> recentDateMap) {
        return bookService.modifyRecentDate(token, isbn, recentDateMap.get("recentDate"));
    }

    // 책별 대표 위치 등록 API
    @PostMapping("/mainLocation/{isbn}")
    public ResponseEntity addMainLocation(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody LocationRequest request) {
        return bookService.addMainLocation(token, isbn, request);
    }

    // 책별 대표 위치 변경 API
    @PatchMapping("/modifyMainLocation/{isbn}")
    public ResponseEntity modifyMainLocation(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody LocationRequest request) {
        return bookService.modifyMainLocation(token, isbn, request);
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

    // 인물사전 수정 API
    @PatchMapping("/modifyPersonalDictionary/{isbn}")
    public ResponseEntity modifyPersonalDictionary(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody PersonalDictionaryRequest request) {
        return bookService.modifyPersonalDictionary(token, isbn, request);
    }

    // 인물사전 삭제 API
    @DeleteMapping("/deletePersonalDictionary/{isbn}")
    public ResponseEntity deletePersonalDictionary(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody DeletePersonalDictionaryRequest request) {
        return bookService.deletePersonalDictionary(token, isbn, request);
    }

    // 인물사전 전체조회 API
    @GetMapping("/getPersonalDictionary/{isbn}")
    public ResponseEntity getPersonalDictionary(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn) {
        return bookService.getPersonalDictionary(token, isbn);
    }

    // 메모 등록 API
    @PostMapping("/memo/{isbn}")
    public ResponseEntity addMemo(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody MemoRequest request) {
        return bookService.addMemo(token, isbn, request);
    }

    // 메모 수정 API
    @PatchMapping("/modifyMemo/{isbn}")
    public ResponseEntity modifyMemo(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody MemoRequest request) {
        return bookService.modifyMemo(token, isbn, request);
    }

    // 메모 삭제 API
    @DeleteMapping("/deleteMemo/{isbn}")
    public ResponseEntity deleteMemo(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody DeleteUuidRequest request) {
        return bookService.deleteMemo(token, isbn, request);
    }

    // 메모 전체조회 API
    @GetMapping("/getMemo/{isbn}")
    public ResponseEntity getMemo(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn) {
        return bookService.getMemo(token, isbn);
    }

    // 책갈피 등록 API
    @PostMapping("/bookmark/{isbn}")
    public ResponseEntity addBookmark(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody BookmarkRequest request) {
        return bookService.addBookmark(token, isbn, request);
    }

    // 책갈피 수정 API
    @PatchMapping("/modifyBookmark/{isbn}")
    public ResponseEntity modifyBookmark(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody BookmarkRequest request) {
        return bookService.modifyBookmark(token, isbn, request);
    }

    // 책갈피 삭제 API
    @DeleteMapping("/deleteBookmark/{isbn}")
    public ResponseEntity deleteBookmark(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn, @RequestBody DeleteUuidRequest request) {
        return bookService.deleteBookmark(token, isbn, request);
    }

    // 책갈피 전체조회 API
    @GetMapping("/getBookmark/{isbn}")
    public  ResponseEntity getBookmark(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn) {
        return  bookService.getBookmark(token, isbn);
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
    public ResponseEntity deleteRecentLocation(@RequestHeader("xAuthToken") String token, @RequestBody DeleteRecentLocationRequest request) {
        return bookService.deleteRecentLocation(token, request);
    }
}
