package com.codecozy.server.controller;

import com.codecozy.server.security.TokenProvider;
import com.codecozy.server.service.HomeService;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {
    private final TokenProvider tokenProvider;
    private final HomeService homeService;

    // 메인 화면 조회
    @GetMapping("")
    public ResponseEntity getMainPage(@RequestHeader("xAuthToken") String token) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return homeService.getMainPage(memberId);
    }

    // 검색
    @GetMapping("/search/{searchText}")
    public ResponseEntity getSearchList(@RequestHeader("xAuthToken") String token,
            @PathVariable("searchText") String searchText) throws IOException {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return homeService.getSearchList(memberId, searchText);
    }

    // 읽고 싶은 책 조회
    @GetMapping("/want-to-read")
    public ResponseEntity getWantToReadBooks(@RequestHeader("xAuthToken") String token) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return homeService.getWantToReadBooks(memberId);
    }

    // 읽고 있는 책 조회
    @GetMapping("/reading")
    public ResponseEntity getReadingBooks(@RequestHeader("xAuthToken") String token) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return homeService.getReadingBooks(memberId);
    }

    // 다 읽은 책 조회
    @GetMapping("/finish-read")
    public ResponseEntity getFinishReadBooks(@RequestHeader("xAuthToken") String token) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return homeService.getFinishReadBooks(memberId);
    }

    // 읽고 있는 책 숨기기 & 꺼내기
    @PatchMapping("/hidden/{isbn}")
    public ResponseEntity modifyHidden(@RequestHeader("xAuthToken") String token,
            @PathVariable("isbn") String isbn, @RequestBody Map<String, Boolean> isHiddenMap) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return homeService.modifyHidden(memberId, isbn, isHiddenMap.get("isHidden"));
    }
}
