package com.codecozy.server.service;

import com.codecozy.server.context.StatusCode;
import com.codecozy.server.dto.request.*;
import com.codecozy.server.dto.response.*;
import com.codecozy.server.entity.*;
import com.codecozy.server.repository.*;
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

    // 사용자가 독서노트 추가 시 실행 (책 등록, 위치 등록, 독서노트 등록, 최근 검색 위치 등록)
    public ResponseEntity<DefaultResponse> createBook(Long memberId, String isbn, ReadingBookCreateRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 자주 사용되는 객체 재사용 용도로 선언
        LocationList locationList = null;
        MemberLocation memberLocation = null;

        // memberId와 isbn을 이용해 사용자별 리뷰 등록 책이 중복되었는지 검사
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);
        if (bookRecord != null && bookRecord.getBookType() != -1) { // -1: reading_status가 '읽고싶은'(0)인 경우
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "이미 등록한 독서 노트입니다."),
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
        bookRecord = BookRecord.create(member, book, request.readingStatus(), request.bookType(), locationList, request.isMine(), request.startDate(), request.recentDate());
        bookRecordRepository.save(bookRecord);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
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
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 독서노트 조회
    public ResponseEntity<DefaultResponse> getReadingNote(Long memberId, String isbn) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

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
        int readingPercent = (int) ((double) readPage / totalPage * 100);
        String keywordReview = bookRecord.getKeyWord();
        String commentReview = bookReviewRepository.findByMemberAndBook(member, book).getReviewText();

        String selectReviewStr = keywordReviewRepository.findByMemberAndBook(member, book).getSelectReviewCode();
        String[] selectReviewStrList = selectReviewStr.split(",");
        List<Integer> selectReview = new ArrayList<>();
        for (String s : selectReviewStrList) {
            selectReview.add(Integer.valueOf(s));
        }

        boolean isMine = bookRecord.isMine();
        int bookType = bookRecord.getBookType();
        int readingStatus = bookRecord.getReadingStatus();
        String mainLocation = bookRecord.getLocationList().getPlaceName();
        String startDate = bookRecord.getStartDate();
        String recentDate = bookRecord.getRecentDate();

        List<Bookmark> bookmarkList = bookmarkRepository.findTop3ByMemberAndBookOrderByDateDesc(member, book);
        List<GetBookmarkPreviewResponse> bookmarks = new ArrayList<>();
        for (Bookmark bookmark : bookmarkList) {
            int markPercent = (int) ((double) bookmark.getMarkPage() / totalPage * 100);
            bookmarks.add(new GetBookmarkPreviewResponse(
                    bookmark.getDate(),
                    bookmark.getMarkPage(),
                    markPercent,
                    bookmark.getLocationList().getPlaceName(),
                    bookmark.getUuid()
            ));
        }

        List<Memo> memoList = memoRepository.findTop3ByMemberAndBookOrderByDateDesc(member, book);
        List<GetMemoResponse> memos = new ArrayList<>();
        for (Memo memo : memoList) {
            int markPercent = (int) ((double) memo.getMarkPage() / totalPage * 100);
            memos.add(new GetMemoResponse(
               memo.getDate(),
               memo.getMarkPage(),
               markPercent,
               memo.getMemoText(),
               memo.getUuid()
            ));
        }

        List<PersonalDictionary> personalDictionaryList = personalDictionaryRepository.findTop3ByMemberAndBookOrderByNameAsc(member, book);
        List<GetPersonalDictionaryPreviewResponse> characters = new ArrayList<>();
        for (PersonalDictionary personalDictionary : personalDictionaryList) {
            characters.add(new GetPersonalDictionaryPreviewResponse(
                    personalDictionary.getEmoji(),
                    personalDictionary.getName(),
                    personalDictionary.getPreview()
            ));
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공", new GetReadingNoteResponse(
                                cover,
                                title,
                                author,
                                categoryName,
                                totalPage,
                                readPage,
                                readingPercent,
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
    public ResponseEntity<DefaultResponse> modifyReadingStatus(Long memberId, String isbn, int readingStatus) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // 해당 책 찾기
        Book book = bookRepository.findByIsbn(isbn);
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 변경 후 저장
        bookRecord.setReadingStatus(readingStatus);
        bookRecordRepository.save(bookRecord);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
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
                DefaultResponse.from(StatusCode.OK, "성공"),
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
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 읽은 페이지 변경
    public ResponseEntity<DefaultResponse> modifyReadingPage(Long memberId, String isbn, ModifyPageRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // 해당 책 찾기
        Book book = bookRepository.findByIsbn(isbn);
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 페이지 단위일 경우
        if (request.type()) {
            bookRecord.setMarkPage(request.page());
        }
        // 퍼센트 단위일 경우
        else {
            int totalPage = book.getTotalPage();
            int markPage = (int) (request.page() / 100.0 * totalPage);
            bookRecord.setMarkPage(markPage);
        }

        // 변경사항 반영
        bookRecordRepository.save(bookRecord);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
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
        BookReview bookReview = bookReviewRepository.findByMemberAndBook(commentMember, book);

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
        BookReviewReviewer bookReviewReviewer = bookReviewReviewerRepository.findByBookReviewAndMember(bookReview, member);
        // 한줄평 반응을 처음 남기는 유저라면
        if (bookReviewReviewer == null) {
            // 반응(신고) 여부, 인 인스턴스 생성 및 저장
            bookReviewReviewer = BookReviewReviewer.create(bookReview, member);
            bookReviewReviewerRepository.save(bookReviewReviewer);
            // 검색해서 저장 후 카운트 수정에 사용
            bookReviewReviewer = bookReviewReviewerRepository.findByBookReview(bookReview);
        }

        // 0이면 부적절한 리뷰, 1이면 스팸성 리뷰 카운트 올리고, 신고 여부와 종류 수정
        if (!bookReviewReviewer.isReport()) {
            bookReviewReaction.setReportCountUp(request.reportType());
            bookReviewReviewer.setIsReportReverse();
            bookReviewReviewer.setReportType(request.reportType());
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
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
            // 등록되지 않은 책이면 새로 등록
            book = Book.create(isbn, request.cover(), request.title(), request.author(), request.category(), Integer.parseInt(request.totalPage()));
            bookRepository.save(book);
            book = bookRepository.findByIsbn(isbn);
        }

        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);
        if (bookRecord != null) {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "독서 노트에 등록한 도서는 읽고 싶은 도서로 등록할 수 없습니다."),
                    HttpStatus.CONFLICT);
        }
        // 읽고싶은 책 등록 시에는 독서노트 생성 전이므로 member, book, reading_status 외에는 임시 데이터로 book_record 레코드 생성 후 저장
        bookRecordRepository.save(BookRecord.create(member, book));

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
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
        BookReview bookReview = bookReviewRepository.findByMemberAndBook(commentMember, book);

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
        BookReviewReviewer bookReviewReviewer = bookReviewReviewerRepository.findByBookReviewAndMember(bookReview, member);
        // 현재 한줄평에 반응을 처음 남기는 유저라면
        if (bookReviewReviewer == null) {
            // 반응 여부 false, 종류 0인 인스턴스 생성 및 저장
            bookReviewReviewer = BookReviewReviewer.create(bookReview, member);
            bookReviewReviewerRepository.save(bookReviewReviewer);
            // 검색해서 저장 후 카운트 수정에 사용
            bookReviewReviewer = bookReviewReviewerRepository.findByBookReview(bookReview);
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
                DefaultResponse.from(StatusCode.OK, "성공"),
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
        BookReview bookReview = bookReviewRepository.findByMemberAndBook(commentMember, book);

        if (bookReview != null) {
            // 한줄평 객체를 이용한 검색으로 bookReviewReaction 테이블에서 한줄평에 대한 반응 카운트 레코드 검색
            BookReviewReaction bookReviewReaction = bookReviewReactionRepository.findByBookReview(bookReview);
            // 한줄평에 대한 반응 여부와 종류를 담은 레코드 검색
            BookReviewReviewer bookReviewReviewer = bookReviewReviewerRepository.findByBookReviewAndMember(bookReview, member);

            if ((bookReviewReaction != null) && (bookReviewReviewer != null)) {
                if (bookReviewReviewer.isReaction()) { // 현재 한줄평에 반응을 남긴적이 있으면
                    // 기존 반응 카운트 하나 내리기
                    bookReviewReaction.setReactionCountDown(bookReviewReviewer.getReactionCode());

                    // 새 반응 종류 저장하기
                    bookReviewReviewer.setReactionCode(request.commentReaction());
                    // 새 반응 카운트 하나 올리기
                    bookReviewReaction.setReactionCountUp(request.commentReaction());
                }
                else {
                    return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "해당 한줄평에 남긴 반응이 없습니다."),
                            HttpStatus.CONFLICT);
                }
            }
            else {
                return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "해당 한줄평에 남긴 반응이 없습니다."),
                        HttpStatus.CONFLICT);
            }
        }
        else {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "해당 한줄평이 없습니다."),
                    HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
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
        BookReview bookReview = bookReviewRepository.findByMemberAndBook(commentMember, book);

        if (bookReview != null) {
            // 한줄평 객체를 이용한 검색으로 bookReviewReaction 테이블에서 한줄평에 대한 반응 카운트 레코드 검색
            BookReviewReaction bookReviewReaction = bookReviewReactionRepository.findByBookReview(bookReview);
            // 한줄평에 대한 반응 여부와 종류를 담은 레코드 검색
            BookReviewReviewer bookReviewReviewer = bookReviewReviewerRepository.findByBookReviewAndMember(bookReview, member);

            if ((bookReviewReaction != null) && (bookReviewReviewer != null)) {
                if (bookReviewReviewer.isReaction()) { // 현재 한줄평에 반응을 남긴적이 있으면
                    // 기존 반응 카운트 하나 내리기
                    bookReviewReaction.setReactionCountDown(bookReviewReviewer.getReactionCode());

                    // 반응이 하나도 없으면 반응 카운트 레코드 삭제
                    int cnt = bookReviewReaction.getHeartCount() + bookReviewReaction.getGoodCount() + bookReviewReaction.getWowCount()
                            + bookReviewReaction.getSadCount() + bookReviewReaction.getAngryCount();
                    if (cnt <= 0) bookReviewReactionRepository.delete(bookReviewReaction);

                    // 신고 여부도 없다면 반응 여부, 종류 레코드 삭제
                    if (!bookReviewReviewer.isReport()) bookReviewReviewerRepository.delete(bookReviewReviewer);
                }
                else {
                    return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "해당 한줄평에 남긴 반응이 없습니다."),
                            HttpStatus.CONFLICT);
                }
            }
            else {
                return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "해당 한줄평에 남긴 반응이 없습니다."),
                        HttpStatus.CONFLICT);
            }
        }
        else {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "해당 한줄평이 없습니다."),
                    HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 리뷰 작성 (키워드, 선택 리뷰, 한줄평 각 테이블에 추가)
    public ResponseEntity<DefaultResponse> createReview(Long memberId, String isbn, ReviewCreateRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 키워드 리뷰가 있으면 독서 노트에 수정
        if (request.keyword() != null) {
            BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);
            bookRecord.setKeyWord(request.keyword());
            bookRecordRepository.save(bookRecord);
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

    // 리뷰 전체 수정 (키워드, 선택 리뷰, 한줄평)
    public ResponseEntity<DefaultResponse> modifyReview(Long memberId, String isbn, ReviewCreateRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 키워드 리뷰가 있으면 독서 노트에 수정
        if (request.keyword() != null) {
            BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);
            if (bookRecord == null) { // 독서 노트가 없으면 충돌 메세지
                return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "독서 노트가 없습니다."),
                        HttpStatus.CONFLICT);
            }

            // 키워드 설정
            bookRecord.setKeyWord(request.keyword());
            bookRecordRepository.save(bookRecord);
        }

        // 선택 리뷰가 있으면 레코드 수정
        if (request.select() != null) {
            // 이미 등록한 선택 리뷰면 CONFLICT 응답
            KeywordReview keywordReview = keywordReviewRepository.findByMemberAndBook(member, book);

            String selected = "";
            for (int id = 0; id < request.select().size(); id++) {
                selected += request.select().get(id) + ",";
            }

            // 이전에 선택 리뷰를 등록한 경우
            if (keywordReview != null) {
                // 선택 리뷰 수정
                keywordReview.setSelectReviewCode(selected);
                keywordReviewRepository.save(keywordReview);
            }
            else {
                // 이전에 선택 리뷰를 등록하지 않은 경우 새로 생성
                keywordReview = KeywordReview.create(member, book, selected);
                keywordReviewRepository.save(keywordReview);
            }
        }

        // 한줄평이 있으면 레코드 수정
        if (request.comment() != null) {

            BookReview bookReview = bookReviewRepository.findByMemberAndBook(member, book);
            if (bookReview != null) {
                // 한줄평 수정
                bookReview.setReviewText(request.comment());
                bookReviewRepository.save(bookReview);
            }
            else {
                // 이전에 한줄평을 등록하지 않았거나 삭제한 경우 새로 생성
                bookReview = BookReview.create(member, book, request.comment());
                bookReviewRepository.save(bookReview);
            }
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 리뷰 전체 삭제 (키워드, 선택 리뷰, 한줄평)
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
        }
        else {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "해당 독서노트가 없습니다."),
                    HttpStatus.CONFLICT);
        }

        // 선택리뷰 찾고 삭제
        KeywordReview keywordReview = keywordReviewRepository.findByMemberAndBook(member, book);
        if (keywordReview != null) keywordReviewRepository.delete(keywordReview);

        // 한줄평 찾기
        BookReview bookReview = bookReviewRepository.findByMemberAndBook(member, book);

        // 한줄평 반응 종류 찾고 삭제
        BookReviewReaction bookReviewReaction = bookReviewReactionRepository.findByBookReview(bookReview);
        if (bookReviewReaction != null) bookReviewReactionRepository.delete(bookReviewReaction);
        // 한줄평 반응 여부 찾고 삭제
        BookReviewReviewer bookReviewReviewer = bookReviewReviewerRepository.findByBookReview(bookReview);
        if (bookReviewReviewer != null) bookReviewReviewerRepository.delete(bookReviewReviewer);

        // 한줄평 삭제
        if (bookReview != null) bookReviewRepository.delete(bookReview);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 한줄평 삭제
    public ResponseEntity<DefaultResponse> deleteComment(Long memberId, String isbn) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 한줄평 찾기
        BookReview bookReview = bookReviewRepository.findByMemberAndBook(member, book);
        // 한줄평이 있으면
        if (bookReview != null) {
            // 한줄평에 대한 반응 종류, 여부 레코드 찾기
            BookReviewReaction bookReviewReaction = bookReviewReactionRepository.findByBookReview(bookReview);
            BookReviewReviewer bookReviewReviewer = bookReviewReviewerRepository.findByBookReview(bookReview);

            // 해당 한줄평에 대한 반응 종류 레코드 지우기
            if (bookReviewReaction != null) bookReviewReactionRepository.deleteById(bookReview);
            // 해당 한줄평에 대한 반응 여부 레코드 지우기
            if (bookReviewReviewer != null) bookReviewReviewerRepository.deleteById(bookReview);

            // 한줄평 지우기
            bookReviewRepository.delete(bookReview);
        }
        else {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "해당 한줄평이 없습니다."),
                    HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
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
        if (bookRecord != null) bookRecord.setStartDate(startDate);
        else {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "해당 독서노트가 없습니다."),
                    HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
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
        if (bookRecord != null) bookRecord.setRecentDate(recentDate);
        else {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "해당 독서노트가 없습니다."),
                    HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
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
    public ResponseEntity<DefaultResponse> modifyMainLocation(Long memberId, String isbn, LocationRequest request) {
        // 사용자 받아오기
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
    public ResponseEntity<DefaultResponse> deleteMainLocation(Long memberId, String isbn) {
        // 사용자 받아오기
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
    public ResponseEntity<DefaultResponse> addpersonalDictionary(Long memberId, String isbn, PersonalDictionaryRequest request) {
        // 사용자 받아오기
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
            personalDictionary = PersonalDictionary.create(member, book, request.name(), Integer.parseInt(request.emoji()), request.preview(), request.description());
            personalDictionaryRepository.save(personalDictionary);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 인물사전 수정
    public ResponseEntity<DefaultResponse> modifyPersonalDictionary(Long memberId, String isbn, PersonalDictionaryRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 해당 인물이 있는지 검색
        PersonalDictionary personalDictionary = personalDictionaryRepository.findByMemberAndBookAndName(member, book, request.name());

        // 중복된 인물이면 (이름이 중복됐으면)
        if (personalDictionary != null) {
            // 인물사전에서 수정 등록
            personalDictionary = PersonalDictionary.create(member, book, request.name(), Integer.parseInt(request.emoji()), request.preview(), request.description());
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

    // 인물사전 삭제
    public ResponseEntity<DefaultResponse> deletePersonalDictionary(Long memberId, String isbn, DeletePersonalDictionaryRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 해당 인물이 있는지 검색
        PersonalDictionary personalDictionary = personalDictionaryRepository.findByMemberAndBookAndName(member, book, request.characterName());
        if (personalDictionary != null) {
            // 인물사전에서 삭제
            personalDictionaryRepository.delete(personalDictionary);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);

    }

    // 인물사전 전체조회
    public ResponseEntity<DefaultResponse> getPersonalDictionary(Long memberId, String isbn) {
        // 응답으로 보낼 인물사전 List
        List<GetPersonalDictionaryResponse> personalDictionaryList = new ArrayList<>();

        // 사용자 받아오기
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
    public ResponseEntity<DefaultResponse> addMemo(Long memberId, String isbn, MemoRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        Memo memo = memoRepository.findByMemberAndBookAndUuid(member, book, request.uuid());
        if (memo != null) {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "이미 등록한 메모입니다."),
                    HttpStatus.CONFLICT);
        }

        memo = Memo.create(member, book, request.uuid(), request.markPage(), request.date(), request.memoText());
        memoRepository.save(memo);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 메모 수정
    public ResponseEntity<DefaultResponse> modifyMemo(Long memberId, String isbn, MemoRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 메모가 있으면
        Memo memo = memoRepository.findByMemberAndBookAndUuid(member, book, request.uuid());
        if (memo != null) {
            memo = Memo.create(member, book, request.uuid(), request.markPage(), request.date(), request.memoText());
            memoRepository.save(memo);
        }
        else {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "등록하지 않은 메모입니다."),
                    HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 메모 삭제
    public ResponseEntity<DefaultResponse> deleteMemo(Long memberId, String isbn, DeleteUuidRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 메모가 있으면
        Memo memo = memoRepository.findByMemberAndBookAndUuid(member, book, request.uuid());
        if (memo != null) {
            // 메모 삭제
            memoRepository.delete(memo);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 메모 전체조회
    public ResponseEntity<DefaultResponse> getMemo(Long memberId, String isbn) {
        // 응답으로 보낼 메모 List
        List<GetMemoResponse> memoList = new ArrayList<>();

        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 한 유저의 한 책에 대한 메모 전체 검색
        List<Memo> memos = memoRepository.findAllByMemberAndBook(member, book);
        for (int i = 0; i < memos.size(); i++) {
            Memo memo = memos.get(i);

            // 종이책이면
            if (bookRecordRepository.findByMemberAndBook(member, book).getBookType() == 0) {
                // 페이지 -> 퍼센트 계산
                int percent = (int) Math.round(100.0 * memo.getMarkPage() / book.getTotalPage());

                // 응답으로 보낼 내용에 더하기
                memoList.add(new GetMemoResponse(memo.getDate(), memo.getMarkPage(), percent, memo.getMemoText(), memo.getUuid()));
            }
            else { // 전자책, 오디오북이면 퍼센트 -> 페이지 계산
                // 페이지 -> 퍼센트 계산
                int page = (int) Math.round(book.getTotalPage() / 100.0 / memo.getMarkPage());

                // 응답으로 보낼 내용에 더하기
                memoList.add(new GetMemoResponse(memo.getDate(), page, memo.getMarkPage(), memo.getMemoText(), memo.getUuid()));

            }
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공", memoList),
                HttpStatus.OK);
    }

    // 책갈피 등록
    public ResponseEntity<DefaultResponse> addBookmark(Long memberId, String isbn, BookmarkRequest request) {
        // 사용자 받아오기
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

        bookmark = Bookmark.create(member, book, request.uuid(), request.markPage(), locationList, request.date());
        bookmarkRepository.save(bookmark);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 책갈피 수정
    public ResponseEntity<DefaultResponse> modifyBookmark(Long memberId, String isbn, BookmarkRequest request) {
        // 사용자 받아오기
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

            // 페이지 그대로 책갈피 등록
            bookmark = Bookmark.create(member, book, request.uuid(), request.markPage(), locationList, request.date());
            bookmarkRepository.save(bookmark);
        }
        else {
            return new ResponseEntity<>(DefaultResponse.from(StatusCode.CONFLICT, "등록하지 않은 책갈피입니다."),
                    HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 책갈피 삭제
    public ResponseEntity<DefaultResponse> deleteBookmark(Long memberId, String isbn, DeleteUuidRequest request) {
        // 사용자 받아오기
        Member member = memberRepository.findByMemberId(memberId);

        // isbn으로 책 검색
        Book book = bookRepository.findByIsbn(isbn);

        // 책갈피가 있으면
        Bookmark bookmark = bookmarkRepository.findByMemberAndBookAndUuid(member, book, request.uuid());
        if (bookmark != null) {
            // 참조하고 있는 위치 받아오기
            LocationList deleteLocation = locationRepository.findByLocationId(bookmark.getLocationList().getLocationId());

            // 다른 곳에서 사용중이 아닌 위치면 삭제
            if (deleteLocation != null) {
                Long memberLocationCnt = memberLocationRepository.countByLocationList(deleteLocation);
                Long bookRecordLocationCnt = bookRecordRepository.countByLocationList(deleteLocation);
                Long bookmarkLocationCnt = bookmarkRepository.countByLocationList(deleteLocation);

                if (memberLocationCnt + bookRecordLocationCnt + bookmarkLocationCnt <= 0) {
                    locationRepository.delete(deleteLocation);
                }
            }

            // 책갈피 삭제
            bookmarkRepository.delete(bookmark);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 책갈피 전체조회
    public ResponseEntity<DefaultResponse> getBookmark(Long memberId, String isbn) {
        // 응답으로 보낼 메모 List
        List<GetBookmarkResponse> bookmarkList = new ArrayList<>();

        // 사용자 받아오기
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

            // 종이책이면
            if (bookRecordRepository.findByMemberAndBook(member, book).getBookType() == 0) {
                // 페이지 -> 퍼센트 계산
                int percent = (int) Math.round(100.0 * bookmark.getMarkPage() / book.getTotalPage());

                // 응답으로 보낼 내용에 더하기
                bookmarkList.add(new GetBookmarkResponse(bookmark.getDate(), bookmark.getMarkPage(), percent, location, bookmark.getUuid()));
            }
            else { // 전자책, 오디오북이면 퍼센트 -> 페이지 계산
                // 페이지 -> 퍼센트 계산
                int page = (int) Math.round(book.getTotalPage() / 100.0 / bookmark.getMarkPage());

                // 응답으로 보낼 내용에 더하기
                bookmarkList.add(new GetBookmarkResponse(bookmark.getDate(), page, bookmark.getMarkPage(), location, bookmark.getUuid()));
            }
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공", bookmarkList),
                HttpStatus.OK);
    }

    // 전체 위치 조회
    public ResponseEntity<DefaultResponse> getAllLocation(Long memberId, GetAllLocationRequest request) {
        // 응답으로 보낼 객체 리스트
        List<GetAllLocationResponse> locationInfo = new ArrayList<>();

        int orderNumber = request.orderNumber();

        // 사용자 받아오기
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
    public ResponseEntity<DefaultResponse> getRecentLocation(Long memberId) {
        // 응답으로 보낼 객체 리스트
        List<GetRecentLocationResponse> location = new ArrayList<>();

        // 사용자 받아오기
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
    public ResponseEntity<DefaultResponse> deleteRecentLocation(Long memberId, DeleteRecentLocationRequest request) {
        // 사용자 받아오기
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