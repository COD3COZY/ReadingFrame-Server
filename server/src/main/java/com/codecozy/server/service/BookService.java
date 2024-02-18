package com.codecozy.server.service;

import com.codecozy.server.context.StatusCode;
import com.codecozy.server.dto.request.*;
import com.codecozy.server.dto.response.*;
import com.codecozy.server.entity.*;
import com.codecozy.server.repository.*;
import com.codecozy.server.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
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
    private final PersonalDictionaryRepository personalDictionaryRepository;
    private final MemoRepository memoRepository;
    private final BookmarkRepository bookmarkRepository;
    private final TokenProvider tokenProvider;

    // 사용자가 독서노트 추가 시 실행 (책 등록, 위치 등록, 독서노트 등록, 최근 검색 위치 등록)
    public ResponseEntity<DefaultResponse> createBook(String token, String isbn, ReadingBookCreateRequest request) {
        // 사용자 받아오기
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // 자주 사용되는 객체 재사용 용도로 선언
        Book book = bookRepository.findByIsbn(isbn);
        LocationList locationList = null;
        MemberLocation memberLocation = null;

        // memberId와 isbn을 이용해 사용자별 리뷰 등록 책이 중복되었는지 검사
        if (bookRecordRepository.findByMemberAndBook(member, book) != null) {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "이미 등록한 도서입니다."),
                    HttpStatus.CONFLICT);
        }

        // isbn을 이용해 책 등록이 중복되었는지 검색
        if (book == null) {
            // 등록되지 않은 책이면 새로 등록
            book = Book.create(isbn, request.bookInformation().cover(), request.bookInformation().title(), request.bookInformation().author(), request.bookInformation().category(), Integer.parseInt(request.bookInformation().totalPage()));
            bookRepository.save(book);
            book = bookRepository.findByIsbn(isbn);
        }

        if (request.mainLocation() != null) {
            double latitude = Double.parseDouble(request.mainLocation().latitude());
            double longitude = Double.parseDouble(request.mainLocation().longitude());

            // 이미 있는 위치인지 검색
            locationList = locationRepository.findByPlaceName(request.mainLocation().placeName());
            if (locationList == null) {
                // 등록하지 않은 위치면 새로 등록
                locationList = LocationList.create(request.mainLocation().placeName(), request.mainLocation().address(), latitude, longitude);
                locationRepository.save(locationList);
                locationList = locationRepository.findByPlaceName(request.mainLocation().placeName());
            }

            memberLocation = memberLocationRepository.findByMemberAndLocationList(member, locationList);
            // 최근 위치 검색 기록에 없으면 추가
            if (memberLocation == null) {
                // 현재 사용자의 레코드가 5개 이상인지 확인, 5개 이상이면 날짜 순으로 정렬 후 가장 오래된 레코드 삭제
                if (memberLocationRepository.countAllByMember(member) >= 5) {
                    List<MemberLocation> memberLocationList = memberLocationRepository.findByMemberOrderByDateAsc(member);
                    memberLocationRepository.delete(memberLocationList.get(0));
                }

                // 검색 기록에 추가
                memberLocation = MemberLocation.create(member, locationList, LocalDate.now().toString());
                memberLocationRepository.save(memberLocation);
            }
        }

        // 독서노트에 등록
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

        // 사용자 닉네임으로 한줄평 남긴 사용자 찾기


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
            bookReviewReaction = BookReviewReaction.create(bookReview);
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

    // 한줄평 반응 추가
    public ResponseEntity<DefaultResponse> reactionComment(String token, String isbn, ReactionCommentRequest request) {
        // 사용자 받아오기
        // 문제! 이러면 본인이 등록한 한줄평만 검색할 수 있음. 사용자 닉네임을 받아와서 닉네임으로 한줄평 사용자 찾도록 수정
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // 사용자 닉네임으로 한줄평 남긴 사용자 찾기


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
            bookReviewReaction = BookReviewReaction.create(bookReview);
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

    // 리뷰 작성 (키워드, 선택 리뷰, 한줄평 각 테이블에 추가)
    public ResponseEntity<DefaultResponse> createReview(String token, String isbn, ReviewCreateRequest request) {
        // 사용자 받아오기
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 키워드 리뷰가 있으면 독서 노트에 수정
        if (request.keyword() != null) {
            BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);
            bookRecord.setKeyWord(request.keyword());
        }

        // 선택 리뷰가 있으면 레코드 추가
        if (request.select() != null) {
            // 이미 등록한 선택 리뷰면 CONFLICT 응답
            KeywordReview keywordReview = keywordReviewRepository.findByMemberAndBook(member, book);
            if (keywordReview != null) {
                return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "이미 등록한 리뷰입니다."),
                        HttpStatus.CONFLICT);
            }

            String selected = "";
            for (int id = 0; id < request.select().size(); id++) {
                selected += request.select().get(id) + ",";
            }

            keywordReview = KeywordReview.create(member, book, selected);
            keywordReviewRepository.save(keywordReview);
        }

        // 한줄평이 있으면 레코드 추가
        if (request.comment() != null) {
            // 이미 등록한 한줄평이면 CONFLICT 응답
            BookReview bookReview = bookReviewRepository.findByMemberAndBook(member, book);
            if (bookReview != null) {
                return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "이미 등록한 한줄평입니다."),
                        HttpStatus.CONFLICT);
            }

            bookReview = BookReview.create(member, book, request.comment());
            bookReviewRepository.save(bookReview);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 책별 대표 위치 등록 (주소 테이블에 추가, 해당 책에 대표 위치 등록)
    public ResponseEntity<DefaultResponse> addMainLocation(String token, String isbn, LocationRequest request) {
        // 사용자 받아오기
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 자주 사용하는 위도, 경도 변수 따로 저장
        double latitude = Double.parseDouble(request.latitude());
        double longitude = Double.parseDouble(request.longitude());

        // 장소명으로 해당 주소 찾기
        LocationList locationList = locationRepository.findByPlaceName(request.placeName());
        // 등록되지 않은 주소면 새로 등록
        if(locationList == null) {
            locationList = LocationList.create(request.placeName(), request.address(), latitude, longitude);
            locationRepository.save(locationList);
            locationList = locationRepository.findByPlaceName(request.placeName());
        }

        // 사용자 독서노트 검색
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);
        if (bookRecord != null) {
            // 대표 위치가 이미 있으면 CONFLICT 응답
            if (bookRecord.getLocationList() != null) {
                return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "대표 위치가 이미 있습니다."),
                        HttpStatus.CONFLICT);
            }
            else { // 대표 위치가 없으면 대표위치 등록
                bookRecord.setLocationList(locationList);
            }
        }

        // 사용자 최근 검색 위치에 등록
        MemberLocation memberLocation = memberLocationRepository.findByMemberAndLocationList(member, locationList);
        // 최근 위치 검색 기록에 없으면 추가
        if (memberLocation == null) {
            // 현재 사용자의 레코드가 5개 이상인지 확인, 5개 이상이면 날짜 순으로 정렬 후 가장 오래된 레코드 삭제
            if (memberLocationRepository.countAllByMember(member) >= 5) {
                List<MemberLocation> memberLocationList = memberLocationRepository.findByMemberOrderByDateAsc(member);
                memberLocationRepository.delete(memberLocationList.get(0));
            }

            // 검색 기록에 추가
            memberLocation = MemberLocation.create(member, locationList, LocalDate.now().toString());
            memberLocationRepository.save(memberLocation);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 책별 대표 위치 변경
    public ResponseEntity<DefaultResponse> modifyMainLocation(String token, String isbn, LocationRequest request) {
        // 사용자 받아오기
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 자주 사용하는 위도, 경도 변수 따로 저장
        double latitude = Double.parseDouble(request.latitude());
        double longitude = Double.parseDouble(request.longitude());

        // 장소명으로 해당 주소 찾기
        LocationList locationList = locationRepository.findByPlaceName(request.placeName());
        // 등록되지 않은 주소면 새로 등록
        if(locationList == null) {
            locationList = LocationList.create(request.placeName(), request.address(), latitude, longitude);
            locationRepository.save(locationList);
            locationList = locationRepository.findByPlaceName(request.placeName());
        }

        // 사용자 독서노트 검색
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);
        if (bookRecord != null) { // 해당 독서 노트가 있는 경우에만
            // 쓰이지 않는 위치면 삭제하기 위해 변경 전 위치를 받아옴
            LocationList preLocation = bookRecord.getLocationList();
            bookRecord.setLocationList(locationList);

            if (preLocation != null) {
                // 다른 곳에서 사용중이 아닌 위치면 삭제
                Long memberLocationCnt = memberLocationRepository.countByLocationList(preLocation);
                Long bookRecordLocationCnt = bookRecordRepository.countByLocationList(preLocation);
                Long bookmarkLocationCnt = bookmarkRepository.countByLocationList(preLocation);

                if (memberLocationCnt + bookRecordLocationCnt + bookmarkLocationCnt <= 0) {
                    locationRepository.delete(preLocation);
                }
            }
        }

        // 사용자 최근 검색 위치에 등록
        MemberLocation memberLocation = memberLocationRepository.findByMemberAndLocationList(member, locationList);
        // 최근 위치 검색 기록에 없으면 추가
        if (memberLocation == null) {
            // 현재 사용자의 레코드가 5개 이상인지 확인, 5개 이상이면 날짜 순으로 정렬 후 가장 오래된 레코드 삭제
            if (memberLocationRepository.countAllByMember(member) >= 5) {
                List<MemberLocation> memberLocationList = memberLocationRepository.findByMemberOrderByDateAsc(member);
                memberLocationRepository.delete(memberLocationList.get(0));
            }

            // 검색 기록에 추가
            memberLocation = MemberLocation.create(member, locationList, LocalDate.now().toString());
            memberLocationRepository.save(memberLocation);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 책별 대표 위치 삭제
    public ResponseEntity<DefaultResponse> deleteMainLocation(String token, String isbn) {
        // 사용자 받아오기
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 독서노트 검색
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        if (bookRecord != null) {
            // 대표위치 삭제 전 위치 객체 받아오기
            LocationList preLocation = bookRecord.getLocationList();

            //  대표위치 삭제
            bookRecord.deleteLocationList();
            bookRecordRepository.save(bookRecord);

            // 다른 곳에서 사용중이 아닌 위치면 삭제
            Long memberLocationCnt = memberLocationRepository.countByLocationList(preLocation);
            Long bookRecordLocationCnt = bookRecordRepository.countByLocationList(preLocation);
            Long bookmarkLocationCnt = bookmarkRepository.countByLocationList(preLocation);

            if (memberLocationCnt + bookRecordLocationCnt + bookmarkLocationCnt <= 0) {
                locationRepository.delete(preLocation);
            }
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 인물사전 등록
    public ResponseEntity<DefaultResponse> addpersonalDictionary(String token, String isbn, PersonalDictionaryRequest request) {
        // 사용자 받아오기
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 해당 인물이 중복됐는지 검색
        PersonalDictionary personalDictionary = personalDictionaryRepository.findByMemberAndBookAndName(member, book, request.name());

        // 중복된 인물이면 (이름이 중복됐으면)
        if (personalDictionary != null) {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "이미 등록한 인물입니다."),
                    HttpStatus.CONFLICT);
        }
        else {
            // 인물사전에 등록
            personalDictionary = PersonalDictionary.create(member, book, request.name(), request.emoji(), request.preview(), request.description());
            personalDictionaryRepository.save(personalDictionary);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 인물사전 수정
    public ResponseEntity<DefaultResponse> modifyPersonalDictionary(String token, String isbn, PersonalDictionaryRequest request) {
        // 사용자 받아오기
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 해당 인물이 있는지 검색
        PersonalDictionary personalDictionary = personalDictionaryRepository.findByMemberAndBookAndName(member, book, request.name());

        // 중복된 인물이면 (이름이 중복됐으면)
        if (personalDictionary != null) {
            // 인물사전에서 수정 등록
            personalDictionary = PersonalDictionary.create(member, book, request.name(), request.emoji(), request.preview(), request.description());
            personalDictionaryRepository.save(personalDictionary);
        }
        else {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "등록하지 않은 인물입니다."),
                    HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 인물사전 전체조회
    public ResponseEntity<DefaultResponse> getPersonalDictionary(String token, String isbn) {
        // 응답으로 보낼 인물사전 List
        List<GetPersonalDictionaryResponse> personalDictionaryList = new ArrayList<>();

        // 사용자 받아오기
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 한 유저의 한 책에 대한 인물사전 전체 검색
        List<PersonalDictionary> personalDictionaries = personalDictionaryRepository.findAllByMemberAndBook(member, book);
        for (int i = 0; i < personalDictionaries.size(); i++) {
            PersonalDictionary personalDictionary = personalDictionaries.get(i);

            // 응답으로 보낼 내용에 더하기
            personalDictionaryList.add(new GetPersonalDictionaryResponse(personalDictionary.getEmoji(), personalDictionary.getName(), personalDictionary.getPreview(), personalDictionary.getDescription()));
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공", personalDictionaryList),
                HttpStatus.OK);
    }

    // 메모 등록
    public ResponseEntity<DefaultResponse> addMemo(String token, String isbn, MemoRequest request) {
        // 사용자 받아오기
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        Memo memo = memoRepository.findByMemberAndBookAndUuid(member, book, request.uuid());
        if (memo != null) {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "이미 등록한 메모입니다."),
                    HttpStatus.CONFLICT);
        }

        // 종이책이면
        if (bookRecordRepository.findByMemberAndBook(member, book).getBookType() == 0) {
            // 페이지 그대로 메모 등록
            memo = Memo.create(member, book, request.uuid(), request.markPage(), request.date(), request.memoText());
            memoRepository.save(memo);
        }
        else { // 전자책, 오디오북이면 퍼센트 계산해서 메모 등록
            memo = Memo.create(member, book, request.uuid(), request.markPage() / book.getTotalPage(), request.date(), request.memoText());
            memoRepository.save(memo);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 메모 전체조회
    public ResponseEntity<DefaultResponse> getMemo(String token, String isbn) {
        // 응답으로 보낼 메모 List
        List<GetMemoResponse> memoList = new ArrayList<>();

        // 사용자 받아오기
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 한 유저의 한 책에 대한 메모 전체 검색
        List<Memo> memos = memoRepository.findAllByMemberAndBook(member, book);
        for (int i = 0; i < memos.size(); i++) {
            Memo memo = memos.get(i);

            // 응답으로 보낼 내용에 더하기
            memoList.add(new GetMemoResponse(memo.getDate(), memo.getMarkPage(), memo.getMemoText(), memo.getUuid()));
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공", memoList),
                HttpStatus.OK);
    }

    // 책갈피 등록
    public ResponseEntity<DefaultResponse> addBookmark(String token, String isbn, BookmarkRequest request) {
        // 사용자 받아오기
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        Bookmark bookmark = bookmarkRepository.findByMemberAndBookAndUuid(member, book, request.uuid());
        if (bookmark != null) {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "이미 등록한 책갈피입니다."),
                    HttpStatus.CONFLICT);
        }

        LocationList locationList = null;
        if (request.mainLocation() != null) {
            // 자주 사용하는 변수 따로 선언
            double latitude = Double.parseDouble(request.mainLocation().latitude());
            double longitude = Double.parseDouble(request.mainLocation().longitude());

            // 주소 없으면 등록
            locationList = locationRepository.findByPlaceName(request.mainLocation().placeName());
            if (locationList == null) {
                locationList = LocationList.create(request.mainLocation().placeName(), request.mainLocation().address(), latitude, longitude);
                locationRepository.save(locationList);
                locationList = locationRepository.findByPlaceName(request.mainLocation().placeName());
            }

            // 사용자 최근 검색 위치에 등록
            MemberLocation memberLocation = memberLocationRepository.findByMemberAndLocationList(member, locationList);
            // 최근 위치 검색 기록에 없으면 추가
            if (memberLocation == null) {
                // 현재 사용자의 레코드가 5개 이상인지 확인, 5개 이상이면 날짜 순으로 정렬 후 가장 오래된 레코드 삭제
                if (memberLocationRepository.countAllByMember(member) >= 5) {
                    List<MemberLocation> memberLocationList = memberLocationRepository.findByMemberOrderByDateAsc(member);
                    memberLocationRepository.delete(memberLocationList.get(0));
                }

                // 검색 기록에 추가
                memberLocation = MemberLocation.create(member, locationList, LocalDate.now().toString());
                memberLocationRepository.save(memberLocation);
            }
        }

        // 종이책이면
        if (bookRecordRepository.findByMemberAndBook(member, book).getBookType() == 0) {
            // 페이지 그대로 책갈피 등록
            bookmark = Bookmark.create(member, book, request.uuid(), request.markPage(), locationList, request.date());
            bookmarkRepository.save(bookmark);
        }
        else { // 전자책, 오디오북이면 퍼센트 계산해서 메모 등록
            bookmark = Bookmark.create(member, book, request.uuid(), request.markPage() / book.getTotalPage(), locationList, request.date());
            bookmarkRepository.save(bookmark);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 책갈피 수정
    public ResponseEntity<DefaultResponse> modifyBookmark(String token, String isbn, BookmarkRequest request) {
        // 사용자 받아오기
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 책갈피가 있으면
        Bookmark bookmark = bookmarkRepository.findByMemberAndBookAndUuid(member, book, request.uuid());
        if (bookmark != null) {

            // 수정할 위치를 받았으면
            LocationList locationList = null;
            if (request.mainLocation() != null) {
                // 자주 사용하는 변수 따로 선언
                double latitude = Double.parseDouble(request.mainLocation().latitude());
                double longitude = Double.parseDouble(request.mainLocation().longitude());

                // 변경 전 위치가 아무데도 사용중이 아니면 삭제하기 위해 받아옴
                LocationList preLocation = bookmark.getLocationList();

                // 새로 받은 위치가 주소 테이블에 없으면 등록
                locationList = locationRepository.findByPlaceName(request.mainLocation().placeName());
                if (locationList == null) {
                    locationList = LocationList.create(request.mainLocation().placeName(), request.mainLocation().address(), latitude, longitude);
                    locationRepository.save(locationList);
                    locationList = locationRepository.findByPlaceName(request.mainLocation().placeName());
                }

                // 사용자 최근 검색 위치에 등록
                MemberLocation memberLocation = memberLocationRepository.findByMemberAndLocationList(member, locationList);
                // 최근 위치 검색 기록에 없으면 추가
                if (memberLocation == null) {
                    // 현재 사용자의 레코드가 5개 이상인지 확인, 5개 이상이면 날짜 순으로 정렬 후 가장 오래된 레코드 삭제
                    if (memberLocationRepository.countAllByMember(member) >= 5) {
                        List<MemberLocation> memberLocationList = memberLocationRepository.findByMemberOrderByDateAsc(member);
                        memberLocationRepository.delete(memberLocationList.get(0));
                    }

                    // 검색 기록에 추가
                    memberLocation = MemberLocation.create(member, locationList, LocalDate.now().toString());
                    memberLocationRepository.save(memberLocation);
                }

                if (preLocation != null) {
                    // 다른 곳에서 사용중이 아닌 위치면 삭제
                    Long memberLocationCnt = memberLocationRepository.countByLocationList(preLocation);
                    Long bookRecordLocationCnt = bookRecordRepository.countByLocationList(preLocation);
                    Long bookmarkLocationCnt = bookmarkRepository.countByLocationList(preLocation);

                    if (memberLocationCnt + bookRecordLocationCnt + bookmarkLocationCnt <= 0) {
                        locationRepository.delete(preLocation);
                    }
                }
            }

            // 종이책이면
            if (bookRecordRepository.findByMemberAndBook(member, book).getBookType() == 0) {
                // 페이지 그대로 책갈피 등록
                bookmark = Bookmark.create(member, book, request.uuid(), request.markPage(), locationList, request.date());
                bookmarkRepository.save(bookmark);
            }
            else { // 전자책, 오디오북이면 퍼센트 계산해서 메모 등록
                bookmark = Bookmark.create(member, book, request.uuid(), request.markPage() / book.getTotalPage(), locationList, request.date());
                bookmarkRepository.save(bookmark);
            }
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 책갈피 전체조회
    public ResponseEntity<DefaultResponse> getBookmark(String token, String isbn) {
        // 응답으로 보낼 메모 List
        List<GetBookmarkResponse> bookmarkList = new ArrayList<>();

        // 사용자 받아오기
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 한 유저의 한 책에 대한 메모 전체 검색
        List<Bookmark> bookmarks = bookmarkRepository.findAllByMemberAndBook(member, book);
        for (int i = 0; i < bookmarks.size(); i++) {
            Bookmark bookmark = bookmarks.get(i);

            // 위치 List 저장
            List<String> location = new ArrayList<>();
            location.add(bookmark.getLocationList().getPlaceName());
            location.add(bookmark.getLocationList().getAddress());
            location.add(String.valueOf(bookmark.getLocationList().getLatitude()));
            location.add(String.valueOf(bookmark.getLocationList().getLongitude()));

            // 응답으로 보낼 내용에 더하기
            bookmarkList.add(new GetBookmarkResponse(bookmark.getDate(), bookmark.getMarkPage(), location, bookmark.getUuid()));
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공", bookmarkList),
                HttpStatus.OK);
    }

    // 전체 위치 조회
    public ResponseEntity<DefaultResponse> getAllLocation(String token, GetAllLocationRequest request) {
        // 응답으로 보낼 객체 리스트
        List<GetAllLocationResponse> locationInfo = new ArrayList<>();

        int orderNumber = request.orderNumber();

        // 사용자 받아오기
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // Response 전달 시 들어가는 데이터
        String date;
        String title;
        int readPage;

        // 독서노트에서 정보 받아오기
        List<BookRecord> bookRecords = bookRecordRepository.findAllByMember(member);
        int size = bookRecords.size();
        BookRecord bookRecord;
        for (int i = (orderNumber * 20); i < 20 + (orderNumber * 20); i++) {
            // 현재 조회하는 컬럼이 받아온 컬럼들의 사이즈보다 같거나 크면 break
            if (i >= size) break;

            bookRecord = bookRecords.get(i);
            if (bookRecord.getLocationList() == null) break;

            date = bookRecord.getStartDate();
            title = bookRecord.getBook().getTitle();

            List<String> location = new ArrayList<>();
            location.add(bookRecord.getLocationList().getPlaceName());
            location.add(bookRecord.getLocationList().getAddress());
            location.add(String.valueOf(bookRecord.getLocationList().getLatitude()));
            location.add(String.valueOf(bookRecord.getLocationList().getLongitude()));

            locationInfo.add(new GetAllLocationResponse(date, false, title, 0, location));
        }

        // 책갈피에서 정보 받아오기
        List<Bookmark> bookmarks = bookmarkRepository.findAllByMember(member);
        size = bookmarks.size();
        Bookmark bookmark;
        for (int i = (orderNumber * 20); i < 20 + (orderNumber * 20); i++) {
            if (i >= size) break;

            bookmark = bookmarks.get(i);
            if (bookmark.getLocationList() == null) break;

            date = bookmark.getDate();
            title = bookmark.getBook().getTitle();
            readPage = bookmark.getMarkPage();

            List<String> location = new ArrayList<>();
            location.add(bookmark.getLocationList().getPlaceName());
            location.add(bookmark.getLocationList().getAddress());
            location.add(String.valueOf(bookmark.getLocationList().getLatitude()));
            location.add(String.valueOf(bookmark.getLocationList().getLongitude()));

            locationInfo.add(new GetAllLocationResponse(date, true, title, readPage, location));
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공", locationInfo),
                HttpStatus.OK);
    }

    // 최근 등록 위치 조회
    public ResponseEntity<DefaultResponse> getRecentLocation(String token) {
        // 응답으로 보낼 객체 리스트
        List<GetRecentLocationResponse> location = new ArrayList<>();

        // 사용자 받아오기
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        List<MemberLocation> memberLocationList = memberLocationRepository.findAllByMember(member);
        for (MemberLocation value : memberLocationList) {
            LocationList memberLocation = value.getLocationList();

            // 응답으로 보낼 객체에 추가
            location.add(new GetRecentLocationResponse(memberLocation.getLocationId(), memberLocation.getPlaceName(), memberLocation.getAddress(), memberLocation.getLatitude(), memberLocation.getLongitude()));
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공", location),
                HttpStatus.OK);
    }

    // 최근 등록 위치 삭제
    public ResponseEntity<DefaultResponse> deleteRecentLocation(String token, deleteRecentLocationRequest request) {
        // 사용자 받아오기
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        Member member = memberRepository.findByMemberId(memberId);

        // 전체 위치에서 검색
        LocationList locationList = locationRepository.findByLocationId(request.locationId());
        if (locationList != null) {
            // 최근 등록 위치에서 해당 위치가 있는지 확인하고 삭제
            MemberLocation memberLocation = memberLocationRepository.findByMemberAndLocationList(member, locationList);
            if (memberLocation != null) {
                memberLocationRepository.delete(memberLocation);
            }

            // 다른 곳에서 사용중이 아닌 위치면 삭제
            Long bookRecordLocationCnt = bookRecordRepository.countByLocationList(locationList);
            Long bookmarkLocationCnt = bookmarkRepository.countByLocationList(locationList);

            if (bookRecordLocationCnt + bookmarkLocationCnt <= 0) {
                locationRepository.delete(locationList);
            }
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }
}