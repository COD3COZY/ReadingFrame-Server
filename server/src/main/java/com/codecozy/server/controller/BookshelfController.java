package com.codecozy.server.controller;

import com.codecozy.server.security.TokenProvider;
import com.codecozy.server.service.BookshelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookshelf")
@RequiredArgsConstructor
public class BookshelfController {
    private final TokenProvider tokenProvider;
    private final BookshelfService bookshelfService;

    // 책장 초기 조회
    @GetMapping("/{type}")
    public ResponseEntity getAllBookshelf(@RequestHeader("xAuthToken") String token,
                                          @PathVariable("type") int bookshelfType) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookshelfService.getAllBookshelf(memberId, bookshelfType);
    }

    // 책장 리스트용 조회
    @GetMapping("/{code}/detail")
    public ResponseEntity getDetailBookshelf(@RequestHeader("xAuthToken") String token,
                                             @PathVariable("code") String bookshelfCode) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return bookshelfService.getDetailBookshelf(memberId, bookshelfCode);
    }
}
