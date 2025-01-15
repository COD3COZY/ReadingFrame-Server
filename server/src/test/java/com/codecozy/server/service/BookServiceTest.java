package com.codecozy.server.service;

import com.codecozy.server.dto.request.BookmarkRequest;
import com.codecozy.server.dto.request.MemoRequest;
import com.codecozy.server.dto.response.DefaultResponse;
import com.codecozy.server.entity.*;
import com.codecozy.server.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
public class BookServiceTest {

    @Autowired
    private BookService bookService;
    @MockBean
    private MemberRepository memberRepository;
    @MockBean
    private BookRepository bookRepository;
    @MockBean
    private BookmarkRepository bookmarkRepository;
    @MockBean
    private BookRecordRepository bookRecordRepository;
    @MockBean
    private BookRecordDateRepository bookRecordDateRepository;
    @MockBean
    private MemoRepository memoRepository;

    private Member member;
    private Book book;

    @BeforeEach
    public void setUp() {
        // 테스트 데이터 준비
        member = Member.create("다은", "01");
        book = Book.create("9791190090018", "https://image.aladin.co.kr/product/19359/16/cover500/s152835852_1.jpg", "우리가 빛의 속도로 갈 수 없다면", "김초엽", "SF", 330, "허블", LocalDate.of(2019, 6, 24));
        BookRecord bookRecord = BookRecord.create(member, book);
        bookRecord.setRecentDate(LocalDate.of(2024, 1, 1));
        BookRecordDate bookRecordDate = BookRecordDate.create(bookRecord);

        memberRepository.save(member);
        bookRepository.save(book);
        bookRecordRepository.save(bookRecord);

        // mock 설정
        when(memberRepository.findByMemberId(member.getMemberId())).thenReturn(member);
        when(bookRepository.findByIsbn(book.getIsbn())).thenReturn(book);
        when(bookRecordRepository.findByMemberAndBook(member, book)).thenReturn(bookRecord);
        when(bookRecordDateRepository.findByBookRecord(bookRecord)).thenReturn(bookRecordDate);

        // 북마크 데이터
        Bookmark bookmark = Bookmark.create(bookRecord, "3b7d", 50, null, LocalDate.of(2024, 12, 10));
        when(bookmarkRepository.findByBookRecordAndUuid(bookRecord, "3b7d")).thenReturn(bookmark);

        // 메모 데이터
        Memo memo = Memo.create(bookRecord, "5c8i", 20, LocalDate.of(2024, 12, 31), "춘식이 첫 등장");
        when(memoRepository.findByBookRecordAndUuid(bookRecord, "5c8i")).thenReturn(memo);
    }

    @AfterEach
    public void afterEach() {
        // 리포지토리 데이터 삭제
        memberRepository.deleteAll();
        bookRepository.deleteAll();
        bookRecordRepository.deleteAll();
        bookRecordDateRepository.deleteAll();
        bookmarkRepository.deleteAll();

        // Mock 초기화
        reset(memberRepository, bookRepository, bookRecordRepository, bookRecordDateRepository, bookmarkRepository);
    }

    @Test
    @DisplayName("독서노트의 첫 리뷰 날짜 등록 테스트")
    public void testFirstReviewDate() {
        // given
        LocalDate today = LocalDate.now();
        BookRecord bookRecord = new BookRecord();

        // when
        bookRecord.setFirstReviewDate(today);

        // then
        assertThat(bookRecord.getFirstReviewDate())
                .isBeforeOrEqualTo(today) // 오늘 날짜를 포함한 이전 날짜
                .isNotNull(); // null 값이 아닌지 확인
    }

    @Test
    @DisplayName("독서노트의 최근 수정 날짜(recentDate) 업데이트 테스트")
    public void testUpdateRecentDate() {
        // given
        LocalDate testDay = LocalDate.of(2024, 12, 17);
        BookRecord bookRecord = new BookRecord();

        // when
        bookRecord.setRecentDate(testDay);

        // then
        assertThat(bookRecord.getRecentDate())
                .isEqualTo(testDay) // 테스트 날짜와 같은지
                .isNotNull();
    }

    @Test
    @DisplayName("markPage 업데이트 테스트")
    public void testUpdateMarkPage() {
        // given
        String uuid = "3b7d";
        int page = 110;
        LocalDate updateDate = LocalDate.of(2024, 12, 17);
        BookmarkRequest request = new BookmarkRequest("2024.12.17", page, null, uuid);

        // ArgumentCaptor를 사용하여 save() 메서드에 전달된 인자를 캡처
        ArgumentCaptor<Bookmark> captor = ArgumentCaptor.forClass(Bookmark.class);

        // when
        ResponseEntity<DefaultResponse> response = bookService.modifyBookmark(member.getMemberId(), book.getIsbn(), request);

        // then
        assertThat(response.getBody().getMessage()).isEqualTo("성공");

        // save 호출 시 전달된 bookmark 객체 캡처
        verify(bookmarkRepository).save(captor.capture());
        Bookmark capturedBookmark = captor.getValue();

        // 캡쳐한 값 검증
        assertThat(capturedBookmark.getUuid()).isEqualTo(uuid);
        assertThat(capturedBookmark.getMarkPage()).isEqualTo(page);
        assertThat(capturedBookmark.getDate()).isEqualTo(updateDate);
    }

    @Test
    @DisplayName("메모 등록 시 recentDate 업데이트 테스트")
    public void testUpdateRecentDateWhenAddMemo() {
        // given
        String uuid = "6a1b";
        int page = 50;
        LocalDate setDate = LocalDate.of(2024, 12, 29);
        MemoRequest request = new MemoRequest(uuid, "2024.12.29", page, "라이언 첫 등장");

        // save() 메서드 호출 시 전달된 인자 캡처용 captor
        ArgumentCaptor<Memo> captor = ArgumentCaptor.forClass(Memo.class);

        // when
        ResponseEntity<DefaultResponse> response = bookService.addMemo(member.getMemberId(), book.getIsbn(), request);

        // then
        // 성공 여부 확인과 캡쳐 값 검증(uuid, recentDate, markPage, memoText)
        assertThat(response.getBody().getMessage()).isEqualTo("성공");
        verify(memoRepository).save(captor.capture());
        Memo capturedMemo = captor.getValue();

        assertThat(capturedMemo.getUuid()).isEqualTo(uuid);
        assertThat(capturedMemo.getBookRecord().getRecentDate()).isEqualTo(setDate);
        assertThat(capturedMemo.getMarkPage()).isEqualTo(50);
        assertThat(capturedMemo.getMemoText()).isEqualTo("라이언 첫 등장");
    }

    @Test
    @DisplayName("메모 수정 시 recentDate 업데이트 테스트")
    public void testUpdateRecentDateWhenModifyMemo() {
        // given
        String uuid = "5c8i";
        int page = 60;
        LocalDate setDate = LocalDate.of(2025, 1, 1);
        MemoRequest request = new MemoRequest(uuid, "2025.01.01", page, "라이언 두 마리 등장");

        // save() 메서드 호출 시 전달된 인자 캡처용 captor
        ArgumentCaptor<Memo> captor = ArgumentCaptor.forClass(Memo.class);

        // when
        ResponseEntity<DefaultResponse> response = bookService.modifyMemo(member.getMemberId(), book.getIsbn(), request);

        // then
        // 성공 여부 확인과 캡쳐 값 검증(uuid, recentDate, markPage, memoText)
        assertThat(response.getBody().getMessage()).isEqualTo("성공");
        verify(memoRepository).save(captor.capture());
        Memo capturedMemo = captor.getValue();

        assertThat(capturedMemo.getUuid()).isEqualTo(uuid);
        assertThat(capturedMemo.getBookRecord().getRecentDate()).isEqualTo(setDate);
        assertThat(capturedMemo.getMarkPage()).isEqualTo(60);
        assertThat(capturedMemo.getMemoText()).isEqualTo("라이언 두 마리 등장");
    }
}
