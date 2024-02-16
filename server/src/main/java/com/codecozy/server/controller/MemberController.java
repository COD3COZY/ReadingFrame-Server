package com.codecozy.server.controller;

import com.codecozy.server.dto.request.SignUpKakaoRequest;
import com.codecozy.server.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    // 카카오 회원가입
    @PostMapping("/sign-up/kakao")
    public ResponseEntity signUpKakao(@RequestBody SignUpKakaoRequest request) {
        return memberService.signUpKakao(request);
    }
}
