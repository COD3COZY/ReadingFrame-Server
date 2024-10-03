package com.codecozy.server.controller;

import com.codecozy.server.dto.request.*;
import com.codecozy.server.security.TokenProvider;
import com.codecozy.server.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/book")
@RequiredArgsConstructor
public class BookController {
    private final TokenProvider tokenProvider;
    private final BookService bookService;

    // 책 등록 API
    @PostMapping("/create/{isbn}")
    public ResponseEntity createBook(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn,
                                     @RequestBody ReadingBookCreateRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.createBook(memberId, isbn, request);
    }

    // 책 삭제 API
    @DeleteMapping("/delete/{isbn}")
    public ResponseEntity deleteBook(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.deleteBook(memberId, isbn);
    }

    // 책별 독서노트 조회 API
    @GetMapping("/readingNote/{isbn}")
    public ResponseEntity getReadingNote(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.getReadingNote(memberId, isbn);
    }

    // 독서상태 변경 API
    @PatchMapping("/readingStatus/{isbn}")
    public ResponseEntity modifyReadingStatus(@RequestHeader("xAuthToken") String token,
                                              @PathVariable("isbn") String isbn,
                                              @RequestBody Map<String, Integer> readingStatusMap) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.modifyReadingStatus(memberId, isbn, readingStatusMap.get("readingStatus"));
    }

    // 소장여부 변경 API
    @PatchMapping("/isMine/{isbn}")
    public ResponseEntity modifyIsMine(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn,
                                       @RequestBody Map<String, Boolean> isMineMap) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.modifyIsMine(memberId, isbn, isMineMap.get("isMine"));
    }

    // 책 유형 변경 API
    @PatchMapping("/bookType/{isbn}")
    public ResponseEntity modifyBookType(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn,
                                         @RequestBody Map<String, Integer> bookTypeMap) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.modifyBookType(memberId, isbn, bookTypeMap.get("bookType"));
    }

    // 읽은 페이지 변경 API
    @PatchMapping("/page/{isbn}")
    public ResponseEntity modifyReadingPage(@RequestHeader("xAuthToken") String token,
                                            @PathVariable("isbn") String isbn, @RequestBody ModifyPageRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.modifyReadingPage(memberId, isbn, request);
    }

    // 도서정보 초기 조회 API
    @GetMapping("/bookDetail/{isbn}")
    public ResponseEntity searchBookDetail(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn) throws IOException {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.searchBookDetail(memberId, isbn);
    }

    // 한줄평 추가 조회 API
    @GetMapping("/commentDetail")
    public ResponseEntity commentDetail(@RequestHeader("xAuthToken") String token,
                                        @RequestParam("isbn") String isbn,
                                        @RequestParam("orderNumber") Integer orderNumber,
                                        @RequestParam("orderType") Boolean orderType) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        CommentDetailRequest request = new CommentDetailRequest(orderNumber, orderType);

        return bookService.commentDetail(memberId, isbn, request);
    }

    // 리뷰 작성 API
    @PostMapping("/review/{isbn}")
    public ResponseEntity createReview(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn,
                                       @RequestBody ReviewCreateRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.createReview(memberId, isbn, request);
    }

    // 리뷰 전체 수정 API
    @PatchMapping("/modifyReview/{isbn}")
    public ResponseEntity modifyReview(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn,
                                       @RequestBody ReviewCreateRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.modifyReview(memberId, isbn, request);
    }

    // 리뷰 전체 삭제 API
    @DeleteMapping("/deleteReview/{isbn}")
    public ResponseEntity deleteReview(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.deleteReview(memberId, isbn);
    }

    // 한줄평 삭제 API
    @DeleteMapping("/deleteComment/{isbn}")
    public ResponseEntity deleteComment(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.deleteComment(memberId, isbn);
    }

    // 한줄평 반응 추가 API
    @PostMapping("/reaction/{isbn}")
    public ResponseEntity reactionComment(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn,
                                          @RequestBody CommentReactionRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.reactionComment(memberId, isbn, request);
    }

    // 한줄평 반응 수정 API
    @PatchMapping("/modifyReaction/{isbn}")
    public ResponseEntity modifyReaction(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn,
                                         @RequestBody CommentReactionRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.modifyReaction(memberId, isbn, request);
    }

    // 한줄평 반응 삭제 API
    @DeleteMapping("/deleteReaction/{isbn}")
    public ResponseEntity deleteReaction(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn,
                                         @RequestBody DeleteReactionRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.deleteReaction(memberId, isbn, request);
    }

    // 신고하기 API
    @PostMapping("/report/{isbn}")
    public ResponseEntity reportComment(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn,
                                        @RequestBody ReportCommentRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.reportComment(memberId, isbn, request);
    }

    // 읽고싶은 책 등록 API
    @PostMapping("/wantToRead/{isbn}")
    public ResponseEntity wantToRead(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn,
                                     @RequestBody BookCreateRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.wantToRead(memberId, isbn, request);
    }

    // 읽기 시작한 날짜 변경 API
    @PatchMapping("/modifyStartDate/{isbn}")
    public ResponseEntity modifyStartDate(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn,
                                          @RequestBody Map<String, String> startDateMap) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.modifyStartDate(memberId, isbn, startDateMap.get("startDate"));
    }

    // 마지막 읽은 날짜 변경 API
    @PatchMapping("/modifyRecentDate/{isbn}")
    public ResponseEntity modifyRecentDate(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn,
                                           @RequestBody Map<String, String> recentDateMap) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.modifyRecentDate(memberId, isbn, recentDateMap.get("recentDate"));
    }

    // 책별 대표 위치 등록 API
    @PostMapping("/mainLocation/{isbn}")
    public ResponseEntity addMainLocation(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn,
                                          @RequestBody LocationRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.addMainLocation(memberId, isbn, request);
    }

    // 책별 대표 위치 변경 API
    @PatchMapping("/patchMainLocation/{isbn}")
    public ResponseEntity modifyMainLocation(@RequestHeader("xAuthToken") String token,
                                             @PathVariable("isbn") String isbn, @RequestBody LocationRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.patchMainLocation(memberId, isbn, request);
    }

    // 책별 대표 위치 삭제 API
    @DeleteMapping("/deleteMainLocation/{isbn}")
    public ResponseEntity deleteMainLocation(@RequestHeader("xAuthToken") String token,
                                             @PathVariable("isbn") String isbn) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.deleteMainLocation(memberId, isbn);
    }

    // 인물사전 등록 API
    @PostMapping("/personalDictionary/{isbn}")
    public ResponseEntity addpersonalDictionary(@RequestHeader("xAuthToken") String token,
                                                @PathVariable("isbn") String isbn,
                                                @RequestBody PersonalDictionaryRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.addpersonalDictionary(memberId, isbn, request);
    }

    // 인물사전 수정 API
    @PatchMapping("/modifyPersonalDictionary/{isbn}")
    public ResponseEntity modifyPersonalDictionary(@RequestHeader("xAuthToken") String token,
                                                   @PathVariable("isbn") String isbn,
                                                   @RequestBody PersonalDictionaryRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.modifyPersonalDictionary(memberId, isbn, request);
    }

    // 인물사전 삭제 API
    @DeleteMapping("/deletePersonalDictionary/{isbn}")
    public ResponseEntity deletePersonalDictionary(@RequestHeader("xAuthToken") String token,
                                                   @PathVariable("isbn") String isbn,
                                                   @RequestBody DeletePersonalDictionaryRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.deletePersonalDictionary(memberId, isbn, request);
    }

    // 인물사전 전체조회 API
    @GetMapping("/getPersonalDictionary/{isbn}")
    public ResponseEntity getPersonalDictionary(@RequestHeader("xAuthToken") String token,
                                                @PathVariable("isbn") String isbn) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.getPersonalDictionary(memberId, isbn);
    }

    // 메모 등록 API
    @PostMapping("/memo/{isbn}")
    public ResponseEntity addMemo(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn,
                                  @RequestBody MemoRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.addMemo(memberId, isbn, request);
    }

    // 메모 수정 API
    @PatchMapping("/modifyMemo/{isbn}")
    public ResponseEntity modifyMemo(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn,
                                     @RequestBody MemoRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.modifyMemo(memberId, isbn, request);
    }

    // 메모 삭제 API
    @DeleteMapping("/deleteMemo/{isbn}")
    public ResponseEntity deleteMemo(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn,
                                     @RequestBody DeleteUuidRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.deleteMemo(memberId, isbn, request);
    }

    // 메모 전체조회 API
    @GetMapping("/getMemo/{isbn}")
    public ResponseEntity getMemo(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.getMemo(memberId, isbn);
    }

    // 책갈피 등록 API
    @PostMapping("/bookmark/{isbn}")
    public ResponseEntity addBookmark(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn,
                                      @RequestBody BookmarkRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.addBookmark(memberId, isbn, request);
    }

    // 책갈피 수정 API
    @PatchMapping("/modifyBookmark/{isbn}")
    public ResponseEntity modifyBookmark(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn,
                                         @RequestBody BookmarkRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.modifyBookmark(memberId, isbn, request);
    }

    // 책갈피 삭제 API
    @DeleteMapping("/deleteBookmark/{isbn}")
    public ResponseEntity deleteBookmark(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn,
                                         @RequestBody DeleteUuidRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.deleteBookmark(memberId, isbn, request);
    }

    // 책갈피 전체조회 API
    @GetMapping("/getBookmark/{isbn}")
    public ResponseEntity getBookmark(@RequestHeader("xAuthToken") String token, @PathVariable("isbn") String isbn) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.getBookmark(memberId, isbn);
    }

    // 전체 위치 조회 API
    @GetMapping("/getAllLocation/{orderNumber}")
    public ResponseEntity getAllLocation(@RequestHeader("xAuthToken") String token,
                                         @PathVariable("orderNumber") Integer orderNumber) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.getAllLocation(memberId, orderNumber);
    }

    // 최근 등록 위치 조회 API
    @GetMapping("/getRecentLocation")
    public ResponseEntity getRecentLocation(@RequestHeader("xAuthToken") String token) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.getRecentLocation(memberId);
    }

    // 최근 등록 위치 삭제 API
    @DeleteMapping("/deleteRecentLocation")
    public ResponseEntity deleteRecentLocation(@RequestHeader("xAuthToken") String token,
                                               @RequestBody DeleteRecentLocationRequest request) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.deleteRecentLocation(memberId, request);
    }

    // 지도 마크 조회 API
    @GetMapping("/getAllMarker")
    public ResponseEntity getAllMarker(@RequestHeader("xAuthToken") String token) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookService.getAllMarker(memberId);
    }

    // 마크 세부 조회 API
    @GetMapping("/getMarkDetail")
    public ResponseEntity getMarkDetail(@RequestHeader("xAuthToken") String token,
                                        @RequestParam("locationId") Long locationId,
                                        @RequestParam("orderNumber") Integer orderNumber) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        MarkDetailRequest request = new MarkDetailRequest(locationId, orderNumber);

        return bookService.getMarkDetail(memberId, request);
    }
}
