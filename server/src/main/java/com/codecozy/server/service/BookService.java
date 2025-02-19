package com.codecozy.server.service;

import com.codecozy.server.cache.MemberCacheManager;
import com.codecozy.server.context.ResponseMessages;
import com.codecozy.server.context.StatusCode;
import com.codecozy.server.dto.request.*;
import com.codecozy.server.dto.response.*;
import com.codecozy.server.entity.*;
import com.codecozy.server.repository.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookRecordRepository bookRecordRepository;
    private final MemberRepository memberRepository;
    private final LocationInfoRepository locationInfoRepository;
    private final MemberLocationRepository memberLocationRepository;
    private final BookReviewRepository bookReviewRepository;
    private final BookReviewReactionRepository bookReviewReactionRepository;
    private final BookReviewReviewerRepository bookReviewReviewerRepository;
    private final SelectReviewRepository selectReviewRepository;
    private final PersonalDictionaryRepository personalDictionaryRepository;
    private final MemoRepository memoRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ConverterService converterService;

    // 독서상태 값 모음
    private static final int UNREGISTERED = -1;    // 미등록
    private static final int WANT_TO_READ = 0;     // 읽고 싶은
    private static final int READING = 1;          // 읽는 중
    private static final int FINISH_READ = 2;      // 다 읽음

    // 카테고리 이름 리스트
    private static final List<String> categoryNameList = List.of("문학", "에세이", "인문사회", "과학", "자기계발", "원서", "예술", "기타");

    // 캐시 매니저 설정(의존성 주입)
    private final MemberCacheManager cacheManager;

    // 사용자가 독서노트 추가 시 실행 (책 등록, 위치 등록, 독서노트 등록, 최근 검색 위치 등록)
    public ResponseEntity<DefaultResponse> createBook(Long memberId, String isbn, ReadingBookCreateRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 자주 사용되는 객체 재사용 용도로 선언
        LocationInfo locationInfo = null;
        MemberLocation memberLocation = null;

        // memberId와 isbn을 이용해 사용자별 리뷰 등록 책이 중복되었는지 검사
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);
        if (bookRecord != null && bookRecord.getBookType() != -1) { // reading_status가 '읽고싶은'(0)인 경우, bookType이 -1로 생성됨
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.CONFLICT, ResponseMessages.CONFLICT_BOOK_RECORD.get()),
                    HttpStatus.CONFLICT);
        }

        // isbn을 이용해 책 등록이 중복되었는지 검색
        if (book == null) {
            book = registerBook(isbn, request.bookInformation());
        }

        if (request.mainLocation() != null) {
            double latitude = Double.parseDouble(request.mainLocation().latitude());
            double longitude = Double.parseDouble(request.mainLocation().longitude());

            // 이미 있는 위치인지 검색
            locationInfo = locationInfoRepository.findByPlaceName(request.mainLocation().placeName());
            if (locationInfo == null) {
                locationInfo = registerLocation(request.mainLocation().placeName(), request.mainLocation().address(),
                        latitude, longitude);
            }

            memberLocation = memberLocationRepository.findByMemberAndLocationInfo(member,
                    locationInfo);
            // 최근 위치 검색 기록에 없으면 추가
            if (memberLocation == null) {
                registerRecentLocation(member, locationInfo);
            }
        }

        // 독서노트에 등록
        LocalDate startDate = converterService.stringToDate(request.startDate());
        LocalDate recentDate = null;
        if (request.recentDate() != null) {
            recentDate = converterService.stringToDate(request.recentDate());
        }
        bookRecord = BookRecord.create(member, book, request.readingStatus(), request.bookType(),
                locationInfo,
                request.isMine(), startDate, recentDate);

        // 독서노트에 마지막으로 수정한 날짜 저장
        bookRecord.setLastEditDate(LocalDateTime.now());

        // 반영
        bookRecordRepository.save(bookRecord);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 독서노트 삭제
    public ResponseEntity<DefaultResponse> deleteBook(Long memberId, String isbn) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // 해당 책 찾기
        Book book = bookRepository.findByIsbn(isbn);
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 삭제
        bookRecordRepository.delete(bookRecord);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 독서노트 조회
    public ResponseEntity<DefaultResponse> getReadingNote(Long memberId, String isbn) {
        // 사용자 받아오기
        Member member = getMemberById(memberId);

        // 해당 책 찾기
        Book book = bookRepository.findByIsbn(isbn);
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // ---- 정보 가져오기 ---- \\
        String cover = book.getCover();
        String title = book.getTitle();
        String author = book.getAuthor();
        String categoryName = book.getCategory();
        int totalPage = book.getTotalPage();
        int readPage = bookRecord.getMarkPage();
        int readingPercent = converterService.pageToPercent(readPage, totalPage);
        String firstReviewDate = bookRecord.getFirstReviewDate() != null ?
                converterService.dateToString(bookRecord.getFirstReviewDate()) : null;
        String keywordReview = bookRecord.getKeyWord();
        String commentReview = null;
        try {
            commentReview = bookRecord.getBookReview().getReviewText();
        } catch (NullPointerException e) {
            log.info("해당 책의 한줄평 없음");
        }

        String selectReviewStr;
        String[] selectReviewStrList = null;
        List<Integer> selectReview = new ArrayList<>();
        boolean existSelectReview = true;
        try {
            selectReviewStr = bookRecord.getSelectReview().getSelectReviewCode();
            selectReviewStrList = selectReviewStr.split(",");
        } catch (NullPointerException e) {
            log.info("해당 책의 선택 리뷰 없음");
            existSelectReview = false;
        }
        if (existSelectReview) {
            for (String s : selectReviewStrList) {
                selectReview.add(Integer.valueOf(s));
            }
        }

        boolean isMine = bookRecord.isMine();
        int bookType = bookRecord.getBookType();
        int readingStatus = bookRecord.getReadingStatus();
        String mainLocation = null;
        try {
            mainLocation = bookRecord.getLocationInfo().getPlaceName();
        } catch (NullPointerException e) {
            log.info("해당 책의 대표 위치 없음");
        }
        String startDate = converterService.dateToString(bookRecord.getStartDate());
        String recentDate = null;
        try {
            recentDate = converterService.dateToString(bookRecord.getRecentDate());
        } catch (NullPointerException e) {
            log.info("해당 책의 마지막으로 읽은 날짜 없음");
        }

        List<Bookmark> bookmarkList = bookmarkRepository.findTop3ByBookRecordOrderByDateDesc(bookRecord);
        List<BookmarkPreviewResponse> bookmarks = new ArrayList<>();
        for (Bookmark bookmark : bookmarkList) {
            int markPage = bookmark.getMarkPage();
            int markPercent = converterService.pageToPercent(markPage, totalPage);
            String dateStr = converterService.dateToString(bookmark.getDate());

            bookmarks.add(new BookmarkPreviewResponse(
                    dateStr,
                    markPage,
                    markPercent,
                    bookmark.getLocationInfo().getPlaceName(),
                    bookmark.getUuid()
            ));
        }

        List<Memo> memoList = memoRepository.findTop3ByBookRecordOrderByDateDesc(bookRecord);
        List<MemoResponse> memos = new ArrayList<>();
        for (Memo memo : memoList) {
            int markPage = memo.getMarkPage();
            int markPercent = converterService.pageToPercent(markPage, totalPage);
            String dateStr = converterService.dateToString(memo.getDate());

            memos.add(new MemoResponse(
                    dateStr,
                    markPage,
                    markPercent,
                    memo.getMemoText(),
                    memo.getUuid()
            ));
        }

        List<PersonalDictionary> personalDictionaryList =
                personalDictionaryRepository.findTop3ByBookRecordOrderByNameAsc(bookRecord);
        List<PersonalDictionaryPreviewResponse> characters = new ArrayList<>();
        for (PersonalDictionary personalDictionary : personalDictionaryList) {
            characters.add(new PersonalDictionaryPreviewResponse(
                    personalDictionary.getEmoji(),
                    personalDictionary.getName(),
                    personalDictionary.getPreview()
            ));
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(), new GetReadingNoteResponse(
                        cover,
                        title,
                        author,
                        categoryName,
                        totalPage,
                        readPage,
                        readingPercent,
                        firstReviewDate,
                        keywordReview,
                        commentReview,
                        selectReview,
                        isMine,
                        bookType,
                        readingStatus,
                        mainLocation,
                        startDate,
                        recentDate,
                        bookmarks,
                        memos,
                        characters)),
                HttpStatus.OK);
    }

    // 독서상태 변경
    public ResponseEntity<DefaultResponse> modifyReadingStatus(Long memberId, String isbn,
                                                               ModifyReadingStatusRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // 해당 책 찾기
        Book book = bookRepository.findByIsbn(isbn);
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // ** 이전 값 확인 & 상황에 따른 추가 로직 수행 **
        int beforeStatus = bookRecord.getReadingStatus();
        int afterStatus = request.readingStatus();

        // 읽는 중 -> 다 읽음 전환 시
        if (beforeStatus == READING && afterStatus == FINISH_READ) {
            // 1. 마지막 읽은 날짜 수정
            bookRecord.setRecentDate(LocalDate.now());

            // 2. 독서 진행률 100%로 수정
            bookRecord.setMarkPage(book.getTotalPage());

            // 3. 100% 책갈피 하나 추가 + BOOK_RECORD_DATE 변경
            // 만일 uuid 값이 없다면
            if (request.uuid() == null) {
                return new ResponseEntity<>(
                        DefaultResponse.from(StatusCode.BAD_REQUEST, ResponseMessages.MISSING_UUID.get()),
                        HttpStatus.BAD_REQUEST);
            }
            bookmarkRepository.save(Bookmark.create(
                    bookRecord,
                    request.uuid(),
                    book.getTotalPage(),
                    null,
                    LocalDate.now()
            ));

            // 독서노트에 마지막으로 수정한 날짜 저장
            bookRecord.setLastEditDate(LocalDateTime.now());
        }
        // 다 읽음 -> 읽는 중 전환 시
        else if (beforeStatus == FINISH_READ && afterStatus == READING) {
            // 읽은 페이지 0으로 변경
            bookRecord.setMarkPage(0);
        }

        // 독서상태 값 변경 후 저장
        bookRecord.setReadingStatus(afterStatus);
        bookRecordRepository.save(bookRecord);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 소장 여부 변경
    public ResponseEntity<DefaultResponse> modifyIsMine(Long memberId, String isbn, boolean isMine) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // 해당 책 찾기
        Book book = bookRepository.findByIsbn(isbn);
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 변경 후 저장
        bookRecord.setIsMine(isMine);
        bookRecordRepository.save(bookRecord);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 책 유형 변경
    public ResponseEntity<DefaultResponse> modifyBookType(Long memberId, String isbn, int bookType) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // 해당 책 찾기
        Book book = bookRepository.findByIsbn(isbn);
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 변경 후 저장
        bookRecord.setBookType(bookType);
        bookRecordRepository.save(bookRecord);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 도서 정보 초기 조회 API
    public ResponseEntity<DefaultResponse> searchBookDetail(Long memberId, String isbn) throws IOException {
        // URL로 도서 API 데이터 가져오기
        String jsonResponse = getBookDate(isbn);
        SearchBookResponse response = dataParsing(jsonResponse);

        // 사용자 받아오기
        Member member = getMemberById(memberId);

        // 해당 책 찾기
        Book book = bookRepository.findByIsbn(isbn);
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // categoryName 수정
        String categoryName = response.categoryName().substring(response.categoryName().lastIndexOf(">") + 1);
        categoryName = extractCategory(categoryName);

        // readingStatus 검색
        int readingStatus = (bookRecord != null ? bookRecord.getReadingStatus() : UNREGISTERED);

        // commentCount 검색
        int commentCount = bookReviewRepository.countByBookRecordBook(book);

        // selectedReview를 검색해 한줄평 반응 찾기
        List<Integer> selectedReviewList = getSelectedReviewList(selectReviewRepository.findAllByBookRecordBook(book));

        // 해당 책에 대한 한줄평 정보(commentList)를 날짜 내림차순으로 검색해서 실제로 반환하는 commentList(최대 5개) 찾기
        List<String> commentList = getLatestCommentList(
                bookReviewRepository.findAllByBookRecordBookOrderByReviewDateDesc(book));

        response = new SearchBookResponse(response.cover(), response.title(), response.author(), categoryName,
                readingStatus, response.publisher(), response.publicationDate(),
                response.totalPage(), response.description(), commentCount, selectedReviewList, commentList);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(), response),
                HttpStatus.OK);
    }

    // 한줄평 추가조회
    public ResponseEntity<DefaultResponse> commentDetail(Long memberId, String isbn, CommentDetailRequest request) {
        // 사용자 받아오기
        Member member = getMemberById(memberId);

        // 해당 책 찾기
        Book book = bookRepository.findByIsbn(isbn);

        // 응답 객체 생성
        List<CommentDetailResponse> response = new ArrayList<>();

        if (request.orderType()) { // orderType이 반응순이라면
            // 해당 책의 모든 한줄평 찾기
            List<BookReview> bookReviews = bookReviewRepository.findAllByBookRecordBook(book);

            if (bookReviews.size() <= 0) {
                return new ResponseEntity<>(DefaultResponse.from(StatusCode.NOT_FOUND,
                        ResponseMessages.NOT_FOUND_REGISTERED_COMMENT.get()),
                        HttpStatus.NOT_FOUND);
            }

            // 한줄평 ID와 반응 합산 수를 저장할 맵
            Map<Long, Integer> reactionMap = new HashMap<>();
            // 한줄평에 대한 반응 검색 후 모든 반응 합산
            for (BookReview bookReview : bookReviews) {
                BookReviewReaction bookReviewReaction = bookReview.getBookReviewReaction();

                // 반응한 한줄평이 없는 경우
                if (bookReviewReaction == null) {
                    reactionMap.put(bookReview.getCommentId(), 0);
                    break;
                }

                reactionMap.put(bookReview.getCommentId(),
                        bookReviewReaction.getHeartCount() + bookReviewReaction.getGoodCount()
                                + bookReviewReaction.getWowCount()
                                + bookReviewReaction.getSadCount() + bookReviewReaction.getAngryCount());
            }

            // 내림차순으로 반응 수 정렬
            List<Map.Entry<Long, Integer>> entryList = new LinkedList<>(reactionMap.entrySet());
            entryList.sort(new Comparator<Map.Entry<Long, Integer>>() {
                @Override
                public int compare(Map.Entry<Long, Integer> o1, Map.Entry<Long, Integer> o2) {
                    return o2.getValue() - o1.getValue();
                }
            });

            // 원하는 위치의 한줄평만 반환
            for (int i = request.orderNumber() * 20; i < (request.orderNumber() * 20) + 20; i++) {
                if (i >= bookReviews.size()) {
                    break;
                }

                BookReview bookReview = bookReviewRepository.findByCommentId(entryList.get(i).getKey());
                BookReviewReaction bookReviewReaction = bookReview.getBookReviewReaction();

                // 한줄평 리액션 수 리스트
                List<Integer> reactions = new ArrayList<>();
                if (bookReviewReaction != null) {
                    reactions.add(bookReviewReaction.getHeartCount());
                    reactions.add(bookReviewReaction.getGoodCount());
                    reactions.add(bookReviewReaction.getWowCount());
                    reactions.add(bookReviewReaction.getSadCount());
                    reactions.add(bookReviewReaction.getAngryCount());
                } else {
                    for (int j = 0; j < 5; j++) {
                        reactions.add(0);
                    }
                }

                // 현재 사용자가 리뷰를 남긴 적이 있으면 리뷰 작성 여부와 종류 받아오기
                boolean isMyReaction = false;
                int myReactionCode = -1;
                BookReviewReviewer bookReviewReviewer = bookReviewReviewerRepository.findByBookReviewAndMember(
                        bookReview, member);
                if (bookReviewReviewer != null) {
                    isMyReaction = bookReviewReviewer.isReaction();
                    myReactionCode = bookReviewReviewer.getReactionCode();
                }

                response.add(new CommentDetailResponse(bookReview.getCommentId(),
                        bookReview.getBookRecord().getMember().getNickname(),
                        bookReview.getReviewText(), bookReview.getReviewDate(),
                        reactions, isMyReaction, myReactionCode));
            }
        } else { // orderType이 최신순이라면
            // 날짜 내림차순으로 해당 책의 모든 한줄평 찾기
            List<BookReview> bookReviews = bookReviewRepository.findAllByBookRecordBookOrderByReviewDateDesc(book);

            if (bookReviews.size() <= 0) {
                return new ResponseEntity<>(DefaultResponse.from(StatusCode.NOT_FOUND,
                        ResponseMessages.NOT_FOUND_REGISTERED_COMMENT.get()),
                        HttpStatus.NOT_FOUND);
            }

            // 원하는 위치의 한줄평만 반환
            for (int i = request.orderNumber() * 20; i < (request.orderNumber() * 20) + 20; i++) {
                if (i >= bookReviews.size()) {
                    break;
                }

                BookReview bookReview = bookReviews.get(i);
                BookReviewReaction bookReviewReaction = bookReview.getBookReviewReaction();

                // 한줄평 리액션 수 리스트
                List<Integer> reactions = new ArrayList<>();
                if (bookReviewReaction != null) {
                    reactions.add(bookReviewReaction.getHeartCount());
                    reactions.add(bookReviewReaction.getGoodCount());
                    reactions.add(bookReviewReaction.getWowCount());
                    reactions.add(bookReviewReaction.getSadCount());
                    reactions.add(bookReviewReaction.getAngryCount());
                } else {
                    for (int j = 0; j < 5; j++) {
                        reactions.add(0);
                    }
                }

                // 현재 사용자가 리뷰를 남긴 적이 있으면 리뷰 작성 여부와 종류 받아오기
                boolean isMyReaction = false;
                int myReactionCode = -1;
                BookReviewReviewer bookReviewReviewer = bookReviewReviewerRepository.findByBookReviewAndMember(
                        bookReview, member);
                if (bookReviewReviewer != null) {
                    isMyReaction = bookReviewReviewer.isReaction();
                    myReactionCode = bookReviewReviewer.getReactionCode();
                }

                response.add(new CommentDetailResponse(bookReview.getCommentId(),
                        bookReview.getBookRecord().getMember().getNickname(),
                        bookReview.getReviewText(), bookReview.getReviewDate(),
                        reactions, isMyReaction, myReactionCode));
            }
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(), response),
                HttpStatus.OK);
    }

    // 한줄평 신고
    public ResponseEntity<DefaultResponse> reportComment(Long memberId, String isbn, ReportCommentRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // 사용자 닉네임으로 한줄평 남긴 사용자 찾기
        Member commentMember = memberRepository.findByNickname(request.name());

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 한줄평 남긴 사용자와 책을 이용한 검색으로 bookReview 테이블에서 한줄평 찾기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(commentMember, book);
        BookReview bookReview = bookRecord.getBookReview();

        if (bookReview == null) {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_COMMENT.get()),
                    HttpStatus.NOT_FOUND);
        }

        // 한줄평 객체를 이용한 검색으로 bookReviewReaction 테이블에서 한줄평에 대한 반응 레코드 검색
        BookReviewReaction bookReviewReaction = bookReview.getBookReviewReaction();
        // 해당 한줄평에 대한 반응 레코드가 없으면 새로 생성
        if (bookReviewReaction == null) {
            bookReviewReaction = registerBookReviewReaction(bookReview);
        }

        // 한줄평에 반응을 등록한 유저인지 검색
        BookReviewReviewer bookReviewReviewer = bookReviewReviewerRepository.findByBookReviewAndMember(bookReview,
                member);

        // 한줄평 반응을 처음 남기는 유저라면
        if (bookReviewReviewer == null) {
            bookReviewReviewer = registerBookReviewReviewer(bookReview, member);
        }

        // 신고하지 않았던 경우
        if (!bookReviewReviewer.isReport()) {
            // 0이면 부적절한 리뷰, 1이면 스팸성 리뷰 카운트 올리고, 신고 여부와 종류 수정
            bookReviewReaction.setReportCountUp(request.reportType());
            bookReviewReviewer.setIsReportReverse();
            bookReviewReviewer.setReportType(request.reportType());
            bookReviewReviewerRepository.save(bookReviewReviewer);
        }
        // 이미 신고했던 경우
        else {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.CONFLICT, ResponseMessages.CONFLICT_REPORT.get()),
                    HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 읽고싶은 책 등록
    public ResponseEntity<DefaultResponse> wantToRead(Long memberId, String isbn, BookCreateRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // 책 검색
        Book book = bookRepository.findByIsbn(isbn);
        // isbn을 이용해 책 등록이 중복되었는지 검색
        if (book == null) {
            // 주의! publicationDate를 보내지 않으면 오류 발생
            book = registerBook(isbn, request);
        }

        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);
        if (bookRecord != null) {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.CONFLICT,
                            ResponseMessages.CANNOT_REGISTER_BOOK_AS_WANT_TO_READ.get()),
                    HttpStatus.CONFLICT);
        }
        // 읽고싶은 책 등록 시에는 독서노트 생성 전이므로 member, book, reading_status 외에는 임시 데이터로 book_record 레코드 생성 후 저장
        bookRecordRepository.save(BookRecord.create(member, book));

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }


    // 한줄평 반응 추가
    public ResponseEntity<DefaultResponse> reactionComment(Long memberId, String isbn, CommentReactionRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // 사용자 닉네임으로 한줄평 남긴 사용자 찾기
        Member commentMember = memberRepository.findByNickname(request.name());

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 한줄평 남긴 사용자와 책을 이용한 검색으로 bookReview 테이블에서 한줄평 찾기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(commentMember, book);
        BookReview bookReview = bookRecord.getBookReview();

        if (bookReview == null) {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_COMMENT.get()),
                    HttpStatus.NOT_FOUND);
        }

        // 한줄평 객체를 이용한 검색으로 bookReviewReaction 테이블에서 한줄평에 대한 반응 레코드 검색
        BookReviewReaction bookReviewReaction = bookReview.getBookReviewReaction();
        // 해당 한줄평에 대한 반응 레코드가 없으면 새로 생성
        if (bookReviewReaction == null) {
            bookReviewReaction = registerBookReviewReaction(bookReview);
        }

        // 한줄평에 반응을 등록한 유저인지 검색
        BookReviewReviewer bookReviewReviewer = bookReviewReviewerRepository.findByBookReviewAndMember(bookReview,
                member);
        // 현재 한줄평에 반응을 처음 남기는 유저라면
        if (bookReviewReviewer == null) {
            bookReviewReviewer = registerBookReviewReviewer(bookReview, member);
        }

        // 코드에 맞는 반응 카운트 올리고, 반응 여부와 종류 설정
        if (!bookReviewReviewer.isReaction()) {
            // 카운트 올리기
            bookReviewReaction.setReactionCountUp(request.commentReaction());

            // 반응 여부, 종류 설정
            bookReviewReviewer.setIsReactionReverse();
            bookReviewReviewer.setReactionCode(request.commentReaction());
            bookReviewReviewerRepository.save(bookReviewReviewer);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 한줄평 반응 수정
    public ResponseEntity<DefaultResponse> modifyReaction(Long memberId, String isbn, CommentReactionRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // 사용자 닉네임으로 한줄평 남긴 사용자 찾기
        Member commentMember = memberRepository.findByNickname(request.name());

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 한줄평 남긴 사용자와 책을 이용한 검색으로 bookReview 테이블에서 한줄평 찾기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(commentMember, book);
        BookReview bookReview = bookRecord.getBookReview();

        if (bookReview != null) {
            // 한줄평 객체를 이용한 검색으로 bookReviewReaction 테이블에서 한줄평에 대한 반응 카운트 레코드 검색
            BookReviewReaction bookReviewReaction = bookReview.getBookReviewReaction();
            // 한줄평에 대한 반응 여부와 종류를 담은 레코드 검색
            BookReviewReviewer bookReviewReviewer = bookReviewReviewerRepository.findByBookReviewAndMember(bookReview,
                    member);

            if ((bookReviewReaction != null) && (bookReviewReviewer != null)) {
                if (bookReviewReviewer.isReaction()) { // 현재 한줄평에 반응을 남긴적이 있으면
                    // 기존 반응 카운트 하나 내리기
                    bookReviewReaction.setReactionCountDown(bookReviewReviewer.getReactionCode());

                    // 새 반응 종류 저장하기
                    bookReviewReviewer.setReactionCode(request.commentReaction());
                    // 새 반응 카운트 하나 올리기
                    bookReviewReaction.setReactionCountUp(request.commentReaction());

                    // 반응 수정하기
                    bookReviewReviewerRepository.save(bookReviewReviewer);
                    bookReviewReactionRepository.save(bookReviewReaction);
                } else {
                    return new ResponseEntity<>(DefaultResponse.from(StatusCode.NOT_FOUND,
                            ResponseMessages.NOT_FOUND_COMMENT_REACTION.get()),
                            HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<>(DefaultResponse.from(StatusCode.NOT_FOUND,
                        ResponseMessages.NOT_FOUND_COMMENT_REACTION.get()),
                        HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_COMMENT.get()),
                    HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 한줄평 반응 삭제
    public ResponseEntity<DefaultResponse> deleteReaction(Long memberId, String isbn, DeleteReactionRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // 사용자 닉네임으로 한줄평 남긴 사용자 찾기
        Member commentMember = memberRepository.findByNickname(request.name());

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 한줄평 남긴 사용자와 책을 이용한 검색으로 bookReview 테이블에서 한줄평 찾기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(commentMember, book);
        BookReview bookReview = bookRecord.getBookReview();

        if (bookReview != null) {
            // 한줄평 객체를 이용한 검색으로 bookReviewReaction 테이블에서 한줄평에 대한 반응 카운트 레코드 검색
            BookReviewReaction bookReviewReaction = bookReview.getBookReviewReaction();
            // 한줄평에 대한 반응 여부와 종류를 담은 레코드 검색
            BookReviewReviewer bookReviewReviewer = bookReviewReviewerRepository.findByBookReviewAndMember(bookReview,
                    member);

            if ((bookReviewReaction != null) && (bookReviewReviewer != null)) {
                if (bookReviewReviewer.isReaction()) { // 현재 한줄평에 반응을 남긴적이 있으면
                    // 기존 반응 카운트 하나 내리기
                    bookReviewReaction.setReactionCountDown(bookReviewReviewer.getReactionCode());

                    // 신고/반응이 하나도 없으면 반응 카운트 레코드 삭제
                    int cnt = bookReviewReaction.getHeartCount() + bookReviewReaction.getGoodCount()
                            + bookReviewReaction.getWowCount()
                            + bookReviewReaction.getSadCount() + bookReviewReaction.getAngryCount()
                            + bookReviewReaction.getReportHatefulCount()
                            + bookReviewReaction.getReportSpamCount();
                    if (cnt <= 0) {
                        bookReview.setBookReviewReaction(null);
                        bookReviewRepository.save(bookReview);
                        bookReviewReactionRepository.delete(bookReviewReaction);
                    }

                    // 반응 여부 false로 변경
                    bookReviewReviewer.setIsReactionReverse();
                    // 신고/반응 여부도 없다면 신고/반응 여부, 종류 레코드 삭제
                    if (!bookReviewReviewer.isReport() && !bookReviewReviewer.isReaction()) {
                        bookReviewReviewerRepository.delete(bookReviewReviewer);
                    }
                } else {
                    return new ResponseEntity<>(DefaultResponse.from(StatusCode.NOT_FOUND,
                            ResponseMessages.NOT_FOUND_COMMENT_REACTION.get()),
                            HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<>(DefaultResponse.from(StatusCode.NOT_FOUND,
                        ResponseMessages.NOT_FOUND_COMMENT_REACTION.get()),
                        HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_COMMENT.get()),
                    HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 리뷰 작성 (키워드, 선택 리뷰, 한줄평 각 테이블에 추가)
    public ResponseEntity<DefaultResponse> createReview(Long memberId, String isbn, ReviewCreateRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);
        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);
        // 해당 리뷰를 작성하는 독서노트 가져오기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 키워드 리뷰가 있으면 독서 노트에 수정
        if (request.keyword() != null) {
            bookRecord.setKeyWord(request.keyword());
            bookRecordRepository.save(bookRecord);
        }

        // 선택 리뷰가 있으면 레코드 추가
        if (request.select() != null) {
            // 이미 등록한 선택 리뷰면 CONFLICT 응답
            SelectReview selectReview = bookRecord.getSelectReview();
            if (selectReview != null) {
                return new ResponseEntity<>(
                        DefaultResponse.from(StatusCode.CONFLICT, ResponseMessages.CONFLICT_SELECT_REVIEW.get()),
                        HttpStatus.CONFLICT);
            }

            String selected = "";
            for (int id = 0; id < request.select().size(); id++) {
                selected += request.select().get(id) + ",";
            }

            selectReview = SelectReview.create(bookRecord, selected);
            selectReviewRepository.save(selectReview);
        }

        // 한줄평이 있으면 레코드 추가
        if (request.comment() != null) {
            // 이미 등록한 한줄평이면 CONFLICT 응답
            BookReview bookReview = bookRecord.getBookReview();
            if (bookReview != null) {
                return new ResponseEntity<>(
                        DefaultResponse.from(StatusCode.CONFLICT, ResponseMessages.CONFLICT_COMMENT.get()),
                        HttpStatus.CONFLICT);
            }

            bookReview = BookReview.create(bookRecord, request.comment());
            bookReviewRepository.save(bookReview);
        }

        // 독서노트에 첫 리뷰 날짜 기록
        bookRecord.setFirstReviewDate(converterService.stringToDate(request.date()));

        // 최근 날짜와 비교해 더 최근이면 수정
        LocalDate date = converterService.stringToDate(request.date());
        if (bookRecord.getRecentDate() == null) {
            bookRecord.setRecentDate(date);
        } else {
            if (date.isAfter(bookRecord.getRecentDate())) {
                bookRecord.setRecentDate(date);
            }
        }

        // 독서노트에 마지막으로 수정한 날짜 저장
        bookRecord.setLastEditDate(LocalDateTime.now());

        // 반영
        bookRecordRepository.save(bookRecord);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 리뷰 전체 수정 (키워드, 선택 리뷰, 한줄평)
    @Transactional
    public ResponseEntity<DefaultResponse> modifyReview(Long memberId, String isbn, ReviewCreateRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        if (bookRecord == null) { // 독서 노트가 없으면 충돌 메세지
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_BOOK_RECORD.get()),
                    HttpStatus.NOT_FOUND);
        }

        // 키워드 리뷰가 있으면 독서 노트에 수정
        if (request.keyword() != null) {
            // 키워드 설정
            bookRecord.setKeyWord(request.keyword());
            bookRecordRepository.save(bookRecord);
        } else { // 키워드 리뷰가 없으면 null로 설정
            bookRecord.setKeyWord(null);
            bookRecordRepository.save(bookRecord);
        }

        // 선택 리뷰가 있으면 레코드 수정
        if (request.select() != null) {
            // 이미 등록한 선택 리뷰면 CONFLICT 응답
            SelectReview selectReview = bookRecord.getSelectReview();

            String selected = "";
            for (int id = 0; id < request.select().size(); id++) {
                selected += request.select().get(id) + ",";
            }

            // 이전에 선택 리뷰를 등록한 경우
            if (selectReview != null) {
                // 선택 리뷰 수정
                selectReview.setSelectReviewCode(selected);
                selectReviewRepository.save(selectReview);
            } else {
                // 이전에 선택 리뷰를 등록하지 않은 경우 새로 생성
                selectReview = SelectReview.create(bookRecord, selected);
                selectReviewRepository.save(selectReview);
            }
        } else { // 선택 리뷰가 없으면 선택 키워드 리뷰 레코드 삭제
            SelectReview selectReview = bookRecord.getSelectReview();
            if (selectReview != null) {
                selectReviewRepository.delete(selectReview);
            }
        }

        // 한줄평이 있으면 레코드 수정
        if (request.comment() != null) {
            BookReview bookReview = bookRecord.getBookReview();
            if (bookReview != null) {
                // 한줄평 수정
                bookReview.setReviewText(request.comment());
                bookReviewRepository.save(bookReview);
            } else {
                // 이전에 한줄평을 등록하지 않았거나 삭제한 경우 새로 생성
                bookReview = BookReview.create(bookRecord, request.comment());
                bookReviewRepository.save(bookReview);
            }
        } else { // 없으면 한줄평 레코드 삭제
            BookReview bookReview = bookRecord.getBookReview();

            if (bookReview != null) {
                bookReviewRepository.delete(bookReview);
            }
        }

        // 독서노트에 마지막으로 수정한 날짜 저장
        bookRecord.setLastEditDate(LocalDateTime.now());

        // 반영
        bookRecordRepository.save(bookRecord);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 리뷰 전체 삭제 (키워드, 선택 리뷰, 한줄평)
    @Transactional
    public ResponseEntity<DefaultResponse> deleteReview(Long memberId, String isbn) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 독서노트 찾기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 독서노트에서 키워드 삭제
        if (bookRecord != null) {
            bookRecord.setKeyWord(null);
            bookRecordRepository.save(bookRecord);
        } else {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_BOOK_RECORD.get()),
                    HttpStatus.NOT_FOUND);
        }

        // 선택리뷰 찾고 삭제
        SelectReview selectReview = bookRecord.getSelectReview();
        if (selectReview != null) {
            selectReviewRepository.delete(selectReview);
        }

        // 한줄평 찾기
        BookReview bookReview = bookRecord.getBookReview();

        // 한줄평 삭제
        if (bookReview != null) {
            bookReviewRepository.delete(bookReview);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 한줄평 삭제
    public ResponseEntity<DefaultResponse> deleteComment(Long memberId, String isbn) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 한줄평 찾기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);
        BookReview bookReview = bookRecord.getBookReview();
        // 한줄평이 있으면
        if (bookReview != null) {
            // 한줄평 지우기
            bookReviewRepository.delete(bookReview);
        } else {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_COMMENT.get()),
                    HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 읽기 시작한 날짜 변경
    public ResponseEntity<DefaultResponse> modifyStartDate(Long memberId, String isbn, String startDate) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 독서노트 찾기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 읽기 시작한 날짜 변경
        if (bookRecord != null) {
            bookRecord.setStartDate(converterService.stringToDate(startDate));
            bookRecordRepository.save(bookRecord);
        } else {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_BOOK_RECORD.get()),
                    HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 마지막 읽은 날짜 (최근 날짜) 변경
    public ResponseEntity<DefaultResponse> modifyRecentDate(Long memberId, String isbn, String recentDate) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 독서노트 찾기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 마지막 읽은 날짜 변경
        if (bookRecord != null) {
            bookRecord.setRecentDate(converterService.stringToDate(recentDate));
            bookRecordRepository.save(bookRecord);
        } else {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_BOOK_RECORD.get()),
                    HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 책별 대표 위치 등록 (주소 테이블에 추가, 해당 책에 대표 위치 등록)
    public ResponseEntity<DefaultResponse> addMainLocation(Long memberId, String isbn, LocationRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 자주 사용하는 위도, 경도 변수 따로 저장
        double latitude = Double.parseDouble(request.latitude());
        double longitude = Double.parseDouble(request.longitude());

        // 장소명으로 해당 주소 찾기
        LocationInfo locationInfo = locationInfoRepository.findByPlaceName(request.placeName());
        // 등록되지 않은 주소면 새로 등록
        if (locationInfo == null) {
            locationInfo = registerLocation(request.placeName(), request.address(), latitude, longitude);
        }

        // 사용자 독서노트 검색
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);
        if (bookRecord != null) {
            // 대표 위치가 이미 있으면 CONFLICT 응답
            if (bookRecord.getLocationInfo() != null) {
                return new ResponseEntity<>(
                        DefaultResponse.from(StatusCode.CONFLICT, ResponseMessages.CONFLICT_MAIN_LOCATION.get()),
                        HttpStatus.CONFLICT);
            } else { // 대표 위치가 없으면 대표위치 등록
                bookRecord.setLocationInfo(locationInfo);
            }
        }

        // 사용자 최근 검색 위치에 등록
        MemberLocation memberLocation = memberLocationRepository.findByMemberAndLocationInfo(member,
                locationInfo);
        // 최근 위치 검색 기록에 없으면 추가
        if (memberLocation == null) {
            registerRecentLocation(member, locationInfo);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 책별 대표 위치 변경
    public ResponseEntity<DefaultResponse> patchMainLocation(Long memberId, String isbn, LocationRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 자주 사용하는 위도, 경도 변수 따로 저장
        double latitude = Double.parseDouble(request.latitude());
        double longitude = Double.parseDouble(request.longitude());

        // 장소명으로 해당 주소 찾기
        LocationInfo locationInfo = locationInfoRepository.findByPlaceName(request.placeName());
        // 등록되지 않은 주소면 새로 등록
        if (locationInfo == null) {
            locationInfo = registerLocation(request.placeName(), request.address(), latitude, longitude);
        }

        // 사용자 독서노트 검색
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);
        if (bookRecord != null) { // 해당 독서 노트가 있는 경우에만
            // 쓰이지 않는 위치면 삭제하기 위해 변경 전 위치를 받아옴
            LocationInfo preLocation = bookRecord.getLocationInfo();

            // 해당 독서 노트의 대표 위치 변경
            bookRecord.setLocationInfo(locationInfo);

            // 변경 전 위치가 다른 곳에서도 사용되지 않으면 locationRepository에서 삭제
            if (preLocation != null) {
                Long memberLocationCnt = memberLocationRepository.countByLocationInfo(preLocation);
                Long bookRecordLocationCnt = bookRecordRepository.countByLocationInfo(preLocation);
                Long bookmarkLocationCnt = bookmarkRepository.countByLocationInfo(preLocation);

                if (memberLocationCnt + bookRecordLocationCnt + bookmarkLocationCnt <= 0) {
                    locationInfoRepository.delete(preLocation);
                }
            }
        } else {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_BOOK_RECORD.get()),
                    HttpStatus.NOT_FOUND);
        }

        // 사용자 최근 검색 위치에 등록
        MemberLocation memberLocation = memberLocationRepository.findByMemberAndLocationInfo(member,
                locationInfo);
        // 최근 위치 검색 기록에 없으면 추가
        if (memberLocation == null) {
            registerRecentLocation(member, locationInfo);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 책별 대표 위치 삭제
    public ResponseEntity<DefaultResponse> deleteMainLocation(Long memberId, String isbn) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 독서노트 검색
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        if (bookRecord != null) {
            // 대표위치 삭제 전 위치 객체 받아오기
            LocationInfo preLocation = bookRecord.getLocationInfo();

            // 삭제하려는 위치 객체가 없으면
            if (preLocation == null) {
                return new ResponseEntity<>(
                        DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_MAIN_LOCATION.get()),
                        HttpStatus.NOT_FOUND);
            }

            //  대표위치 삭제
            bookRecord.deleteLocationInfo();
            bookRecordRepository.save(bookRecord);

            // 다른 곳에서 사용중이 아닌 위치면 삭제
            Long memberLocationCnt = memberLocationRepository.countByLocationInfo(preLocation);
            Long bookRecordLocationCnt = bookRecordRepository.countByLocationInfo(preLocation);
            Long bookmarkLocationCnt = bookmarkRepository.countByLocationInfo(preLocation);

            if (memberLocationCnt + bookRecordLocationCnt + bookmarkLocationCnt <= 0) {
                locationInfoRepository.delete(preLocation);
            }
        } else {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_BOOK_RECORD.get()),
                    HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 인물사전 등록
    public ResponseEntity<DefaultResponse> addpersonalDictionary(Long memberId, String isbn,
                                                                 PersonalDictionaryRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 독서노트 가져오기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 해당 인물이 중복됐는지 검색
        PersonalDictionary personalDictionary = personalDictionaryRepository.findByBookRecordAndName(bookRecord,
                request.name());

        // 중복된 인물이면 (이름이 중복됐으면)
        if (personalDictionary != null) {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.CONFLICT, ResponseMessages.CONFLICT_CHARACTER.get()),
                    HttpStatus.CONFLICT);
        } else {
            // 인물사전에 등록
            personalDictionary = PersonalDictionary.create(bookRecord, request.name(),
                    Integer.parseInt(request.emoji()), request.preview(), request.description());
            personalDictionaryRepository.save(personalDictionary);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 인물사전 수정
    public ResponseEntity<DefaultResponse> modifyPersonalDictionary(Long memberId, String isbn,
                                                                    PersonalDictionaryRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 독서노트 가져오기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 해당 인물이 있는지 검색
        PersonalDictionary personalDictionary =
                personalDictionaryRepository.findByBookRecordAndName(bookRecord, request.name());

        // 중복된 인물이면 (이름이 중복됐으면)
        if (personalDictionary != null) {
            // 인물사전에서 수정 등록
            personalDictionary = PersonalDictionary.create(bookRecord, request.name(),
                    Integer.parseInt(request.emoji()), request.preview(), request.description());
            personalDictionaryRepository.save(personalDictionary);
        } else {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.UNREGISTERED_CHARACTER.get()),
                    HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 인물사전 삭제
    public ResponseEntity<DefaultResponse> deletePersonalDictionary(Long memberId, String isbn,
                                                                    DeletePersonalDictionaryRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 독서노트 가져오기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 해당 인물이 있는지 검색
        PersonalDictionary personalDictionary =
                personalDictionaryRepository.findByBookRecordAndName(bookRecord, request.name());
        if (personalDictionary != null) {
            // 인물사전에서 삭제
            personalDictionaryRepository.delete(personalDictionary);
        } else {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_CHARACTER.get()),
                    HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);

    }

    // 인물사전 전체조회
    public ResponseEntity<DefaultResponse> getPersonalDictionary(Long memberId, String isbn) {
        // 응답으로 보낼 인물사전 List
        List<PersonalDictionaryResponse> personalDictionaryList = new ArrayList<>();

        // 사용자 받아오기
        Member member = getMemberById(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 독서노트 가져오기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 한 유저의 한 책에 대한 인물사전 전체 검색
        List<PersonalDictionary> personalDictionaries = bookRecord.getPersonalDictionaries();
        for (int i = 0; i < personalDictionaries.size(); i++) {
            PersonalDictionary personalDictionary = personalDictionaries.get(i);

            // 응답으로 보낼 내용에 더하기
            personalDictionaryList.add(
                    new PersonalDictionaryResponse(personalDictionary.getEmoji(), personalDictionary.getName(),
                            personalDictionary.getPreview(), personalDictionary.getDescription()));
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(), personalDictionaryList),
                HttpStatus.OK);
    }

    // 메모 등록
    public ResponseEntity<DefaultResponse> addMemo(Long memberId, String isbn, MemoRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 독서노트 가져오기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        Memo memo = memoRepository.findByBookRecordAndUuid(bookRecord, request.uuid());
        if (memo != null) {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, ResponseMessages.CONFLICT_MEMO.get()),
                    HttpStatus.CONFLICT);
        }

        // 최근 날짜와 비교해 더 최근이면 수정
        LocalDate date = converterService.stringToDate(request.date());
        if (bookRecord.getRecentDate() == null) {
            bookRecord.setRecentDate(date);
        } else {
            if (date.isAfter(bookRecord.getRecentDate())) {
                bookRecord.setRecentDate(date);
            }
        }

        memo = Memo.create(bookRecord, request.uuid(), request.markPage(), date, request.memoText());
        memoRepository.save(memo);

        // 독서노트에 마지막으로 수정한 날짜 저장
        bookRecord.setLastEditDate(LocalDateTime.now());

        // 반영
        bookRecordRepository.save(bookRecord);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 메모 수정
    public ResponseEntity<DefaultResponse> modifyMemo(Long memberId, String isbn, MemoRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 최근 날짜와 비교해 더 최근이면 수정
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);
        LocalDate date = converterService.stringToDate(request.date());
        if (date.isAfter(bookRecord.getRecentDate())) {
            bookRecord.setRecentDate(date);
            bookRecordRepository.save(bookRecord);
        }

        // 메모가 있으면
        Memo memo = memoRepository.findByBookRecordAndUuid(bookRecord, request.uuid());
        if (memo != null) {
            memo = Memo.create(bookRecord, request.uuid(), request.markPage(), date, request.memoText());
            memoRepository.save(memo);
        } else {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.UNREGISTERED_MEMO.get()),
                    HttpStatus.NOT_FOUND);
        }

        // 독서노트에 마지막으로 수정한 날짜 저장
        bookRecord.setLastEditDate(LocalDateTime.now());

        // 반영
        bookRecordRepository.save(bookRecord);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 메모 삭제
    public ResponseEntity<DefaultResponse> deleteMemo(Long memberId, String isbn, DeleteUuidRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 독서노트 가져오기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 메모가 있으면
        Memo memo = memoRepository.findByBookRecordAndUuid(bookRecord, request.uuid());
        if (memo != null) {
            // 메모 삭제
            memoRepository.delete(memo);
        } else {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_MEMO.get()),
                    HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 메모 전체조회
    public ResponseEntity<DefaultResponse> getMemo(Long memberId, String isbn) {
        // 응답으로 보낼 메모 List
        List<MemoResponse> memoList = new ArrayList<>();

        // 사용자 받아오기
        Member member = getMemberById(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 독서노트 가져오기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 한 유저의 한 책에 대한 메모 전체 검색
        List<Memo> memos = memoRepository.findAllByBookRecord(bookRecord);
        for (Memo memo : memos) {
            String dateStr = converterService.dateToString(memo.getDate());

            // 종이책이면 페이지 -> 퍼센트 계산
            if (bookRecord.getBookType() == 0) {
                int percent = converterService.pageToPercent(memo.getMarkPage(), book.getTotalPage());
                // 응답으로 보낼 내용에 더하기
                memoList.add(
                        new MemoResponse(dateStr, memo.getMarkPage(), percent, memo.getMemoText(), memo.getUuid()));
            } else { // 전자책, 오디오북이면 퍼센트 -> 페이지 계산
                int page = converterService.percentToPage(memo.getMarkPage(), book.getTotalPage());
                // 응답으로 보낼 내용에 더하기
                memoList.add(new MemoResponse(dateStr, page, memo.getMarkPage(), memo.getMemoText(), memo.getUuid()));
            }
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(), memoList),
                HttpStatus.OK);
    }

    // 책갈피 등록
    public ResponseEntity<DefaultResponse> addBookmark(Long memberId, String isbn, BookmarkRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 해당 독서노트 가져오기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        Bookmark bookmark = bookmarkRepository.findByBookRecordAndUuid(bookRecord, request.uuid());
        if (bookmark != null) {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.CONFLICT, ResponseMessages.CONFLICT_BOOKMARK.get()),
                    HttpStatus.CONFLICT);
        }

        LocationInfo locationInfo = null;
        if (request.mainLocation() != null) {
            // 자주 사용하는 변수 따로 선언
            double latitude = Double.parseDouble(request.mainLocation().latitude());
            double longitude = Double.parseDouble(request.mainLocation().longitude());

            // 주소 없으면 등록
            locationInfo = locationInfoRepository.findByPlaceName(request.mainLocation().placeName());
            if (locationInfo == null) {
                locationInfo = registerLocation(request.mainLocation().placeName(), request.mainLocation().address(),
                        latitude, longitude);
            }

            // 사용자 최근 검색 위치에 등록
            MemberLocation memberLocation = memberLocationRepository.findByMemberAndLocationInfo(member,
                    locationInfo);
            // 최근 위치 검색 기록에 없으면 추가
            if (memberLocation == null) {
                registerRecentLocation(member, locationInfo);
            }
        }

        // 책갈피 페이지에 따른 읽는중, 다읽음 수정
        bookRecord.setMarkPage(request.markPage());
        if (bookRecord.getBookType() == 0) { // 종이책인 경우
            if (request.markPage() >= book.getTotalPage()) { // 책갈피 페이지가 전체 페이지보다 같거나 크면
                bookRecord.setReadingStatus(FINISH_READ); // 다읽음으로 수정
            } else {
                bookRecord.setReadingStatus(READING);
            }
        } else { // 전자책, 오디오북인 경우
            if (request.markPage() >= 100) { // 100% 이상이면
                bookRecord.setReadingStatus(FINISH_READ);
            } else {
                bookRecord.setReadingStatus(READING);
            }
        }

        // 최근 날짜와 비교해 더 최근이면 수정
        LocalDate date = converterService.stringToDate(request.date());
        if (bookRecord.getRecentDate() == null) {
            bookRecord.setRecentDate(date);
        } else {
            if (date.isAfter(bookRecord.getRecentDate())) {
                bookRecord.setRecentDate(date);
            }
        }

        // readingStatus, recentDate 수정 내용 저장
        bookRecordRepository.save(bookRecord);

        // 책갈피 생성, 저장
        bookmark = Bookmark.create(bookRecord, request.uuid(), request.markPage(), locationInfo, date);
        bookmarkRepository.save(bookmark);

        // 독서노트에 마지막으로 수정한 날짜 저장
        bookRecord.setLastEditDate(LocalDateTime.now());

        // 반영
        bookRecordRepository.save(bookRecord);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 책갈피 수정
    public ResponseEntity<DefaultResponse> modifyBookmark(Long memberId, String isbn, BookmarkRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 독서노트 가져오기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 책갈피가 있으면
        Bookmark bookmark = bookmarkRepository.findByBookRecordAndUuid(bookRecord, request.uuid());
        if (bookmark != null) {

            // 수정할 위치를 받았으면
            LocationInfo locationInfo = null;
            if (request.mainLocation() != null) {
                // 자주 사용하는 변수 따로 선언
                double latitude = Double.parseDouble(request.mainLocation().latitude());
                double longitude = Double.parseDouble(request.mainLocation().longitude());

                // 변경 전 위치가 아무데도 사용중이 아니면 삭제하기 위해 받아옴
                LocationInfo preLocation = bookmark.getLocationInfo();

                // 새로 받은 위치가 주소 테이블에 없으면 등록
                locationInfo = locationInfoRepository.findByPlaceName(request.mainLocation().placeName());
                if (locationInfo == null) {
                    locationInfo = registerLocation(request.mainLocation().placeName(),
                            request.mainLocation().address(), latitude, longitude);
                }

                // 사용자 최근 검색 위치에 등록
                MemberLocation memberLocation = memberLocationRepository.findByMemberAndLocationInfo(member,
                        locationInfo);
                // 최근 위치 검색 기록에 없으면 추가
                if (memberLocation == null) {
                    registerRecentLocation(member, locationInfo);
                }

                if (preLocation != null) {
                    // 다른 곳에서 사용중이 아닌 위치면 삭제
                    Long memberLocationCnt = memberLocationRepository.countByLocationInfo(preLocation);
                    Long bookRecordLocationCnt = bookRecordRepository.countByLocationInfo(preLocation);
                    Long bookmarkLocationCnt = bookmarkRepository.countByLocationInfo(preLocation);

                    if (memberLocationCnt + bookRecordLocationCnt + bookmarkLocationCnt <= 0) {
                        locationInfoRepository.delete(preLocation);
                    }
                }
            }

            // 최근 날짜와 비교해 더 최근이면 수정
            bookRecord.setMarkPage(request.markPage());
            LocalDate date = converterService.stringToDate(request.date());
            if (date.isAfter(bookRecord.getRecentDate())) {
                bookRecord.setRecentDate(date);
                bookRecordRepository.save(bookRecord);
            }

            // 페이지 그대로 책갈피 등록
            bookmark = Bookmark.create(bookRecord, request.uuid(), request.markPage(), locationInfo, date);
            bookmarkRepository.save(bookmark);

            // 독서노트에 마지막으로 수정한 날짜 저장
            bookRecord.setLastEditDate(LocalDateTime.now());

            // 반영
            bookRecordRepository.save(bookRecord);
        } else {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.UNREGISTERED_BOOKMARK.get()),
                    HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 책갈피 삭제
    public ResponseEntity<DefaultResponse> deleteBookmark(Long memberId, String isbn, DeleteUuidRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 독서노트 가져오기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 책갈피가 있으면
        Bookmark bookmark = bookmarkRepository.findByBookRecordAndUuid(bookRecord, request.uuid());
        if (bookmark != null) {
            // 위치 정보를 포함하고 있으면
            if (bookmark.getLocationInfo() != null) {
                // 참조하고 있는 위치 받아오기
                LocationInfo deleteLocation = bookmark.getLocationInfo();

                // 다른 곳에서 사용중이 아닌 위치면 삭제
                if (deleteLocation != null) {
                    Long memberLocationCnt = memberLocationRepository.countByLocationInfo(deleteLocation);
                    Long bookRecordLocationCnt = bookRecordRepository.countByLocationInfo(deleteLocation);
                    Long bookmarkLocationCnt = bookmarkRepository.countByLocationInfo(deleteLocation);

                    if (memberLocationCnt + bookRecordLocationCnt + bookmarkLocationCnt <= 0) {
                        locationInfoRepository.delete(deleteLocation);
                    }
                }
            }

            // 책갈피 삭제
            bookmarkRepository.delete(bookmark);
        } else {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_BOOKMARK.get()),
                    HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    // 책갈피 전체조회
    public ResponseEntity<DefaultResponse> getBookmark(Long memberId, String isbn) {
        // 응답으로 보낼 메모 List
        List<BookmarkResponse> bookmarkList = new ArrayList<>();

        // 사용자 받아오기
        Member member = getMemberById(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 독서노트 가져오기
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 한 유저의 한 책에 대한 메모 전체 검색
        List<Bookmark> bookmarks = bookRecord.getBookmarks();
        for (Bookmark bookmark : bookmarks) {
            // 위치 List 저장
            List<String> location = new ArrayList<>();
            location.add(bookmark.getLocationInfo().getPlaceName());
            location.add(bookmark.getLocationInfo().getAddress());
            location.add(String.valueOf(bookmark.getLocationInfo().getLatitude()));
            location.add(String.valueOf(bookmark.getLocationInfo().getLongitude()));

            // 응답에 보낼 데이터들
            int markPage = bookmark.getMarkPage();
            String dateStr = converterService.dateToString(bookmark.getDate());

            // 종이책이면
            if (bookRecord.getBookType() == 0) {
                // 페이지 -> 퍼센트 계산
                int percent = converterService.pageToPercent(markPage, book.getTotalPage());
                bookmarkList.add(new BookmarkResponse(dateStr, markPage, percent, location, bookmark.getUuid()));
            } else { // 전자책, 오디오북이면
                // 퍼센트 -> 페이지 계산
                int page = converterService.percentToPage(markPage, book.getTotalPage());
                bookmarkList.add(new BookmarkResponse(dateStr, page, markPage, location, bookmark.getUuid()));
            }
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(), bookmarkList),
                HttpStatus.OK);
    }

    // 최근 등록 위치 조회
    public ResponseEntity<DefaultResponse> getRecentLocation(Long memberId) {
        // 응답으로 보낼 객체 리스트
        List<RecentLocationResponse> location = new ArrayList<>();

        // 사용자 받아오기
        Member member = getMemberById(memberId);

        // 사용자별 등록 위치 받아오기
        List<MemberLocation> memberLocationList = member.getMemberLocations();
        for (MemberLocation value : memberLocationList) {
            LocationInfo memberLocation = value.getLocationInfo();

            // 응답으로 보낼 객체에 추가
            location.add(new RecentLocationResponse(memberLocation.getLocationId(), memberLocation.getPlaceName(),
                    memberLocation.getAddress(), memberLocation.getLatitude(), memberLocation.getLongitude()));
        }

        if (location.isEmpty()) {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_FOUND_LOCATION.get()),
                    HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(), location),
                HttpStatus.OK);
    }

    // 최근 등록 위치 삭제
    public ResponseEntity<DefaultResponse> deleteRecentLocation(Long memberId, Long locationId) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // 전체 위치에서 검색
        LocationInfo locationInfo = locationInfoRepository.findByLocationId(locationId);
        if (locationInfo != null) {
            // 최근 등록 위치에서 해당 위치가 있는지 확인하고 삭제
            MemberLocation memberLocation = memberLocationRepository.findByMemberAndLocationInfo(member,
                    locationInfo);
            if (memberLocation != null) { // 해당 멤버의 최근 등록 위치에 해당 위치가 있으면
                // 최근 등록 위치에서 해당 위치 삭제
                memberLocationRepository.delete(memberLocation);

                // 다른 곳에서 사용중이 아닌 위치면 locationRepository에서 삭제
                Long bookRecordLocationCnt = bookRecordRepository.countByLocationInfo(locationInfo);
                Long bookmarkLocationCnt = bookmarkRepository.countByLocationInfo(locationInfo);

                if (bookRecordLocationCnt + bookmarkLocationCnt <= 0) {
                    locationInfoRepository.delete(locationInfo);
                }
            } else {
                return new ResponseEntity<>(
                        DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.NOT_LATEST_LOCATION.get()),
                        HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ResponseMessages.UNREGISTERED_LOCATION.get()),
                    HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    /** 헬퍼 클래스 및 메소드 **/

    // 중복 책 검색 및 등록
    private Book registerBook(String isbn, BookCreateRequest bookInformation) {
        LocalDate publicationDate = converterService.stringToDate(bookInformation.publicationDate());
        return bookRepository.save(
                Book.create(isbn, bookInformation.cover(), bookInformation.title(), bookInformation.author(),
                        bookInformation.categoryName(), bookInformation.totalPage(),
                        bookInformation.publisher(), publicationDate));
    }

    // 위치 등록 및 반환
    private LocationInfo registerLocation(String placeName, String address, double latitude, double longitude) {
        return locationInfoRepository.save(LocationInfo.create(placeName, address, latitude, longitude));
    }

    // 최근 위치 검색 기록 추가(레코드가 5개 초과되면 삭제하는 코드까지)
    private void registerRecentLocation(Member member, LocationInfo locationInfo) {
        // 현재 사용자의 최근 위치 검색 레코드가 5개 이상인지 확인하고, 5개 이상이면 날짜 순으로 정렬 후 가장 오래된 레코드를 삭제
        if (memberLocationRepository.countAllByMember(member) >= 5) {
            List<MemberLocation> memberLocationList = memberLocationRepository.findByMemberOrderByDateAsc(member);
            memberLocationRepository.delete(memberLocationList.get(0));
        }

        memberLocationRepository.save(MemberLocation.create(member, locationInfo, LocalDateTime.now()));
    }

    // 한줄평에 대한 반응 레코드 등록(BookReviewReaction)
    private BookReviewReaction registerBookReviewReaction(BookReview bookReview) {
        // 반응 레코드 등록
        BookReviewReaction reaction = bookReviewReactionRepository.save(BookReviewReaction.create());

        // FK 설정
        bookReview.setBookReviewReaction(reaction);
        bookReviewRepository.save(bookReview);

        return reaction;
    }

    // 한줄평에 대한 반응 레코드 등록(BookReviewReviewer)
    private BookReviewReviewer registerBookReviewReviewer(BookReview bookReview, Member member) {
        // 반응 여부 false, 종류 0인 레코드 생성 및 카운트 수정에 사용할 용도로 반환
        return bookReviewReviewerRepository.save(BookReviewReviewer.create(bookReview, member));
    }

    // 도서정보 호출 API 데이터를 가져오는 메소드
    private String getBookDate(String isbn) throws IOException {
        StringBuilder result = new StringBuilder();
        String urlStr = createURL(isbn);

        // URL 객체 생성 및 GET 요청
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");

        // 응답 데이터를 받아올 버퍼 생성
        // try-with-resources (버퍼를 명시적으로 close 하지 않고, 간결하게 처리하기 위함)
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8))) {
            String returnLine;

            // 응답 데이터 한 줄씩 기록
            while ((returnLine = br.readLine()) != null) {
                result.append(returnLine + "\n\r");
            }
        } finally {
            urlConnection.disconnect();
        }

        // 응답 데이터 String 형식으로 변환 후 반환
        return result.toString();
    }

    // yaml 파일 읽고 url 작성
    private String createURL(String isbn) {
        String baseUrl = null;
        String ttbKey = null;
        String output = null;
        String version = null;
        String itemIdType = null;

        Yaml yaml = new Yaml();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("aladinConfig.yml");
        if (inputStream != null) {
            Map<String, Object> yamlData = yaml.load(inputStream);
            Map<String, Object> aladinApi = (Map<String, Object>) yamlData.get("aladin_api");

            baseUrl = (String) aladinApi.get("lookup_url");
            ttbKey = (String) aladinApi.get("ttbkey");
            output = (String) ((Map<String, Object>) aladinApi.get("default_params")).get("output");
            version = (String) ((Map<String, Object>) aladinApi.get("default_params")).get("version");
            itemIdType = (String) aladinApi.get("item_id_type");
        }

        return String.format("%s?ttbkey=%s&itemIdType=%s&ItemId=%s&output=%s&Version=%s", baseUrl, ttbKey, itemIdType,
                isbn, output, version);
    }

    // 알라딘 API 데이터 파싱
    private SearchBookResponse dataParsing(String jsonData) {
        try {
            JSONObject jsonResult, jsonResultSub;
            JSONParser jsonParser = new JSONParser();

            // 파싱할 json 문자열
            JSONObject jsonString = (JSONObject) jsonParser.parse(jsonData);

            // item 데이터 받기
            JSONArray jsonArray = (JSONArray) jsonString.get("item");
            jsonResult = (JSONObject) jsonArray.get(0);

            // subInfo 데이터 받기
            jsonResultSub = (JSONObject) jsonResult.get("subInfo");

            return new SearchBookResponse(jsonResult.get("cover").toString(),
                    jsonResult.get("title").toString(),
                    jsonResult.get("author").toString(),
                    jsonResult.get("categoryName").toString(),
                    UNREGISTERED,
                    jsonResult.get("publisher").toString(),
                    jsonResult.get("pubDate").toString(),
                    Integer.parseInt(jsonResultSub.get("itemPage").toString()),
                    jsonResult.get("description").toString(), 0, null, null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // 선택 리뷰 리스트 생성 메소드
    private List<Integer> getSelectedReviewList(List<SelectReview> selectedReviews) {
        if (selectedReviews.isEmpty()) {
            return null;
        }

        // max 값 위치를 체크하기 위한 리스트
        int[] integerArray = new int[31];

        // selectedReviews 문자열 받아오기
        for (SelectReview review : selectedReviews) {
            // , 기준으로 split하고 해당하는 인덱스 위치 1 더하기(max 값으로 정렬 위함)
            for (String i : review.getSelectReviewCode().split(",")) {
                integerArray[Integer.parseInt(i)]++;
            }
        }

        // max 값 체크 리스트 길이만큼 스트림을 생성해 내림차순으로 정렬, 10개(0 제외)만 뽑아 List로 반환
        return IntStream.range(0, integerArray.length)
                .filter(i -> integerArray[i] > 0)
                .boxed()
                .sorted((i, j) -> integerArray[j] - integerArray[i])
                .limit(10)
                .collect(Collectors.toList());
    }

    // 댓글 최신순 리스트 생성 메소드
    private List<String> getLatestCommentList(List<BookReview> bookReviews) {
        if (bookReviews.isEmpty()) {
            return null;
        }

        // BookReview 스트림을 생성해 최대 5개까지 comment 받아오기
        return bookReviews.stream()
                .map(BookReview::getReviewText)
                .limit(5)
                .collect(Collectors.toList());
    }

    // 카테고리 이름 수정 메소드
    private String extractCategory(String categoryFullName) {
        if (categoryFullName.contains("소설")) {
            return categoryNameList.get(0);
        }

        for (String category : categoryNameList) {
            if (categoryFullName.contains(category)) {
                return category;
            }
        }

        return "기타";
    }


    // 캐시 사용을 위한 메소드
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