package com.codecozy.server.controller;

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
    private final BookshelfService bookshelfService;

    // 책장 초기 조회
    @GetMapping("/{bookshelfType}")
    public ResponseEntity getAllBookshelf(@RequestHeader("xAuthToken") String token,
                                          @PathVariable("bookshelfType") int bookshelfType) {
        return bookshelfService.getAllBookshelf(token, bookshelfType);
    }
}
