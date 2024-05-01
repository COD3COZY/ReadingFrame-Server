package com.codecozy.server.service;

import com.codecozy.server.context.StatusCode;
import com.codecozy.server.dto.response.DefaultResponse;
import com.codecozy.server.dto.response.GetFinishReadResponse;
import com.codecozy.server.dto.response.GetMainBooksResponse;
import com.codecozy.server.dto.response.GetMainResponse;
import com.codecozy.server.dto.response.GetReadingResponse;
import com.codecozy.server.dto.response.GetWantToReadResponse;
import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.Member;
import com.codecozy.server.repository.BookRecordRepository;
import com.codecozy.server.repository.BookRepository;
import com.codecozy.server.repository.BookReviewRepository;
import com.codecozy.server.repository.MemberRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HomeService {
    private final ConverterService converterService;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final BookRecordRepository bookRecordRepository;
    private final BookReviewRepository bookReviewRepository;

    // 독서 상태 상수 값
    private final int UNREGISTERED = -1;    // 미등록
    private final int WANT_TO_READ = 0;     // 읽고 싶은
    private final int READING = 1;          // 읽는 중
    private final int FINISH_READ = 2;      // 다 읽음

    // 메인 화면 조회
    public ResponseEntity<DefaultResponse> getMainPage(Long memberId) {
        // 해당 유저 가져오기
        Member member = memberRepository.findByMemberId(memberId);

        /** 전체 책 리스트 가져오기 **/
        List<GetMainBooksResponse> booksList = new ArrayList<>();

        // 읽고 있는 책 리스트 가져오기 (최대 10개)
        List<BookRecord> readingBooks = bookRecordRepository.findTop10ByMemberAndReadingStatusAndIsHidden(member,
                READING, false);
        // dto 값 넣기
        for (BookRecord bookRecord : readingBooks) {
            Book book = bookRecord.getBook();

            int totalPage = book.getTotalPage();
            int readPage = bookRecord.getMarkPage();
            int readingPercent = (int) ((double) readPage / totalPage * 100);

            booksList.add(new GetMainBooksResponse(
                    READING,
                    book.getIsbn(),
                    book.getCover(),
                    book.getTitle(),
                    book.getAuthor(),
                    readingPercent,
                    totalPage,
                    readPage,
                    bookRecord.isMine()
            ));
        }

        // 읽고 싶은 책 리스트 가져오기 (최대 10개)
        List<BookRecord> wantToReadBooks = bookRecordRepository.findTop10ByMemberAndReadingStatus(member, WANT_TO_READ);

        // dto 값 넣기
        for (BookRecord bookRecord : wantToReadBooks) {
            Book book = bookRecord.getBook();

            booksList.add(new GetMainBooksResponse(
                    WANT_TO_READ,
                    book.getIsbn(),
                    book.getCover(),
                    book.getTitle(),
                    book.getAuthor(),
                    -1,
                    -1,
                    -1,
                    null
            ));
        }

        // 다 읽은 책 리스트 가져오기 (전체)
        List<BookRecord> finishReadBooks = bookRecordRepository.findAllByMemberAndReadingStatus(member, FINISH_READ);

        // dto 값 넣기
        for (BookRecord bookRecord : finishReadBooks) {
            Book book = bookRecord.getBook();

            booksList.add(new GetMainBooksResponse(
                    FINISH_READ,
                    book.getIsbn(),
                    book.getCover(),
                    book.getTitle(),
                    book.getAuthor(),
                    -1,
                    -1,
                    -1,
                    bookRecord.isMine()
            ));
        }

        // 읽고 싶은 책 개수 계산
        List<BookRecord> tempWantToReadList = bookRecordRepository.findAllByMemberAndReadingStatus(member,
                WANT_TO_READ);
        int wantToReadCount = tempWantToReadList.size();

        // 읽고 있는 책 개수 계산
        List<BookRecord> tempReadingList = bookRecordRepository.findAllByMemberAndReadingStatus(member, READING);
        int readingCount = tempReadingList.size();

        // 응답 보내기
        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공", new GetMainResponse(booksList, wantToReadCount,
                        readingCount)),
                HttpStatus.OK);
    }

    // 읽고 싶은 책 조회
    public ResponseEntity<DefaultResponse> getWantToReadBooks(Long memberId) {
        // 해당 유저 가져오기
        Member member = memberRepository.findByMemberId(memberId);

        // 읽고 싶은 책 목록 가져오기
        List<BookRecord> bookRecordList = bookRecordRepository.findAllByMemberAndReadingStatus(member,
                WANT_TO_READ);

        // 해당 값 dto 정보 담기
        List<GetWantToReadResponse> wantToReadBooks = new ArrayList<>();
        for (BookRecord bookRecord : bookRecordList) {
            // 해당 책 정보 꺼내기
            Book book = bookRecord.getBook();

            wantToReadBooks.add(new GetWantToReadResponse(
                    book.getIsbn(),
                    book.getCover(),
                    book.getTitle(),
                    book.getAuthor(),
                    converterService.categoryNameToCode(book.getCategory())
            ));
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공", wantToReadBooks),
                HttpStatus.OK);
    }

    // 읽고 있는 책 조회 시 필요한 정보들을 가져오는 메소드
    private GetReadingResponse getReadingBookInfo(Member member, BookRecord bookRecord) {
        // 책 정보 가져오기
        Book book = bookRecord.getBook();

        // readingPercent 계산
        int totalPage = book.getTotalPage();
        int readPage = bookRecord.getMarkPage();
        int readingPercent = (int) ((double) readPage / totalPage * 100);

        // 리뷰의 유무 가져오기
        Boolean isWriteReview = bookReviewRepository.findByMemberAndBook(member, book) != null;

        return new GetReadingResponse(
                book.getIsbn(),
                book.getCover(),
                book.getTitle(),
                book.getAuthor(),
                readingPercent,
                totalPage,
                readPage,
                bookRecord.isHidden(),
                converterService.categoryNameToCode(book.getCategory()),
                bookRecord.getBookType(),
                bookRecord.isMine(),
                isWriteReview);
    }

    // 읽고 있는 책 조회
    public ResponseEntity<DefaultResponse> getReadingBooks(Long memberId) {
        // 해당 유저 가져오기
        Member member = memberRepository.findByMemberId(memberId);

        // 1. 읽고 있는 책 중, 숨기지 않은 책들만 먼저 가져오기
        List<BookRecord> notHiddenBooks = bookRecordRepository.findAllByMemberAndReadingStatusAndIsHidden(member,
                READING, false);

        // 2. 읽고 있는 책 중, 숨긴 책들도 가져오기
        List<BookRecord> hiddenBooks = bookRecordRepository.findAllByMemberAndReadingStatusAndIsHidden(member,
                READING, true);

        // dto 정보 넣기
        List<GetReadingResponse> readingBooks = new ArrayList<>();

        // 숨기지 않은 책들 먼저 정보 넣기
        for (BookRecord bookRecord : notHiddenBooks) {
            readingBooks.add(getReadingBookInfo(member, bookRecord));
        }

        // 숨긴 책들 정보 넣기
        for (BookRecord bookRecord : hiddenBooks) {
            readingBooks.add(getReadingBookInfo(member, bookRecord));
        }

        // 응답 보내기
        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공", readingBooks),
                HttpStatus.OK);
    }

    // 다 읽은 책 조회
    public ResponseEntity<DefaultResponse> getFinishReadBooks(Long memberId) {
        // 해당 유저 가져오기
        Member member = memberRepository.findByMemberId(memberId);

        // 책 정보 가져오기
        List<BookRecord> bookRecordList = bookRecordRepository.findAllByMemberAndReadingStatus(member, FINISH_READ);

        // dto 정보 넣기
        List<GetFinishReadResponse> finishReadBooks = new ArrayList<>();
        for (BookRecord bookRecord : bookRecordList) {
            // 책 정보 가져오기
            Book book = bookRecord.getBook();

            // 리뷰의 유무 가져오기
            Boolean isWriteReview = bookReviewRepository.findByMemberAndBook(member, book) != null;

            // dto 정보 추가
            finishReadBooks.add(new GetFinishReadResponse(
                    book.getIsbn(),
                    book.getCover(),
                    book.getTitle(),
                    book.getAuthor(),
                    converterService.categoryNameToCode(book.getCategory()),
                    bookRecord.getBookType(),
                    bookRecord.isMine(),
                    isWriteReview
            ));
        }

        // 응답 보내기
        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공", finishReadBooks),
                HttpStatus.OK);
    }

    // 읽고 있는 책 숨기기 & 꺼내기
    public ResponseEntity<DefaultResponse> modifyHidden(Long memberId, String isbn, boolean isHidden) {
        // 해당 유저 가져오기
        Member member = memberRepository.findByMemberId(memberId);

        // 해당 독서기록 가져오기
        Book book = bookRepository.findByIsbn(isbn);
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // 숨기기 & 꺼내기 상태 변경 적용
        bookRecord.setIsHidden(isHidden);
        bookRecordRepository.save(bookRecord);

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }
}
