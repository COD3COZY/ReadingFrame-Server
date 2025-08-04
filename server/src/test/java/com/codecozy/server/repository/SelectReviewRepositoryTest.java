package com.codecozy.server.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.Member;
import com.codecozy.server.entity.SelectReview;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class SelectReviewRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private BookRecordRepository bookRecordRepository;

    @Autowired
    private SelectReviewRepository selectReviewRepository;

    private final String isbn = "9791190090018";

    private Member member;
    private Book book;
    private BookRecord bookRecord;

    @BeforeEach
    void setup() {
        // 유저, 책, 독서노트 세팅
        member = testEntityManager.persist(Member.create("이름", "01"));
        book = testEntityManager.persist(Book.create(
                isbn,
                "http://example.com/cover.jpg",
                "제목",
                "작가",
                "과학",
                300,
                "출판사",
                LocalDate.now()));
        bookRecord = testEntityManager.persist(BookRecord.create(member, book));

        // 선택 리뷰 생성
        testEntityManager.persist(SelectReview.create(bookRecord, "11"));

        // DB 반영 및 영속성 컨텍스트 초기화
        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Test
    @DisplayName("독서노트(부모)를 삭제하면 선택리뷰(자식)도 삭제된다")
    void deleteTest() {
        // given
        BookRecord foundBookRecord = testEntityManager.find(BookRecord.class,
                testEntityManager.getId(bookRecord));

        // when
        testEntityManager.remove(foundBookRecord);

        // then
        // 부모 삭제 확인
        assertThat(bookRecordRepository.findByMemberAndBook(member, book)).isNull();
        // 자식 삭제 확인
        assertThat(selectReviewRepository.findByBookRecord(bookRecord)).isNull();
    }

    @Test
    @DisplayName("독서노트로 선택리뷰 찾기")
    void findByBookRecord() {
        // when
        SelectReview find = selectReviewRepository.findByBookRecord(bookRecord);

        // then
        assertThat(find.getSelectReviewCode()).isEqualTo("11");
    }

    @Test
    @DisplayName("특정 책의 모든 선택리뷰 찾기")
    void findAllByBookRecordBook() {
        // given
        // member 1명 추가 생성
        Member member2 = testEntityManager.persist(Member.create("이름2", "02"));

        book = testEntityManager.find(Book.class, isbn);

        // member2의 선택리뷰 추가
        BookRecord bookRecord2 = testEntityManager.persist(BookRecord.create(member2, book));
        testEntityManager.persist(SelectReview.create(bookRecord2, "22"));

        // when
        List<SelectReview> findList = selectReviewRepository.findAllByBookRecordBook(book);

        // then
        assertThat(findList).hasSize(2)
                .extracting(SelectReview::getSelectReviewCode)
                .containsExactlyInAnyOrder("11", "22");
    }
}