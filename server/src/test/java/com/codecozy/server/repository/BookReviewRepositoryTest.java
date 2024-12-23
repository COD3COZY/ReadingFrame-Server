package com.codecozy.server.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.BookReview;
import com.codecozy.server.entity.Member;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class BookReviewRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookRecordRepository bookRecordRepository;

    @Autowired
    private BookReviewRepository bookReviewRepository;

    // 테스트를 위한 ID값 저장
    private String bookIsbn = "9791190090018";
    private Long memberId;
    private Long bookReviewId;

    @BeforeEach
    void setup() {
        // 가입 회원 세팅 (2명)
        Member member1 = memberRepository.save(Member.create("이름1", "01"));
        Member member2 = memberRepository.save(Member.create("이름2", "11"));

        memberId = member1.getMemberId();

        // 테스트 책 세팅
        Book book = bookRepository.save(
                Book.create(
                        bookIsbn,
                        "http://example.com/cover.jpg",
                        "제목",
                        "작가",
                        "과학",
                        300,
                        "출판사",
                        LocalDate.now())
        );

        // 독서노트 세팅
        BookRecord bookRecord1 = bookRecordRepository.save(BookRecord.create(member1, book));
        BookRecord bookRecord2 = bookRecordRepository.save(BookRecord.create(member2, book));

        // 한줄평 세팅
        BookReview bookReview = bookReviewRepository.save(BookReview.create(bookRecord1,
                "재밌음"));
        bookReviewRepository.save(BookReview.create(bookRecord2, "흥미로웠음"));

        bookReviewId = bookReview.getCommentId();

        // 데이터베이스 동기화 및 영속성 context 초기화
        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Test
    @DisplayName("독서노트 삭제시 한줄평 삭제 여부 확인")
    void deleteTest() {
        // given
        BookRecord bookRecord = bookReviewRepository.findById(bookReviewId).get().getBookRecord();

        // when
        // 부모 객체 삭제
        bookRecordRepository.delete(bookRecord);

        // then
        // 자식 객체도 삭제되었는지 검증
        assertThat(bookReviewRepository.findById(bookReviewId)).isEmpty();
    }

    @Test
    @DisplayName("독서노트 정보로 한줄평 불러오기")
    void findByBookRecord() {
        // given
        Member member = memberRepository.findByMemberId(memberId);
        Book book = bookRepository.findByIsbn(bookIsbn);
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // when
        BookReview bookReview = bookReviewRepository.findByBookRecord(bookRecord);

        // then
        assertThat(bookReview).isNotNull();
        assertThat(bookReview.getReviewText()).isEqualTo("재밌음");
        assertThat(bookReview.getBookRecord()).isEqualTo(bookRecord);
    }

    @Test
    @DisplayName("특정 책의 총 한줄평 개수 불러오기")
    void countByBookRecordBook() {
        // given
        Book book = bookRepository.findByIsbn(bookIsbn);

        // when
        int reviewCount = bookReviewRepository.countByBookRecordBook(book);

        // then
        assertThat(reviewCount).isEqualTo(2);
    }

    @Test
    @DisplayName("특정 책의 모든 한줄평 정보 불러오기")
    void findAllByBookRecordBook() {
        // given
        Book book = bookRepository.findByIsbn(bookIsbn);

        // when
        List<BookReview> bookReviews = bookReviewRepository.findAllByBookRecordBook(book);

        // then
        // 순서 상관없이 한줄평 데이터 체크
        assertThat(bookReviews).hasSize(2)
                               .extracting(BookReview::getReviewText)
                               .containsExactlyInAnyOrder("재밌음", "흥미로웠음");
    }

    @Test
    @DisplayName("특정 책의 모든 한줄평 정보를 최근 날짜 순으로 불러오기")
    void findAllByBookRecordBookOrderByReviewDateDesc() {
        // given
        Book book = bookRepository.findByIsbn(bookIsbn);

        // when
        List<BookReview> bookReviews = bookReviewRepository
                .findAllByBookRecordBookOrderByReviewDateDesc(book);

        // then
        assertThat(bookReviews).hasSize(2);
        // 2번째 한줄평 날짜와 같거나 그 이후 날짜인지 확인
        assertThat(bookReviews.get(0).getReviewDate())
                .isAfterOrEqualTo(bookReviews.get(1).getReviewDate());
    }
}