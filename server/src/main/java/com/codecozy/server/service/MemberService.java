package com.codecozy.server.service;

import com.codecozy.server.context.StatusCode;
import com.codecozy.server.dto.request.SignUpKakaoRequest;
import com.codecozy.server.dto.response.DefaultResponse;
import com.codecozy.server.dto.response.SignUpKakaoResponse;
import com.codecozy.server.entity.Member;
import com.codecozy.server.repository.MemberRepository;
import com.codecozy.server.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    // 닉네임 중복 검증
    public ResponseEntity<DefaultResponse> validateNickname(String nickname) {
        Member member = memberRepository.findByNickname(nickname);

        if (member != null) {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "사용 불가능한 닉네임입니다."),
                    HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, "사용 가능한 닉네임입니다."),
                HttpStatus.OK);
    }

    // 카카오 회원가입
    public ResponseEntity<DefaultResponse> signUpKakao(SignUpKakaoRequest request) {
        // 유저 생성 및 저장
        Member member = Member.create(request.nickname(), request.profileImageCode());
        memberRepository.save(member);
        member = memberRepository.findByNickname(request.nickname());

        // 토큰 생성
        Long memberId = member.getMemberId();
        String accessToken = tokenProvider.createAccessToken(memberId);

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, "성공", new SignUpKakaoResponse(accessToken)),
                HttpStatus.OK);
    }
}
