package com.codecozy.server.repository;

import com.codecozy.server.dto.etc.NoteWithReview;
import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.BookReview;
import com.codecozy.server.entity.Member;
import com.codecozy.server.entity.SelectReview;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BookRecordRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private BookRecordRepository bookRecordRepository;

    private Member member;

    // 독서상태 값
    private static final int READING = 1;          // 읽는 중
    private static final int FINISH_READ = 2;      // 다 읽음

    @BeforeEach
    void setup() {
        // 가입 회원 세팅
        member = testEntityManager.persist(Member.create("이름1", "01"));

        // 테스트 책 세팅 (2권)
        Book book = testEntityManager.persist(
                Book.create(
                        "9791190090018",
                        "http://example.com/cover.jpg",
                        "제목",
                        "작가",
                        "과학",
                        300,
                        "출판사",
                        LocalDate.now()));
        Book book2 = testEntityManager.persist(
                Book.create(
                        "9791190090010",
                        "http://example.com/cover.jpg",
                        "제목2",
                        "작가2",
                        "인문",
                        200,
                        "출판사2",
                        LocalDate.now()));

        // 독서노트 세팅
        BookRecord bookRecord1 = BookRecord.create(member, book);
        bookRecord1.setReadingStatus(FINISH_READ);
        bookRecord1 = testEntityManager.persist(bookRecord1);

        BookRecord bookRecord2 = BookRecord.create(member, book2);
        bookRecord2.setReadingStatus(READING);
        bookRecord2 = testEntityManager.persist(bookRecord2);

        // 한줄평 세팅
        testEntityManager.persist(BookReview.create(bookRecord1, "재밌음"));
        // 선택 리뷰 생성
        testEntityManager.persist(SelectReview.create(bookRecord1, "11"));
        testEntityManager.persist(SelectReview.create(bookRecord2, "12"));

        // 데이터베이스 동기화 및 영속성 context 초기화
        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Test
    @DisplayName("리뷰가 하나라도 존재하는 독서노트 가져오기")
    void getBookRecordWithReviews() {
        // given
        List<Integer> readingStatusList = new ArrayList<>();
        readingStatusList.add(READING);
        readingStatusList.add(FINISH_READ);

        // when
        List<NoteWithReview> findList = bookRecordRepository.getBookRecordWithReviews(member, readingStatusList);

        // then
        assertThat(findList).hasSize(2);
        assertThat(findList.get(0).keyword()).isNull();
        assertThat(findList.get(0).bookReview().getReviewText()).isEqualTo("재밌음");
    }
}