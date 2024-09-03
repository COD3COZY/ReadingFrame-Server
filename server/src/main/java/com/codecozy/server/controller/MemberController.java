package com.codecozy.server.controller;

import com.codecozy.server.dto.request.SignUpKakaoRequest;
import com.codecozy.server.service.MemberService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    // 닉네임 중복검사
    @GetMapping("/nickname/{nickname}")
    public ResponseEntity validateNickname(@PathVariable("nickname") String nickname) {
        return memberService.validateNickname(nickname);
    }

    // 카카오 회원가입
    @PostMapping("/sign-up/kakao")
    public ResponseEntity signUpKakao(@RequestBody SignUpKakaoRequest request) {
        return memberService.signUpKakao(request);
    }

    // 카카오 로그인
    @PostMapping("sign-in/kakao")
    public ResponseEntity signInKakao(@RequestBody Map<String, String> emailMap) {
        return memberService.signInKakao(emailMap.get("email"));
    }

    // 닉네임 변경
    @PatchMapping("/nickname")
    public ResponseEntity modifyNickname(@RequestHeader("xAuthToken") String token,
                                         @RequestBody Map<String, String> nicknameMap) {
        return memberService.modifyNickname(token, nicknameMap.get("nickname"));
    }

    // 프로필 이미지 변경
    @PatchMapping("/profileImageCode")
    public ResponseEntity modifyProfileImg(@RequestHeader("xAuthToken") String token,
                                           @RequestBody Map<String, String> profileCodeMap) {
        return memberService.modifyProfileImg(token, profileCodeMap.get("profileImageCode"));
    }

    // 회원 탈퇴
    @DeleteMapping("/deleteAccount")
    public ResponseEntity deleteMember(@RequestHeader("xAuthToken") String token) {
        return memberService.deleteMember(token);
    }

    // 마이페이지 조회
    @GetMapping("/profile")
    public ResponseEntity getProfile(@RequestHeader("xAuthToken") String token) {
        return memberService.getProfile(token);
    }

    // 얻은 배지 조회
    @GetMapping("/badge")
    public ResponseEntity getBadgeList(@RequestHeader("xAuthToken") String token) {
        return memberService.getBadgeList(token);
    }
}
