package com.codecozy.server.service;

import com.codecozy.server.context.StatusCode;
import com.codecozy.server.dto.request.SignUpAppleRequest;
import com.codecozy.server.dto.request.SignUpKakaoRequest;
import com.codecozy.server.dto.response.DefaultResponse;
import com.codecozy.server.dto.response.BadgeResponse;
import com.codecozy.server.dto.response.ProfileResponse;
import com.codecozy.server.dto.response.SignUpKakaoResponse;
import com.codecozy.server.entity.Badge;
import com.codecozy.server.entity.Member;
import com.codecozy.server.entity.MemberKakao;
import com.codecozy.server.repository.BadgeRepository;
import com.codecozy.server.repository.MemberKakaoRepository;
import com.codecozy.server.repository.MemberRepository;
import com.codecozy.server.security.TokenProvider;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final MemberKakaoRepository memberKakaoRepository;
    private final BadgeRepository badgeRepository;
    private final ConverterService converterService;

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

        // 카카오 유저 등록
        MemberKakao memberKakao = MemberKakao.create(member, request.email());
        memberKakaoRepository.save(memberKakao);

        // 토큰 생성
        Long memberId = member.getMemberId();
        String accessToken = tokenProvider.createAccessToken(memberId);

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, "성공", new SignUpKakaoResponse(accessToken)),
                HttpStatus.OK);
    }

    // 카카오 로그인
    public ResponseEntity<DefaultResponse> signInKakao(String email) {
        // 사용자 기가입 여부 검증
        MemberKakao memberKakao = memberKakaoRepository.findByEmail(email);

        // 기가입 유저 X
        if (memberKakao == null) {
            // 실패 응답
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.NOT_FOUND, "해당 사용자가 존재하지 않습니다."),
                    HttpStatus.NOT_FOUND);
       }
        // 기가입 유저 O
        // 토큰 생성 및 응답
        Long memberId = memberKakao.getMemberId();
        String accessToken = tokenProvider.createAccessToken(memberId);

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, "성공", new SignUpKakaoResponse(accessToken)),
                HttpStatus.OK);
    }

    // 애플 회원가입
    public ResponseEntity<DefaultResponse> signUpApple(SignUpAppleRequest request) {
        // 유저 생성 및 저장
        Member member = Member.create(request.nickname(), request.profileImageCode());
        memberRepository.save(member);
        member = memberRepository.findByNickname(request.nickname());

        // 토큰 생성
        Long memberId = member.getMemberId();

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 닉네임 변경
    public ResponseEntity<DefaultResponse> modifyNickname(String token, String nickname) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // 해당 닉네임이 이미 있다면
        if (memberRepository.findByNickname(nickname) != null) {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "사용 불가능한 닉네임입니다."),
                    HttpStatus.CONFLICT);
        }

        // 없을 시 그대로 변경 진행
        member.modifyNickname(nickname);
        memberRepository.save(member);

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 프로필 이미지 변경
    public ResponseEntity<DefaultResponse> modifyProfileImg(String token, String profileImgCode) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        member.modifyProfileImg(profileImgCode);
        memberRepository.save(member);

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 회원 탈퇴
    public ResponseEntity<DefaultResponse> deleteMember(String token) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        memberRepository.deleteById(memberId);

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 마이페이지 조회
    public ResponseEntity<DefaultResponse> getProfile(String token) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);
        List<Badge> badgeList = badgeRepository.findAllByMember(member);

        // 보낼 데이터
        String nickname = member.getNickname();
        int badgeCount = badgeList.size();
        String profileImgCode = member.getProfile();

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, "성공",
                new ProfileResponse(nickname, badgeCount, profileImgCode)),
                HttpStatus.OK);
    }

    // 배지 조회
    public ResponseEntity<DefaultResponse> getBadgeList(String token) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);
        List<Badge> badgeList = badgeRepository.findAllByMember(member);

        // 배지 목록 세팅
        List<BadgeResponse> badgeResponseList = new ArrayList<>();
        // BookCount 관련 배지
        for (int i = 0; i <= 3; i++) {
            BadgeResponse tempBadge = new BadgeResponse(i, false, null);
            badgeResponseList.add(tempBadge);
        }
        // finisher 관련 배지
        for (int i = 10; i <= 13; i++) {
            BadgeResponse tempBadge = new BadgeResponse(i, false, null);
            badgeResponseList.add(tempBadge);
        }
        // record MVP 관련 배지
        for (int i = 20; i <= 22; i++) {
            BadgeResponse tempBadge = new BadgeResponse(i, false, null);
            badgeResponseList.add(tempBadge);
        }
        // review master 관련 배지
        for (int i = 30; i <= 32; i++) {
            BadgeResponse tempBadge = new BadgeResponse(i, false, null);
            badgeResponseList.add(tempBadge);
        }
        // 배지 코드만 빼기
        List<Integer> badgeCodeList = new ArrayList<>();
        for (BadgeResponse badgeResponse : badgeResponseList) {
            badgeCodeList.add(badgeResponse.getBadgeCode());
        }

        // 획득한 배지만 표시
        for (Badge badge : badgeList) {
            if (badgeCodeList.contains(badge.getBadgeCode())) {
                int index = badgeCodeList.indexOf(badge.getBadgeCode());
                badgeResponseList.get(index).setIsGotBadge(true);       // 해당 배지 획득 표시

                String dateStr = converterService.dateToString(badge.getDate());
                badgeResponseList.get(index).setDate(dateStr);  // 획득 날짜 담기
            }
        }

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, "성공", badgeResponseList),
                HttpStatus.OK);
    }
}
