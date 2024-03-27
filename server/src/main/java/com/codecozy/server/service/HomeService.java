package com.codecozy.server.service;

import com.codecozy.server.context.StatusCode;
import com.codecozy.server.dto.response.DefaultResponse;
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
            // 책 정보 가져오기
            Book book = bookRecord.getBook();

            // readingPercent 계산
            int totalPage = book.getTotalPage();
            int readPage = bookRecord.getMarkPage();
            Double readingPercent = (double) readPage / totalPage * 100;

            // 리뷰의 유무 가져오기
            Boolean isWriteReview = bookReviewRepository.findByMemberAndBook(member, book) != null;

            readingBooks.add(new GetReadingResponse(
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
                    isWriteReview
            ));
        }

        // 숨긴 책들 정보 넣기
        for (BookRecord bookRecord : hiddenBooks) {
            // 책 정보 가져오기
            Book book = bookRecord.getBook();

            // readingPercent 계산
            int totalPage = book.getTotalPage();
            int readPage = bookRecord.getMarkPage();
            Double readingPercent = (double) readPage / totalPage * 100;

            // 리뷰의 유무 가져오기
            Boolean isWriteReview = bookReviewRepository.findByMemberAndBook(member, book) != null;

            readingBooks.add(new GetReadingResponse(
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
                    isWriteReview
            ));
        }

        // 응답 보내기
        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, "성공", readingBooks),
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
