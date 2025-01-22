package com.codecozy.server.service;

import com.codecozy.server.context.ResponseMessages;
import com.codecozy.server.dto.request.BookmarkRequest;
import com.codecozy.server.dto.request.MemoRequest;
import com.codecozy.server.dto.request.ModifyReadingStatusRequest;
import com.codecozy.server.dto.response.DefaultResponse;
import com.codecozy.server.dto.response.GetReadingNoteResponse;
import com.codecozy.server.entity.*;
import com.codecozy.server.repository.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookRecordRepository bookRecordRepository;

    @Mock
    private BookRecordDateRepository bookRecordDateRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private MemoRepository memoRepository;

    @Mock
    private PersonalDictionaryRepository personalDictionaryRepository;

    @Mock
    private ConverterService converterService;

    @InjectMocks
    private BookService bookService;

    private Member member;
    private Book book;
    private BookRecord bookRecord;

    // json 문자열 파싱을 위함
    private ObjectMapper om = new ObjectMapper();

    /** mock 설정 - BeforeEach에서 사용 **/
    private void setupConverterService() {
        lenient().when(converterService.stringToDate(anyString()))
                .thenAnswer(data -> {
                    // 전달 받은 인자 가져오기
                    String dateStr = data.getArgument(0);
                    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy.MM.dd"));
                });
        lenient().when(converterService.dateToString(any()))
                .thenAnswer(data -> {
                    LocalDate date = data.getArgument(0);
                    return date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
                });
    }

    /** mock 설정 - 개별 사용 **/
    private void setupBookRecordDate() {
        BookRecordDate bookRecordDate = BookRecordDate.create(bookRecord);
        when(bookRecordDateRepository.findByBookRecord(bookRecord)).thenReturn(bookRecordDate);
    }

    private void setupBookmark(String uuid, int page, LocalDate date) {
        Bookmark bookmark = Bookmark.create(bookRecord, uuid, page, null, date);
        when(bookmarkRepository.findByBookRecordAndUuid(bookRecord, uuid)).thenReturn(bookmark);
    }

    private void setupMemo(String uuid, int page, LocalDate date, String text) {
        Memo memo = Memo.create(bookRecord, uuid, page, date, text);
        when(memoRepository.findByBookRecordAndUuid(bookRecord, uuid)).thenReturn(memo);
    }

    /** 검증 메소드 **/
    // 북마크 검증
    private void verifyCapturedBookmark(String uuid, int page, LocalDate date) {
        ArgumentCaptor<Bookmark> captor = ArgumentCaptor.forClass(Bookmark.class);
        verify(bookmarkRepository).save(captor.capture());
        Bookmark capturedBookmark = captor.getValue();

        assertThat(capturedBookmark.getUuid()).isEqualTo(uuid);
        assertThat(capturedBookmark.getMarkPage()).isEqualTo(page);
        assertThat(capturedBookmark.getDate()).isEqualTo(date);
    }

    // 메모 검증
    private void verifyCapturedMemo(String uuid, int page, String text, LocalDate date) {
        ArgumentCaptor<Memo> captor = ArgumentCaptor.forClass(Memo.class);
        verify(memoRepository).save(captor.capture());
        Memo capturedMemo = captor.getValue();

        assertThat(capturedMemo.getUuid()).isEqualTo(uuid);
        assertThat(capturedMemo.getMarkPage()).isEqualTo(page);
        assertThat(capturedMemo.getMemoText()).isEqualTo(text);
        assertThat(capturedMemo.getBookRecord().getRecentDate()).isEqualTo(date);
    }

    // save된 독서노트 가져오기
    private BookRecord getCapturedBookRecord() {
        ArgumentCaptor<BookRecord> captor = ArgumentCaptor.forClass(BookRecord.class);
        verify(bookRecordRepository).save(captor.capture());
        return captor.getValue();
    }

    /** 기타 유틸리티 메소드 **/
    private <T> T getJsonData(ResponseEntity<DefaultResponse> response, Class<T> valueType) throws JsonProcessingException {
        String jsonStr = om.writeValueAsString(response.getBody().getData());
        return om.readValue(jsonStr, valueType);
    }

    @BeforeEach
    void setup() {
        // 테스트 데이터 준비
        member = Member.create("다은", "01");
        book = Book.create(
                "9791190090018",
                "https://image.aladin.co.kr/product/19359/16/cover500/s152835852_1.jpg",
                "우리가 빛의 속도로 갈 수 없다면",
                "김초엽", "SF", 330, "허블",
                LocalDate.of(2019, 6, 24)
        );
        bookRecord = BookRecord.create(member, book);
        bookRecord.setStartDate(LocalDate.of(2023, 1, 1));

        // 공통 mock 설정
        lenient().when(memberRepository.findByMemberId(member.getMemberId())).thenReturn(member);
        lenient().when(bookRepository.findByIsbn(book.getIsbn())).thenReturn(book);
        lenient().when(bookRecordRepository.findByMemberAndBook(member, book)).thenReturn(bookRecord);
        setupConverterService();
    }

    @Nested
    @DisplayName("독서노트 조회 시 리뷰 최초 등록일 조회 테스트")
    class GetReadingNoteTests {
        @Test
        @DisplayName("succeed: 리뷰가 등록된 상태일 경우")
        void succeed1() throws JsonProcessingException {
            // given
            bookRecord.setFirstReviewDate(LocalDate.of(2025, 1, 16));

            // when
            ResponseEntity<DefaultResponse> response = bookService.getReadingNote(member.getMemberId(), book.getIsbn());

            // then
            assertThat(response.getBody().getMessage()).isEqualTo(ResponseMessages.SUCCESS.get());

            GetReadingNoteResponse dto = getJsonData(response, GetReadingNoteResponse.class);
            assertThat(dto.firstReviewDate()).isEqualTo("2025.01.16");
        }

        @Test
        @DisplayName("succeed: 리뷰를 아직 등록하지 않은 상태일 경우")
        void succeed2() throws JsonProcessingException {
            // when
            ResponseEntity<DefaultResponse> response = bookService.getReadingNote(member.getMemberId(), book.getIsbn());

            // then
            assertThat(response.getBody().getMessage()).isEqualTo(ResponseMessages.SUCCESS.get());

            GetReadingNoteResponse dto = getJsonData(response, GetReadingNoteResponse.class);
            assertThat(dto.firstReviewDate()).isNull();
        }
    }

    @Nested
    @DisplayName("독서상태 변경 테스트")
    class ModifyReadingStatus {
        // 독서상태 값 모음
//        static final int UNREGISTERED = -1;    // 미등록
//        static final int WANT_TO_READ = 0;     // 읽고 싶은
        static final int READING = 1;          // 읽는 중
        static final int FINISH_READ = 2;      // 다 읽음

        @Test
        @DisplayName("succeed: 읽는중 -> 다읽음 전환하기")
        void succeed1() {
            // given
            bookRecord.setReadingStatus(READING);
            LocalDate nowDate = LocalDate.now();
            ModifyReadingStatusRequest request = new ModifyReadingStatusRequest(FINISH_READ, "TEST-UUID");
            // bookRecordDate Repository 스터빙
            setupBookRecordDate();

            // when
            ResponseEntity<DefaultResponse> response = bookService.modifyReadingStatus(member.getMemberId(),
                    book.getIsbn(), request);

            // then
            // http 응답 확인
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getMessage()).isEqualTo(ResponseMessages.SUCCESS.get());

            // 데이터 확인
            BookRecord found = getCapturedBookRecord();
            assertThat(found.getReadingStatus()).isEqualTo(FINISH_READ);
            assertThat(found.getRecentDate()).isEqualTo(nowDate);
            verifyCapturedBookmark("TEST-UUID", book.getTotalPage(), nowDate);
        }

        @Test
        @DisplayName("fail: uuid 없이 읽는중->다읽음 전환 시도")
        void fail() {
            // given
            bookRecord.setReadingStatus(READING);
            ModifyReadingStatusRequest request = new ModifyReadingStatusRequest(FINISH_READ, null);

            // when
            ResponseEntity<DefaultResponse> response = bookService.modifyReadingStatus(member.getMemberId(),
                    book.getIsbn(), request);

            // then
            // http 응답 확인
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().getMessage()).isEqualTo(ResponseMessages.MISSING_UUID.get());
            // save 메소드가 호출되지 않았음을 검증
            verify(bookRecordRepository, never()).save(any(BookRecord.class));
        }

        @Test
        @DisplayName("succeed: 다읽음 -> 읽는중 전환하기")
        void succeed2() {
            // given
            bookRecord.setReadingStatus(FINISH_READ);
            ModifyReadingStatusRequest request = new ModifyReadingStatusRequest(READING, null);

            // when
            ResponseEntity<DefaultResponse> response = bookService.modifyReadingStatus(member.getMemberId(),
                    book.getIsbn(), request);

            // then
            // http 응답 확인
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getMessage()).isEqualTo(ResponseMessages.SUCCESS.get());

            // 독서노트 데이터 확인
            BookRecord found = getCapturedBookRecord();
            assertThat(found.getReadingStatus()).isEqualTo(READING);
            assertThat(found.getMarkPage()).isEqualTo(0);
        }
    }

    @Test
    @DisplayName("북마크 수정 테스트")
    void modifyBookmark() {
        // given
        bookRecord.setRecentDate(LocalDate.of(2024, 1, 1)); // 기존 recentDate가 있는 상황
        setupBookmark("3b7d", 50, LocalDate.of(2024, 12, 10));
        setupBookRecordDate();

        BookmarkRequest request = new BookmarkRequest("2024.12.17", 110, null, "3b7d");


        // when
        ResponseEntity<DefaultResponse> response = bookService.modifyBookmark(member.getMemberId(), book.getIsbn(),
                request);

        // then
        assertThat(response.getBody().getMessage()).isEqualTo(ResponseMessages.SUCCESS.get());
        verifyCapturedBookmark("3b7d", 110, LocalDate.of(2024, 12, 17));
    }

    @Test
    @DisplayName("메모 등록 시 recentDate 업데이트 테스트")
    void addMemo() {
        // given
        bookRecord.setRecentDate(LocalDate.of(2024, 1, 1)); // 기존 recentDate가 있는 상황
        when(memoRepository.findByBookRecordAndUuid(bookRecord, "6a1b")).thenReturn(null);
        setupBookRecordDate();

        MemoRequest request = new MemoRequest("6a1b", "2024.12.29", 50, "라이언 첫 등장");

        // when
        ResponseEntity<DefaultResponse> response = bookService.addMemo(member.getMemberId(), book.getIsbn(), request);

        // then
        assertThat(response.getBody().getMessage()).isEqualTo(ResponseMessages.SUCCESS.get());
        verifyCapturedMemo("6a1b", 50, "라이언 첫 등장", LocalDate.of(2024, 12, 29));
    }

    @Test
    @DisplayName("메모 수정 시 recentDate 업데이트 테스트")
    void modifyMemo() {
        // given
        bookRecord.setRecentDate(LocalDate.of(2024, 1, 1)); // 기존 recentDate가 있는 상황
        setupMemo("5c8i", 20, LocalDate.of(2024, 12, 31), "춘식이 첫 등장");
        setupBookRecordDate();

        MemoRequest request = new MemoRequest("5c8i", "2025.01.01", 60, "라이언 두 마리 등장");

        // when
        ResponseEntity<DefaultResponse> response = bookService.modifyMemo(member.getMemberId(), book.getIsbn(),
                request);

        // then
        assertThat(response.getBody().getMessage()).isEqualTo(ResponseMessages.SUCCESS.get());
        verifyCapturedMemo("5c8i", 60, "라이언 두 마리 등장", LocalDate.of(2025, 1, 1));
    }
}
