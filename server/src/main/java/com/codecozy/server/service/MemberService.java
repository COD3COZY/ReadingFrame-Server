package com.codecozy.server.service;

import com.codecozy.server.cache.MemberCacheManager;
import com.codecozy.server.context.ResponseMessages;
import com.codecozy.server.context.StatusCode;
import com.codecozy.server.dto.request.SignInAppleRequest;
import com.codecozy.server.dto.request.SignUpAppleRequest;
import com.codecozy.server.dto.request.SignUpKakaoRequest;
import com.codecozy.server.dto.response.DefaultResponse;
import com.codecozy.server.dto.response.BadgeResponse;
import com.codecozy.server.dto.response.ProfileResponse;
import com.codecozy.server.dto.response.SignUpKakaoResponse;
import com.codecozy.server.entity.Badge;
import com.codecozy.server.entity.Member;
import com.codecozy.server.entity.MemberApple;
import com.codecozy.server.entity.MemberKakao;
import com.codecozy.server.repository.MemberAppleRepository;
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
    private final MemberAppleRepository memberAppleRepository;
    private final ConverterService converterService;
    private final AppleTokenService appleTokenService;

    private final MemberCacheManager cacheManager;

    // 닉네임 중복 검증
    public ResponseEntity<DefaultResponse> validateNickname(String nickname) {
        Member member = memberRepository.findByNickname(nickname);

        if (member != null) {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, ResponseMessages.CONFLICT_NICKNAME.get()),
                    HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, ResponseMessages.OK_NICKNAME.get()),
                HttpStatus.OK);
    }

    // 카카오 회원가입
    public ResponseEntity<DefaultResponse> signUpKakao(SignUpKakaoRequest request) {
        // 유저 생성 및 저장
        Member member = Member.create(request.nickname(), request.profileImageCode());
        member = memberRepository.save(member);

        // 카카오 유저 등록
        MemberKakao memberKakao = MemberKakao.create(member, request.email());
        memberKakaoRepository.save(memberKakao);

        // 토큰 생성
        Long memberId = member.getMemberId();
        String accessToken = tokenProvider.createAccessToken(memberId);

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(),
                new SignUpKakaoResponse(accessToken)),
                HttpStatus.OK);
    }

    // 카카오 로그인
    public ResponseEntity<DefaultResponse> signInKakao(String email) {
        // 사용자 기가입 여부 검증
        MemberKakao memberKakao = memberKakaoRepository.findByEmail(email);

        // 기가입 유저 X
        if (memberKakao == null) {
            // 실패 응답
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_USER.get()),
                    HttpStatus.NOT_FOUND);
       }
        // 기가입 유저 O
        // 토큰 생성 및 응답
        Long memberId = memberKakao.getMemberId();
        String accessToken = tokenProvider.createAccessToken(memberId);

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(),
                new SignUpKakaoResponse(accessToken)),
                HttpStatus.OK);
    }

    // 애플 회원가입
    public ResponseEntity<DefaultResponse> signUpApple(SignUpAppleRequest request) throws Exception {
        // idToken 유효성 검증(JWK 확인, Claim 확인)
        if (!appleTokenService.isValid(request.idToken())) {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.INVALID_ID_TOKEN.get()),
                    HttpStatus.NOT_FOUND);
        }

        // 이미 가입한 유저인지 확인
        MemberApple signedUpUser = memberAppleRepository.findByUserIdentifier(request.userIdentifier());
        if (signedUpUser != null) {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, ResponseMessages.CONFLICT_USER.get()),
                    HttpStatus.CONFLICT);
        }

        // 유저 생성 및 저장
        Member member = Member.create(request.nickname(), request.profile());
        member = memberRepository.save(member);

        // 애플 유저 등록
        MemberApple memberApple = MemberApple.create(member, request.userIdentifier(), request.idToken());
        memberAppleRepository.save(memberApple);

        // 토큰 생성
        Long memberId = member.getMemberId();
        String accessToken = tokenProvider.createAccessToken(memberId);

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(),
                new SignUpKakaoResponse(accessToken)),
                HttpStatus.OK);
    }

    // 애플 로그인
    public ResponseEntity<DefaultResponse> signInApple(SignInAppleRequest request) {
        // idToken 유효성 검증
        if (!appleTokenService.isValid(request.idToken())) {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.INVALID_ID_TOKEN.get()),
                    HttpStatus.NOT_FOUND);
        }

        // DB에 저장된 회원인지 확인
        MemberApple memberApple = memberAppleRepository.findByUserIdentifier(request.userIdentifier());

        if (memberApple == null) {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_USER.get()),
                    HttpStatus.NOT_FOUND);
        }

        // 토큰 생성 및 응답
        Long memberId = memberApple.getMember().getMemberId();
        String accessToken = tokenProvider.createAccessToken(memberId);

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(),
                new SignUpKakaoResponse(accessToken)),
                HttpStatus.OK);
    }

    // 닉네임 변경
    public ResponseEntity<DefaultResponse> modifyNickname(String token, String nickname) {
        // 해당 닉네임이 이미 있다면
        if (memberRepository.findByNickname(nickname) != null) {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, ResponseMessages.CONFLICT_NICKNAME.get()),
                    HttpStatus.CONFLICT);
        }

        // 없을 시 그대로 변경 진행
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        member.modifyNickname(nickname);
        memberRepository.save(member);

        cacheManager.remove(memberId);
        cacheManager.put(memberId, member);

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 프로필 이미지 변경
    public ResponseEntity<DefaultResponse> modifyProfileImg(String token, String profileImgCode) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        member.modifyProfileImg(profileImgCode);
        memberRepository.save(member);

        cacheManager.remove(memberId);
        cacheManager.put(memberId, member);

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 회원 탈퇴
    public ResponseEntity<DefaultResponse> deleteMember(String token) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        cacheManager.remove(memberId);

        // 소셜 정보 우선 삭제
        memberKakaoRepository.deleteById(memberId);
        memberAppleRepository.deleteById(memberId);

        // 사용자 정보 삭제
        memberRepository.deleteById(memberId);

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 마이페이지 조회
    public ResponseEntity<DefaultResponse> getProfile(String token) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = getMemberById(memberId);
        List<Badge> badgeList = member.getBadges();

        // 보낼 데이터
        String nickname = member.getNickname();
        int badgeCount = badgeList.size();
        String profileImgCode = member.getProfile();

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(),
                new ProfileResponse(nickname, badgeCount, profileImgCode)),
                HttpStatus.OK);
    }

    // 배지 조회
    public ResponseEntity<DefaultResponse> getBadgeList(String token) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = getMemberById(memberId);
        List<Badge> badgeList = member.getBadges();

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

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(), badgeResponseList),
                HttpStatus.OK);
    }

    private Member getMemberById(Long memberId) {
        Member member = cacheManager.get(memberId);
        if (member != null){
            return member;
        }

        // 캐시에 없으면
        member = memberRepository.findByMemberId(memberId);
        cacheManager.put(memberId, member);
        return member;
    }
}
