package com.codecozy.server.service;

import com.codecozy.server.context.BookType;
import com.codecozy.server.context.ReadingStatus;
import com.codecozy.server.context.ResponseMessages;
import com.codecozy.server.context.StatusCode;
import com.codecozy.server.dto.response.DefaultResponse;
import com.codecozy.server.dto.response.AllBookshelfResponse;
import com.codecozy.server.dto.response.DetailBookshelfResponse;
import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.Member;
import com.codecozy.server.repository.MemberRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookshelfService {
    private final ConverterService converterService;
    private final MemberRepository memberRepository;

    // 책장 초기 조회
    public ResponseEntity<DefaultResponse> getAllBookshelf(Long memberId, int bookshelfType) {
        // 유저 정보 가져오기
        Member member = memberRepository.findByMemberId(memberId);

        // 해당 유저의 독서노트 모두 가져오기
        List<BookRecord> bookRecordList = member.getBookRecords();

        // 응답 dto
        List<AllBookshelfResponse> bookshelfResponseList = new ArrayList<>();

        // 책유형별 책장
        if (bookshelfType == 0) {
            log.info("책유형별 책장 불러오기 시작");

            int code0Count = 0, code1Count = 0, code2Count = 0; // 각 책유형에 따른 책 개수
            List<Integer> code0PageList = new ArrayList<>();    // 각 책유형별로 담긴 책들의 총 페이지 수
            List<Integer> code1PageList = new ArrayList<>();
            List<Integer> code2PageList = new ArrayList<>();

            for (BookRecord bookRecord : bookRecordList) {
                // 읽고싶은 책은 추가X
                if (bookRecord.getReadingStatus() == ReadingStatus.WANT_TO_READ) {
                    continue;
                }

                // 해당 책의 페이지 수 가져오기
                Book book = bookRecord.getBook();
                int totalPage = book.getTotalPage();

                // 종이책 (코드값 0)
                if (bookRecord.getBookType() == BookType.PAPER_BOOK) {
                    code0Count++;
                    code0PageList.add(totalPage);
                }
                // 전자책 (코드값 1)
                else if (bookRecord.getBookType() == BookType.E_BOOK) {
                    code1Count++;
                    code1PageList.add(totalPage);
                }
                // 오디오북 (코드값 2)
                else if (bookRecord.getBookType() == BookType.AUDIO_BOOK) {
                    code2Count++;
                    code2PageList.add(totalPage);
                }
            }

            // dto에 정보 담기
            bookshelfResponseList.add(new AllBookshelfResponse(0, code0Count, code0PageList));
            bookshelfResponseList.add(new AllBookshelfResponse(1, code1Count, code1PageList));
            bookshelfResponseList.add(new AllBookshelfResponse(2, code2Count, code2PageList));
        }
        // 독서상태별 책장
        else if (bookshelfType == 1) {
            log.info("독서상태별 책장 불러오기 시작");

            int code0Count = 0, code1Count = 0, code2Count = 0; // 각 독서상태별에 따른 책 개수
            List<Integer> code0PageList = new ArrayList<>();    // 각 독서상태별로 담긴 책들의 총 페이지 수
            List<Integer> code1PageList = new ArrayList<>();
            List<Integer> code2PageList = new ArrayList<>();

            for (BookRecord bookRecord : bookRecordList) {
                // 해당 책의 페이지 수 가져오기
                Book book = bookRecord.getBook();
                int totalPage = book.getTotalPage();

                // 읽고싶은 (코드값 0)
                if (bookRecord.getReadingStatus() == ReadingStatus.WANT_TO_READ) {
                    code0Count++;
                    code0PageList.add(totalPage);
                }
                // 읽는중 (코드값 1)
                else if (bookRecord.getReadingStatus() == ReadingStatus.READING) {
                    code1Count++;
                    code1PageList.add(totalPage);
                }
                // 다읽은 (코드값 2)
                else if (bookRecord.getReadingStatus() == ReadingStatus.FINISH_READ) {
                    code2Count++;
                    code2PageList.add(totalPage);
                }
            }

            // dto에 정보 담기
            bookshelfResponseList.add(new AllBookshelfResponse(0, code0Count, code0PageList));
            bookshelfResponseList.add(new AllBookshelfResponse(1, code1Count, code1PageList));
            bookshelfResponseList.add(new AllBookshelfResponse(2, code2Count, code2PageList));
        }
        // 장르별 책장
        else if (bookshelfType == 2) {
            log.info("장르별 책장 불러오기 시작");

            List<Integer> countList = new ArrayList<>();    // 각 장르별에 따른 책 개수
            for (int i = 0; i < 8; i++) {
                countList.add(0);
            }

            List<List<Integer>> pageList = new ArrayList<>(new ArrayList<>());  // 각 장르별로 담긴 책들의 총 페이지 수
            for (int i = 0; i < 8; i++) {
                pageList.add(new ArrayList<>());
            }

            for (BookRecord bookRecord : bookRecordList) {
                // 읽고싶은 책은 추가X
                if (bookRecord.getReadingStatus() == 0) {
                    continue;
                }

                // 해당 책의 페이지 수 가져오기
                Book book = bookRecord.getBook();
                int totalPage = book.getTotalPage();

                int index = converterService.categoryNameToCode(book.getCategory());
                countList.set(index, countList.get(index) + 1);
                pageList.get(index).add(totalPage);
            }

            // dto에 정보 담기
            for (int i = 0; i < 8; i++) {
                bookshelfResponseList.add(new AllBookshelfResponse(i, countList.get(i), pageList.get(i)));
            }
        }

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(), bookshelfResponseList),
                HttpStatus.OK);
    }

    // 책장 리스트용 조회
    public ResponseEntity<DefaultResponse> getDetailBookshelf(Long memberId, String bookshelfCode) {
        // 유저 정보 가져오기
        Member member = memberRepository.findByMemberId(memberId);

        // 해당 유저의 독서노트 모두 가져오기
        List<BookRecord> bookRecordList = member.getBookRecords();

        // 응답 dto
        List<DetailBookshelfResponse> detailBookshelfResponseList = new ArrayList<>();

        // 책유형
        if (bookshelfCode.charAt(0) == '0') {
            log.info("책장 리스트용 조회 - 책유형 관련 불러오기");

            int bookType = bookshelfCode.charAt(1) - '0';

            for (BookRecord bookRecord : bookRecordList) {
                if (bookRecord.getBookType() == bookType && bookRecord.getReadingStatus() != 0) {
                    // 읽은 퍼센트 계산
                    int readingPage = bookRecord.getMarkPage();
                    float readingPercent = converterService.pageToPercent(readingPage, bookRecord.getBook().getTotalPage());

                    // 데이터 추가
                    detailBookshelfResponseList.add(new DetailBookshelfResponse(
                            bookRecord.getBook().getIsbn(),
                            bookRecord.getBook().getCover(),
                            bookRecord.getBook().getTitle(),
                            bookRecord.getBook().getAuthor(),
                            null,
                            converterService.categoryNameToCode(bookRecord.getBook().getCategory()),
                            bookRecord.isMine(),
                            bookRecord.getBook().getTotalPage(),
                            readingPage,
                            readingPercent
                    ));
                }
            }
        }
        // 독서상태
        else if (bookshelfCode.charAt(0) == '1') {
            log.info("책장 리스트용 조회 - 독서상태 관련 불러오기");

            int readingStatus = bookshelfCode.charAt(1) - '0';

            for (BookRecord bookRecord : bookRecordList) {
                if (bookRecord.getReadingStatus() == readingStatus) {
                    // 읽은 퍼센트 계산
                    int readingPage = bookRecord.getMarkPage();
                    float readingPercent = converterService.pageToPercent(readingPage, bookRecord.getBook().getTotalPage());

                    // 데이터 추가
                    detailBookshelfResponseList.add(new DetailBookshelfResponse(
                            bookRecord.getBook().getIsbn(),
                            bookRecord.getBook().getCover(),
                            bookRecord.getBook().getTitle(),
                            bookRecord.getBook().getAuthor(),
                            bookRecord.getBookType(),
                            converterService.categoryNameToCode(bookRecord.getBook().getCategory()),
                            bookRecord.isMine(),
                            bookRecord.getBook().getTotalPage(),
                            readingPage,
                            readingPercent
                    ));
                }
            }
        }
        // 장르
        else if (bookshelfCode.charAt(0) == '2') {
            log.info("책장 리스트용 조회 - 장르 관련 불러오기");

            String categoryName = converterService.categoryCodeToName(bookshelfCode.charAt(1) - '0');

            for (BookRecord bookRecord : bookRecordList) {
                if (bookRecord.getBook().getCategory().equals(categoryName) && bookRecord.getReadingStatus() != 0) {
                    // 읽은 퍼센트 계산
                    int readingPage = bookRecord.getMarkPage();
                    float readingPercent = converterService.pageToPercent(readingPage, bookRecord.getBook().getTotalPage());

                    // 데이터 추가
                    detailBookshelfResponseList.add(new DetailBookshelfResponse(
                            bookRecord.getBook().getIsbn(),
                            bookRecord.getBook().getCover(),
                            bookRecord.getBook().getTitle(),
                            bookRecord.getBook().getAuthor(),
                            bookRecord.getBookType(),
                            converterService.categoryNameToCode(bookRecord.getBook().getCategory()),
                            bookRecord.isMine(),
                            bookRecord.getBook().getTotalPage(),
                            readingPage,
                            readingPercent
                    ));
                }
            }
        }

        return new ResponseEntity<>(DefaultResponse.from(StatusCode.OK, ResponseMessages.SUCCESS.get(), detailBookshelfResponseList),
                HttpStatus.OK);
    }
}
