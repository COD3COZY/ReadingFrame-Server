package com.codecozy.server.controller;

import com.codecozy.server.service.HomeService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final HomeService homeService;

    // 읽고 있는 책 숨기기 & 꺼내기
    @PatchMapping("/hidden/{ISBN}")
    public ResponseEntity modifyHidden(@RequestHeader("xAuthToken") String token,
            @PathVariable("ISBN") String isbn, @RequestBody Map<String, Boolean> isHiddenMap) {

        return homeService.modifyHidden(token, isbn, isHiddenMap.get("isHidden"));
    }
}
