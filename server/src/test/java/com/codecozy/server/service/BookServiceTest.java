package com.codecozy.server.service;

import com.codecozy.server.dto.response.DefaultResponse;
import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.BookRecordDate;
import com.codecozy.server.entity.Member;
import com.codecozy.server.repository.*;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.codecozy.server.dto.request.ReviewCreateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

// Mock로 가짜 데이터 생성
@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
    @InjectMocks
    private BookService bookService;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookRecordRepository bookRecordRepository;
    @Mock
    private KeywordReviewRepository keywordReviewRepository;
    @Mock
    private BookReviewRepository bookReviewRepository;
    @Mock
    private BookRecordDateRepository bookRecordDateRepository;
    @Mock
    private ConverterService converterService;

    @Test
    public void testCreateReview() {
        // mock 데이터 생성
        Long memberId = 1L;
        String isbn = "9791190090018";
        ReviewCreateRequest request = new ReviewCreateRequest("2024.11.08", "햇님이 책", List.of(0, 20, 24), "햇님이의 책입니다!");

        Member mockMember = Member.create("다은", "01");
        Book mockBook = Book.create(isbn, "https://image.aladin.co.kr/product/19359/16/cover500/s152835852_1.jpg", "우리가 빛의 속도로 갈 수 없다면", "김초엽", "SF", 330, "허블", LocalDate.parse("2019.06.24"));
        BookRecord mockBookRecord = BookRecord.create(mockMember, mockBook);
        BookRecordDate mockBookRecordDate = BookRecordDate.create(mockBookRecord);

        // mock 동작 설정
        when(memberRepository.findByMemberId(memberId)).thenReturn(mockMember);
        when(bookRepository.findByIsbn(isbn)).thenReturn(mockBook);
        when(bookRecordRepository.findByMemberAndBook(mockMember, mockBook)).thenReturn(mockBookRecord);

        // 실제 메소드 호출
        ResponseEntity<DefaultResponse> response = bookService.createReview(memberId, isbn, request);

        // 결과 확인
        assertEquals(LocalDate.parse("2024.11.08"), mockBookRecord.getFirstReviewDate());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("성공", response.getBody().getMessage());

        // 레포지토리 호출 확인
        verify(bookRecordRepository, times(1)).save(mockBookRecord);
        verify(bookRecordDateRepository, times(1)).save(any(BookRecordDate.class));
    }

}
