package com.codecozy.server.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.Member;
import com.codecozy.server.entity.Memo;
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
class MemoRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private BookRecordRepository bookRecordRepository;

    @Autowired
    private MemoRepository memoRepository;

    private final String uuid = "b1526767-b6bc-427e-9f11-825ed2084cc8";

    private Member member;
    private Book book;
    private BookRecord bookRecord;

    @BeforeEach
    void setup() {
        // 유저, 책 세팅
        member = testEntityManager.persist(Member.create("이름", "01"));
        book = testEntityManager.persist(Book.create(
                "9791190090018",
                "http://example.com/cover.jpg",
                "제목",
                "작가",
                "과학",
                300,
                "출판사",
                LocalDate.now()));

        // 독서노트 생성
        bookRecord = testEntityManager.persist(BookRecord.create(member, book));

        // 메모 추가
        testEntityManager.persist(Memo.create(
                bookRecord,
                uuid,
                10,
                LocalDate.of(2024, 12, 1),
                "인상깊음"));

        // DB 동기화 및 영속성 컨텍스트 초기화
        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Test
    @DisplayName("독서노트(부모) 삭제 시 메모(자식)가 삭제된다")
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
        assertThat(memoRepository.findAllByBookRecord(bookRecord)).isEmpty();
    }

    @Test
    @DisplayName("독서노트 객체와 uuid 값으로 메모 한 개를 찾는다")
    void findByBookRecordAndUuid() {
        // when
        Memo find = memoRepository.findByBookRecordAndUuid(bookRecord, uuid);

        // then
        assertThat(find).isNotNull();
        assertThat(find.getMemoText()).isEqualTo("인상깊음");
        assertThat(find.getBookRecord().getMember().getNickname()).isEqualTo("이름");
    }

    @Test
    @DisplayName("특정 독서노트 내에 있는 모든 메모를 찾는다")
    void findAllByBookRecord() {
        // given
        bookRecord = testEntityManager.find(BookRecord.class,
                testEntityManager.getId(bookRecord));

        // 메모 하나 더 추가
        memoRepository.save(Memo.create(
                bookRecord,
                "c1526767-b6bc-427e-9f11-825ed2084cc8",
                50,
                LocalDate.of(2024, 12, 5),
                "이러쿵저러쿵"));

        // when
        List<Memo> findList = memoRepository.findAllByBookRecord(bookRecord);

        // then
        assertThat(findList).hasSize(2);
        assertThat(findList).extracting(Memo::getMemoText)
                            .containsExactlyInAnyOrder("인상깊음", "이러쿵저러쿵");
    }

    @Test
    @DisplayName("특정 독서노트 내에 작성한 최근 3개의 메모를 찾는다")
    void findTop3ByBookRecordOrderByDateDesc() {
        // given
        bookRecord = testEntityManager.find(BookRecord.class, testEntityManager.getId(bookRecord));

        // 메모 2개 추가
        memoRepository.save(Memo.create(
                bookRecord,
                "c1526767-b6bc-427e-9f11-825ed2084cc8",
                50,
                LocalDate.of(2024, 12, 5),
                "이러쿵저러쿵"));
        memoRepository.save(Memo.create(
                bookRecord,
                "d1526767-b6bc-427e-9f11-825ed2084cc8",
                100,
                LocalDate.of(2024, 12, 10),
                "샬라샬라"));

        // when
        List<Memo> findList = memoRepository.findTop3ByBookRecordOrderByDateDesc(bookRecord);

        // then
        assertThat(findList).hasSize(3);
        assertThat(findList).extracting(Memo::getMarkPage)
                            .containsExactly(100, 50, 10);
    }

    @Test
    @DisplayName("특정 사용자가 메모를 작성했다면 true를 반환한다")
    void existsByBookRecordMember() {
        // when
        boolean find = memoRepository.existsByBookRecordMember(member);

        // then
        assertThat(find).isEqualTo(true);
    }
}