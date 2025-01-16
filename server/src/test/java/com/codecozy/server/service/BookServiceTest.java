package com.codecozy.server.service;

import com.codecozy.server.dto.request.BookmarkRequest;
import com.codecozy.server.dto.request.MemoRequest;
import com.codecozy.server.dto.response.DefaultResponse;
import com.codecozy.server.entity.*;
import com.codecozy.server.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    private BookmarkRepository bookmarkRepository;

    @Mock
    private BookRecordRepository bookRecordRepository;

    @Mock
    private BookRecordDateRepository bookRecordDateRepository;

    @Mock
    private MemoRepository memoRepository;

    @Mock
    private ConverterService converterService;

    @InjectMocks
    private BookService bookService;

    private Member member;
    private Book book;
    private BookRecord bookRecord;

    /** mock 설정 **/
    private void setupBookmark(String uuid, int page, LocalDate date) {
        Bookmark bookmark = Bookmark.create(bookRecord, uuid, page, null, date);
        when(bookmarkRepository.findByBookRecordAndUuid(bookRecord, uuid)).thenReturn(bookmark);
    }

    private void setupMemo(String uuid, int page, LocalDate date, String text) {
        Memo memo = Memo.create(bookRecord, uuid, page, date, text);
        when(memoRepository.findByBookRecordAndUuid(bookRecord, uuid)).thenReturn(memo);
    }

    private void setupConverterService() {
        when(converterService.stringToDate(anyString())).thenAnswer(data -> {
            // 전달 받은 인자 가져오기
            String dateStr = data.getArgument(0);
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        });
        // 이 메소드는 사용하지 않아 일단 주석처리함
//        when(converterService.dateToString(any()))
//                .thenAnswer(data -> {
//                    LocalDate date = data.getArgument(0);
//                    return date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
//                });
    }

    /** 검증 메소드 **/
    // 북마크 검증
    private void verifyCapturedBookmark(ArgumentCaptor<Bookmark> captor, String uuid, int page, LocalDate date) {
        verify(bookmarkRepository).save(captor.capture());
        Bookmark capturedBookmark = captor.getValue();
        assertThat(capturedBookmark.getUuid()).isEqualTo(uuid);
        assertThat(capturedBookmark.getMarkPage()).isEqualTo(page);
        assertThat(capturedBookmark.getDate()).isEqualTo(date);
    }

    // 메모 검증
    private void verifyCapturedMemo(ArgumentCaptor<Memo> captor, String uuid, int page, String text, LocalDate date) {
        verify(memoRepository).save(captor.capture());
        Memo capturedMemo = captor.getValue();
        assertThat(capturedMemo.getUuid()).isEqualTo(uuid);
        assertThat(capturedMemo.getMarkPage()).isEqualTo(page);
        assertThat(capturedMemo.getMemoText()).isEqualTo(text);
        assertThat(capturedMemo.getBookRecord().getRecentDate()).isEqualTo(date);
    }

    @BeforeEach
    void setUp() {
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
        bookRecord.setRecentDate(LocalDate.of(2024, 1, 1));
        BookRecordDate bookRecordDate = BookRecordDate.create(bookRecord);

        // 공통 mock 설정
        when(memberRepository.findByMemberId(member.getMemberId())).thenReturn(member);
        when(bookRepository.findByIsbn(book.getIsbn())).thenReturn(book);
        when(bookRecordRepository.findByMemberAndBook(member, book)).thenReturn(bookRecord);
        when(bookRecordDateRepository.findByBookRecord(bookRecord)).thenReturn(bookRecordDate);
        setupConverterService();
    }

    @Test
    @DisplayName("북마크 수정 테스트")
    void modifyBookmark() {
        // given
        setupBookmark("3b7d", 50, LocalDate.of(2024, 12, 10));

        BookmarkRequest request = new BookmarkRequest("2024.12.17", 110, null, "3b7d");
        ArgumentCaptor<Bookmark> captor = ArgumentCaptor.forClass(Bookmark.class);

        // when
        ResponseEntity<DefaultResponse> response = bookService.modifyBookmark(member.getMemberId(), book.getIsbn(), request);

        // then
        assertThat(response.getBody().getMessage()).isEqualTo("성공");
        verifyCapturedBookmark(captor, "3b7d", 110, LocalDate.of(2024, 12, 17));
    }

    @Test
    @DisplayName("메모 등록 시 recentDate 업데이트 테스트")
    void addMemo() {
        // given
        when(memoRepository.findByBookRecordAndUuid(bookRecord, "6a1b")).thenReturn(null);

        MemoRequest request = new MemoRequest("6a1b", "2024.12.29", 50, "라이언 첫 등장");
        ArgumentCaptor<Memo> captor = ArgumentCaptor.forClass(Memo.class);

        // when
        ResponseEntity<DefaultResponse> response = bookService.addMemo(member.getMemberId(), book.getIsbn(), request);

        // then
        assertThat(response.getBody().getMessage()).isEqualTo("성공");
        verifyCapturedMemo(captor, "6a1b", 50, "라이언 첫 등장", LocalDate.of(2024, 12, 29));
    }

    @Test
    @DisplayName("메모 수정 시 recentDate 업데이트 테스트")
    void modifyMemo() {
        // given
        setupMemo("5c8i", 20, LocalDate.of(2024, 12, 31), "춘식이 첫 등장");

        MemoRequest request = new MemoRequest("5c8i", "2025.01.01", 60, "라이언 두 마리 등장");
        ArgumentCaptor<Memo> captor = ArgumentCaptor.forClass(Memo.class);

        // when
        ResponseEntity<DefaultResponse> response = bookService.modifyMemo(member.getMemberId(), book.getIsbn(), request);

        // then
        assertThat(response.getBody().getMessage()).isEqualTo("성공");
        verifyCapturedMemo(captor, "5c8i", 60, "라이언 두 마리 등장", LocalDate.of(2025, 1, 1));
    }
}
