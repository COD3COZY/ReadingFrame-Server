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

@DataJpaTest
class SelectReviewRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookRecordRepository bookRecordRepository;

    @Autowired
    private SelectReviewRepository selectReviewRepository;

    private String bookIsbn = "9791190090018";
    private Long memberId;

    @BeforeEach
    void setup() {
        Member member = memberRepository.save(Member.create("이름", "01"));
        memberId = member.getMemberId();

        Book book = bookRepository.save(Book.create(
                bookIsbn,
                "http://example.com/cover.jpg",
                "제목",
                "작가",
                "과학",
                300,
                "출판사",
                LocalDate.now()));

        BookRecord bookRecord = bookRecordRepository.save(BookRecord.create(member, book));

        selectReviewRepository.save(SelectReview.create(bookRecord, "11"));

        // DB 반영 및 영속성 컨텍스트 초기화
        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Test
    @DisplayName("독서노트(부모)를 삭제하면 선택리뷰(자식)도 삭제된다")
    void deleteTest() {
        // given
        Member member = memberRepository.findByMemberId(memberId);
        Book book = bookRepository.findByIsbn(bookIsbn);
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // when
        bookRecordRepository.delete(bookRecord);

        // then
        assertThat(selectReviewRepository.findByBookRecord(bookRecord)).isNull();
    }

    @Test
    @DisplayName("독서노트로 선택리뷰 찾기")
    void findByBookRecord() {
        // given
        Member member = memberRepository.findByMemberId(memberId);
        Book book = bookRepository.findByIsbn(bookIsbn);
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

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
        Member member2 = memberRepository.save(Member.create("이름2", "02"));

        Book book = bookRepository.findByIsbn(bookIsbn);

        // member2의 선택리뷰 추가
        BookRecord bookRecord2 = bookRecordRepository.save(BookRecord.create(member2, book));
        selectReviewRepository.save(SelectReview.create(bookRecord2, "22"));

        // when
        List<SelectReview> findList = selectReviewRepository.findAllByBookRecordBook(book);

        // then
        assertThat(findList).hasSize(2)
                .extracting(SelectReview::getSelectReviewCode)
                .containsExactlyInAnyOrder("11", "22");
    }
}