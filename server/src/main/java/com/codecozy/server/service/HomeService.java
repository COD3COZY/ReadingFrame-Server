package com.codecozy.server.service;

import com.codecozy.server.context.ReadingStatus;
import com.codecozy.server.context.ResponseMessages;
import com.codecozy.server.context.StatusCode;
import com.codecozy.server.dto.response.DefaultResponse;
import com.codecozy.server.dto.response.FinishReadResponse;
import com.codecozy.server.dto.response.MainBooksResponse;
import com.codecozy.server.dto.response.GetMainResponse;
import com.codecozy.server.dto.response.ReadingResponse;
import com.codecozy.server.dto.response.SearchBookListResponse;
import com.codecozy.server.dto.response.WantToReadResponse;
import com.codecozy.server.dto.response.SearchBookDto;
import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.BookReview;
import com.codecozy.server.entity.SelectReview;
import com.codecozy.server.entity.Member;
import com.codecozy.server.repository.BookRecordRepository;
import com.codecozy.server.repository.BookRepository;
import com.codecozy.server.repository.MemberRepository;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeService {

    private final ConverterService converterService;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final BookRecordRepository bookRecordRepository;
    private final AladinService aladinService;

    // 메인 화면 조회
    public ResponseEntity<DefaultResponse> getMainPage(Long memberId) {
        // 해당 유저 가져오기
        Member member = memberRepository.findByMemberId(memberId);

        /** 전체 책 리스트 가져오기 **/
        List<MainBooksResponse> booksList = new ArrayList<>();

        // 읽고 있는 책 리스트 가져오기 (최대 10개)
        List<BookRecord> readingBooks = bookRecordRepository.getMainReadingBooks(member, ReadingStatus.READING);

        // dto 값 넣기
        for (BookRecord bookRecord : readingBooks) {
            Book book = bookRecord.getBook();

            int totalPage = book.getTotalPage();
            int readPage = bookRecord.getMarkPage();
            int readingPercent = converterService.pageToPercent(readPage, totalPage);

            // 리뷰 유무 판단
            Boolean isWriteReview = false;
            // 1. 한 단어 리뷰
            String keywordReview = bookRecord.getKeyword();
            // 2. 선택 키워드 리뷰
            SelectReview selectReview = bookRecord.getSelectReview();
            // 3. 한줄평 리뷰
            BookReview commentReview = bookRecord.getBookReview();
            // 리뷰들 중 하나라도 있으면 true
            if (!(keywordReview == null) || !(selectReview == null) || !(commentReview == null)) {
                isWriteReview = true;
            }

            booksList.add(new MainBooksResponse(
                    ReadingStatus.READING,
                    book.getIsbn(),
                    book.getCover(),
                    book.getTitle(),
                    book.getAuthor(),
                    readingPercent,
                    totalPage,
                    readPage,
                    bookRecord.isMine(),
                    isWriteReview,
                    bookRecord.getBookType()
            ));
        }

        // 읽고 싶은 책 리스트 가져오기 (최대 10개)
        List<BookRecord> wantToReadBooks = bookRecordRepository.findTop10ByMemberAndReadingStatusOrderByCreateDateDesc(
                member, ReadingStatus.WANT_TO_READ);

        // dto 값 넣기
        for (BookRecord bookRecord : wantToReadBooks) {
            Book book = bookRecord.getBook();

            booksList.add(new MainBooksResponse(
                    ReadingStatus.WANT_TO_READ,
                    book.getIsbn(),
                    book.getCover(),
                    book.getTitle(),
                    book.getAuthor(),
                    -1,
                    -1,
                    -1,
                    null,
                    null,
                    bookRecord.getBookType()
            ));
        }

        // 다 읽은 책 리스트 가져오기 (전체)
        List<BookRecord> finishReadBooks = bookRecordRepository.findAllByMemberAndReadingStatus(
                member, ReadingStatus.FINISH_READ);

        // dto 값 넣기
        for (BookRecord bookRecord : finishReadBooks) {
            Book book = bookRecord.getBook();

            // 리뷰 유무 판단
            Boolean isWriteReview = false;
            // 1. 한 단어 리뷰
            String keywordReview = bookRecord.getKeyword();
            // 2. 선택 키워드 리뷰
            SelectReview selectReview = bookRecord.getSelectReview();
            // 3. 한줄평 리뷰
            BookReview commentReview = bookRecord.getBookReview();
            // 리뷰들 중 하나라도 있으면 true
            if (!(keywordReview == null) || !(selectReview == null) || !(commentReview == null)) {
                isWriteReview = true;
            }

            booksList.add(new MainBooksResponse(
                    ReadingStatus.FINISH_READ,
                    book.getIsbn(),
                    book.getCover(),
                    book.getTitle(),
                    book.getAuthor(),
                    -1,
                    -1,
                    -1,
                    bookRecord.isMine(),
                    isWriteReview,
                    bookRecord.getBookType()
            ));
        }

        // 읽고 싶은 책 개수 계산
        List<BookRecord> tempWantToReadList = bookRecordRepository.findAllByMemberAndReadingStatus(
                member,
                ReadingStatus.WANT_TO_READ);
        int wantToReadCount = tempWantToReadList.size();

        // 읽고 있는 책 개수 계산
        List<BookRecord> tempReadingList = bookRecordRepository.findAllByMemberAndReadingStatus(
                member, ReadingStatus.READING);
        int readingCount = tempReadingList.size();

        // 응답 보내기
        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(),
                        new GetMainResponse(booksList, wantToReadCount,
                                readingCount)),
                HttpStatus.OK);
    }

    // 검색
    public ResponseEntity<DefaultResponse> getSearchList(Long memberId, String searchText) {
        // 응답 DTO
        SearchBookListResponse response = new SearchBookListResponse(0, new ArrayList<>());

        // 독서노트 내 검색된 책들의 isbn 값을 저장하는 리스트 (중복 제거 위함)
        List<String> isbnList = new ArrayList<>();

        // 1. 사용자가 독서노트에 등록한 책 넣기
        Member member = memberRepository.findByMemberId(memberId);
        List<BookRecord> bookRecordList = bookRecordRepository.findAllByMemberAndBookTitleContains(
                member, searchText);
        response.setTotalCount(bookRecordList.size());
        for (BookRecord bookRecord : bookRecordList) {
            Book book = bookRecord.getBook();
            String publicationDateStr = converterService.dateToString(book.getPublicationDate());
            response.getSearchList().add(new SearchBookDto(
                    book.getIsbn(),
                    book.getCover(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getPublisher(),
                    publicationDateStr
            ));

            // isbn값 넣기
            isbnList.add(book.getIsbn());
        }

        // 2. 알라딘 내 검색한 책의 정보 넣기
        // 검색문 공백 제거 (오류 방지를 위함)
        searchText = searchText.replaceAll(" ", "");

        // 알라딘 내 책 검색 (1~4 페이지의 정보 모두 담기)
        for (int i = 1; i < 5; i++) {
            // 알라딘 책 검색 수행
            SearchBookListResponse findList = aladinService.searchBookList(searchText, i);

            // 파싱 데이터에서 불러온 책 count 수 저장
            int totalCount = findList.getTotalCount();

            // 만일 파싱에서 뽑아온 책의 count 수가 200이 넘는다면
            // 실제 불러온 정보는 200권까지이므로.... 200으로 조정
            if (totalCount > 200) {
                totalCount = 200;
            }
            // 파싱 데이터 DTO 집어넣기 수행
            for (SearchBookDto searchInfo : findList.getSearchList()) {
                // 중복된 책이면 책 카운트 줄이고 정보 안 담기
                if (isbnList.contains(searchInfo.isbn())) {
                    totalCount--;
                    continue;
                }
                // 이미 독서노트 내에서 검색된 책이 아닐 경우에만 정보 담기
                response.getSearchList().add(searchInfo);
            }
            response.setTotalCount(response.getTotalCount() + totalCount);
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(), response),
                HttpStatus.OK);
    }

    // 읽고 싶은 책 조회
    public ResponseEntity<DefaultResponse> getWantToReadBooks(Long memberId) {
        // 해당 유저 가져오기
        Member member = memberRepository.findByMemberId(memberId);

        // 읽고 싶은 책 목록 가져오기
        List<BookRecord> bookRecordList = bookRecordRepository.findAllByMemberAndReadingStatus(
                member,
                ReadingStatus.WANT_TO_READ);

        // 해당 값 dto 정보 담기
        List<WantToReadResponse> wantToReadBooks = new ArrayList<>();
        for (BookRecord bookRecord : bookRecordList) {
            // 해당 책 정보 꺼내기
            Book book = bookRecord.getBook();

            wantToReadBooks.add(new WantToReadResponse(
                    book.getIsbn(),
                    book.getCover(),
                    book.getTitle(),
                    book.getAuthor(),
                    converterService.categoryNameToCode(book.getCategory())
            ));
        }

        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(), wantToReadBooks),
                HttpStatus.OK);
    }

    // 읽고 있는 책 조회
    public ResponseEntity<DefaultResponse> getReadingBooks(Long memberId) {
        // 해당 유저 가져오기
        Member member = memberRepository.findByMemberId(memberId);

        // 1. 읽고 있는 책 중, 숨기지 않은 책들만 먼저 가져오기
        List<BookRecord> notHiddenBooks = bookRecordRepository.findAllByMemberAndReadingStatusAndIsHidden(
                member,
                ReadingStatus.READING, false);

        // 2. 읽고 있는 책 중, 숨긴 책들도 가져오기
        List<BookRecord> hiddenBooks = bookRecordRepository.findAllByMemberAndReadingStatusAndIsHidden(
                member,
                ReadingStatus.READING, true);

        // dto 정보 넣기
        List<ReadingResponse> readingBooks = new ArrayList<>();

        // 숨기지 않은 책들 먼저 정보 넣기
        for (BookRecord bookRecord : notHiddenBooks) {
            readingBooks.add(getReadingBookInfo(bookRecord));
        }

        // 숨긴 책들 정보 넣기
        for (BookRecord bookRecord : hiddenBooks) {
            readingBooks.add(getReadingBookInfo(bookRecord));
        }

        // 응답 보내기
        return new ResponseEntity<>(
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(), readingBooks),
                HttpStatus.OK);
    }

    // 다 읽은 책 조회
    public ResponseEntity<DefaultResponse> getFinishReadBooks(Long memberId) {
        // 해당 유저 가져오기
        Member member = memberRepository.findByMemberId(memberId);

        // 책 정보 가져오기
        List<BookRecord> bookRecordList = bookRecordRepository.findAllByMemberAndReadingStatus(
                member, ReadingStatus.FINISH_READ);

        // dto 정보 넣기
        List<FinishReadResponse> finishReadBooks = new ArrayList<>();
        for (BookRecord bookRecord : bookRecordList) {
            // 책 정보 가져오기
            Book book = bookRecord.getBook();

            // 리뷰의 유무 가져오기
            Boolean isWriteReview = bookRecord.getBookReview() != null;

            // dto 정보 추가
            finishReadBooks.add(new FinishReadResponse(
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
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(), finishReadBooks),
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
                DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get()),
                HttpStatus.OK);
    }

    /** 헬퍼 메소드 **/

    // 읽고 있는 책 조회 시 필요한 정보들을 가져오는 메소드
    private ReadingResponse getReadingBookInfo(BookRecord bookRecord) {
        // 책 정보 가져오기
        Book book = bookRecord.getBook();

        // readingPercent 계산
        int totalPage = book.getTotalPage();
        int readPage = bookRecord.getMarkPage();
        int readingPercent = converterService.pageToPercent(readPage, totalPage);

        // 리뷰의 유무 가져오기
        Boolean isWriteReview = bookRecord.getBookReview() != null;

        return new ReadingResponse(
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
}
