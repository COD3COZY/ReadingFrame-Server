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
    private BookRecordRepository bookRecordRepository;

    @Autowired
    private BookReviewRepository bookReviewRepository;

    private Member member;
    private Book book;
    private BookRecord bookRecord;

    @BeforeEach
    void setup() {
        // 가입 회원 세팅 (2명)
        member = testEntityManager.persist(Member.create("이름1", "01"));
        Member member2 = testEntityManager.persist(Member.create("이름2", "11"));

        // 테스트 책 세팅
        book = testEntityManager.persist(
                Book.create(
                        "9791190090018",
                        "http://example.com/cover.jpg",
                        "제목",
                        "작가",
                        "과학",
                        300,
                        "출판사",
                        LocalDate.now()));

        // 독서노트 세팅
        bookRecord = testEntityManager.persist(BookRecord.create(member, book));
        BookRecord bookRecord2 = testEntityManager.persist(BookRecord.create(member2, book));

        // 한줄평 세팅
        testEntityManager.persist(BookReview.create(bookRecord, "재밌음"));
        testEntityManager.persist(BookReview.create(bookRecord2, "흥미로웠음"));

        // 데이터베이스 동기화 및 영속성 context 초기화
        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Test
    @DisplayName("독서노트 삭제시 한줄평 삭제 여부 확인")
    void deleteTest() {
        // given
        BookRecord foundBookRecord = testEntityManager.find(BookRecord.class,
                testEntityManager.getId(bookRecord));

        // when
        // 부모 객체 삭제
        testEntityManager.remove(foundBookRecord);

        // then
        // 부모 객체 삭제 확인
        assertThat(bookRecordRepository.findByMemberAndBook(member, book)).isNull();
        // 자식 객체도 삭제되었는지 검증
        assertThat(bookReviewRepository.findByBookRecord(bookRecord)).isNull();
    }

    @Test
    @DisplayName("독서노트 정보로 한줄평 불러오기")
    void findByBookRecord() {
        // when
        BookReview bookReview = bookReviewRepository.findByBookRecord(bookRecord);

        // then
        assertThat(bookReview).isNotNull();
        assertThat(bookReview.getReviewText()).isEqualTo("재밌음");
    }

    @Test
    @DisplayName("특정 책의 총 한줄평 개수 불러오기")
    void countByBookRecordBook() {
        // when
        int reviewCount = bookReviewRepository.countByBookRecordBook(book);

        // then
        assertThat(reviewCount).isEqualTo(2);
    }

    @Test
    @DisplayName("특정 책의 모든 한줄평 정보 불러오기")
    void findAllByBookRecordBook() {
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