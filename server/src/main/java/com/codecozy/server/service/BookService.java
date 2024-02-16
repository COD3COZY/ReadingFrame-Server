package com.codecozy.server.service;

import com.codecozy.server.context.StatusCode;
import com.codecozy.server.dto.request.ReactionCommentRequest;
import com.codecozy.server.dto.request.ReportCommentRequest;
import com.codecozy.server.dto.request.ReadingBookCreateRequest;
import com.codecozy.server.dto.request.ReviewCreateRequest;
import com.codecozy.server.dto.response.DefaultResponse;
import com.codecozy.server.entity.*;
import com.codecozy.server.repository.*;
import com.codecozy.server.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookRecordRepository bookRecordRepository;
    private final MemberRepository memberRepository;
    private final LocationRepository locationRepository;
    private final MemberLocationRepository memberLocationRepository;
    private final BookReviewRepository bookReviewRepository;
    private final BookReviewReactionRepository bookReviewReactionRepository;
    private final BookReviewReviewerRepository bookReviewReviewerRepository;
    private final KeywordReviewRepository keywordReviewRepository;
    private final TokenProvider tokenProvider;

    // 사용자가 독서노트 추가 시 실행 (책 등록, 위치 등록, 독서노트 등록, 최근 검색 위치 등록)
    public ResponseEntity<DefaultResponse> createBook(String token, String isbn, ReadingBookCreateRequest request) {
        // 사용자 받아오기
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // 자주 사용되는 객체 재사용 용도로 선언
        Book book = new Book();
        LocationList locationList = new LocationList();

        // isbn을 이용해 책 등록이 중복되었는지 검사
        if (bookRepository.findByIsbn(isbn) == null) {
            // 등록되지 않은 책이면 새로 등록
            book = Book.create(isbn, request.bookInformation().cover(), request.bookInformation().title(), request.bookInformation().author(), request.bookInformation().category(), Integer.parseInt(request.bookInformation().totalPage()));
            bookRepository.save(book);
        }

        long latitude = Long.parseLong(request.mainLocation().latitude());
        long longitude = Long.parseLong(request.mainLocation().longitude());

        // latitude, longitude를 이용해 이미 등록한 위치인지 검사
        if (locationRepository.findByLatitudeAndLongitude(latitude, longitude) == null) {
            // 등록하지 않은 위치면 새로 등록
            locationList = LocationList.create(request.mainLocation().placeName(), request.mainLocation().address(), latitude, longitude);
            locationRepository.save(locationList);
        }

        locationList = locationRepository.findByLatitudeAndLongitude(latitude, longitude);
        // 최근 위치 검색 기록에 없으면 추가
        if (memberLocationRepository.findByMemberAndLocationList(member, locationList) == null) {
            // 레코드가 5개 이상인지 확인, 5개 이상이면 날짜 순으로 정렬 후 가장 오래된 레코드 삭제
            if (memberLocationRepository.count() >= 5) {
                List<MemberLocation> memberLocationList = memberLocationRepository.findAllByOrderByDateAsc();
                memberLocationRepository.delete(memberLocationList.get(0));
            }

            // 검색 기록에 추가
            MemberLocation memberLocation = MemberLocation.create(member, locationList, LocalDate.now().toString());
            memberLocationRepository.save(memberLocation);
        }

        // memberId와 isbn을 이용해 사용자별 리뷰 등록 책이 중복되었는지 검사
        if (bookRecordRepository.findByMemberAndBook(member, book) != null) {
            // 이미 등록한 책이면
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "이미 등록한 도서입니다."),
                    HttpStatus.CONFLICT);
        }

        // isbn으로 책 찾고, latitude와 longitude로 위치 찾아서 bookRecord 생성에 사용
        book = bookRepository.findByIsbn(isbn);
        locationList = locationRepository.findByLatitudeAndLongitude(latitude, longitude);

        // 등록하지 않은 책이면 등록
        BookRecord bookRecord = BookRecord.create(member, book, request.readingStatus(), request.bookType(), locationList, request.isMine(), request.isHidden(), request.startDate(), request.recentDate());
        bookRecordRepository.save(bookRecord);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 한줄평 신고
    public ResponseEntity<DefaultResponse> reportComment(String token, String isbn, ReportCommentRequest request) {
        // 사용자 받아오기
        // 문제! 이러면 본인이 등록한 한줄평만 검색할 수 있음. 사용자 닉네임을 받아와서 닉네임으로 한줄평 사용자 찾도록 수정
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 사용자와 책을 이용한 검색으로 bookReview 테이블에서 한줄평 찾기
        BookReview bookReview = bookReviewRepository.findByMemberAndBook(member, book);

        if (bookReview == null) {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "해당 한줄평이 없습니다."),
                    HttpStatus.CONFLICT);
        }

        // 한줄평 객체를 이용한 검색으로 bookReviewReaction 테이블에서 한줄평에 대한 반응 레코드 검색
        BookReviewReaction bookReviewReaction = bookReviewReactionRepository.findByBookReview(bookReview);

        // 해당 한줄평에 대한 반응 레코드가 없으면 새로 생성
        if (bookReviewReaction == null) {
            bookReviewReaction = BookReviewReaction.create(bookReview, 0, 0, 0, 0, 0, 0, 0);
            bookReviewReactionRepository.save(bookReviewReaction);
            bookReviewReaction = bookReviewReactionRepository.findByBookReview(bookReview);
        }

        // 한줄평에 반응을 등록한 유저인지 검색
        BookReviewReviewer bookReviewReviewer = bookReviewReviewerRepository.findByBookReview(bookReview);

        // 한줄평 반응을 처음 남기는 유저라면
        if (bookReviewReviewer == null) {
            // 모든 반응, 신고 플래그가 false인 인스턴스 생성 및 저장
            bookReviewReviewer = BookReviewReviewer.create(bookReview, member);
            bookReviewReviewerRepository.save(bookReviewReviewer);

            // 검색해서 저장 후 카운트 수정에 사용
            bookReviewReviewer = bookReviewReviewerRepository.findByBookReview(bookReview);
        }

        // 0이면 부적절한 리뷰, 1이면 스팸성 리뷰 카운트 올리고, (한줄평을 등록한 유저) 테이블에서 반응 여부 수정
        if (request.reportType() == 0 && !bookReviewReviewer.isReportHateful()) {
            bookReviewReaction.setReportHatefulCount();
            bookReviewReviewer.setIsReportHatefulReverse();
        }
        else if (request.reportType() == 1 && !bookReviewReviewer.isReportSpam()) {
            bookReviewReaction.setReportSpamCountCount();
            bookReviewReviewer.setIsReportSpamReverse();
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    public ResponseEntity<DefaultResponse> reactionComment(String token, String isbn, ReactionCommentRequest request) {
        // 사용자 받아오기
        // 문제! 이러면 본인이 등록한 한줄평만 검색할 수 있음. 사용자 닉네임을 받아와서 닉네임으로 한줄평 사용자 찾도록 수정
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 사용자와 책을 이용한 검색으로 bookReview 테이블에서 한줄평 찾기
        BookReview bookReview = bookReviewRepository.findByMemberAndBook(member, book);

        if (bookReview == null) {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "해당 한줄평이 없습니다."),
                    HttpStatus.CONFLICT);
        }

        // 한줄평 객체를 이용한 검색으로 bookReviewReaction 테이블에서 한줄평에 대한 반응 레코드 검색
        BookReviewReaction bookReviewReaction = bookReviewReactionRepository.findByBookReview(bookReview);

        // 해당 한줄평에 대한 반응 레코드가 없으면 새로 생성
        if (bookReviewReaction == null) {
            bookReviewReaction = BookReviewReaction.create(bookReview, 0, 0, 0, 0, 0, 0, 0);
            bookReviewReactionRepository.save(bookReviewReaction);
            bookReviewReaction = bookReviewReactionRepository.findByBookReview(bookReview);
        }

        // 한줄평에 반응을 등록한 유저인지 검색
        BookReviewReviewer bookReviewReviewer = bookReviewReviewerRepository.findByBookReview(bookReview);

        // 한줄평 반응을 처음 남기는 유저라면
        if (bookReviewReviewer == null) {
            // 모든 반응, 신고 플래그가 false인 인스턴스 생성 및 저장
            bookReviewReviewer = BookReviewReviewer.create(bookReview, member);
            bookReviewReviewerRepository.save(bookReviewReviewer);

            // 검색해서 저장 후 카운트 수정에 사용
            bookReviewReviewer = bookReviewReviewerRepository.findByBookReview(bookReview);
        }
        
        // 코드에 맞는 반응 카운트 올리고, BOOK_REVIEW_REVIEWER (한줄평을 등록한 유저) 테이블에서 반응 여부 수정
        if (request.commentReaction() == 0 && !bookReviewReviewer.isHeart()) {
            bookReviewReaction.setHeartCount();
            bookReviewReviewer.setIsHeartReverse();
        }
        else if (request.commentReaction() == 1 && !bookReviewReviewer.isGood()) {
            bookReviewReaction.setGoodCount();
            bookReviewReviewer.setIsGoodReverse();
        }
        else if (request.commentReaction() == 2 && !bookReviewReviewer.isWow()) {
            bookReviewReaction.setWowCount();
            bookReviewReviewer.setIsWowReverse();
        }
        else if (request.commentReaction() == 3 && !bookReviewReviewer.isSad()) {
            bookReviewReaction.setSadCount();
            bookReviewReviewer.setIsSadReverse();
        }
        else if (request.commentReaction() == 4 && !bookReviewReviewer.isAngry()) {
            bookReviewReaction.setAngryCount();
            bookReviewReviewer.setIsAngryReverse();
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 리뷰 작성 API (키워드, 선택 리뷰, 한줄평 각 테이블에 추가)
    public ResponseEntity<DefaultResponse> createReview(String token, String isbn, ReviewCreateRequest request) {
        // 사용자 받아오기
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 선택 리뷰가 있으면 레코드 추가
        if (!request.select().isEmpty()) {
            String selected = "";
            for (int id = 0; id < request.select().size(); id++) {
                selected += request.select().get(id);
            }

            KeywordReview keywordReview = KeywordReview.create(member, book, selected);
            keywordReviewRepository.save(keywordReview);
        }

        // 한줄평이 있으면 레코드 추가
        if (!request.comment().isEmpty()) {
            BookReview bookReview = BookReview.create(member, book, request.comment());
            bookReviewRepository.save(bookReview);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }
}
