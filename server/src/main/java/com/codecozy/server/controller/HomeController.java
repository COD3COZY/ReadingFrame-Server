package com.codecozy.server.controller;

import com.codecozy.server.security.TokenProvider;
import com.codecozy.server.service.HomeService;
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

    // 읽고 싶은 책 조회
    @GetMapping("/wantToRead")
    public ResponseEntity getWantToReadBooks(@RequestHeader("xAuthToken") String token) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return homeService.getWantToReadBooks(memberId);
    }

    // 읽고 있는 책 숨기기 & 꺼내기
    @PatchMapping("/hidden/{ISBN}")
    public ResponseEntity modifyHidden(@RequestHeader("xAuthToken") String token,
            @PathVariable("ISBN") String isbn, @RequestBody Map<String, Boolean> isHiddenMap) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return homeService.modifyHidden(memberId, isbn, isHiddenMap.get("isHidden"));
    }
}
