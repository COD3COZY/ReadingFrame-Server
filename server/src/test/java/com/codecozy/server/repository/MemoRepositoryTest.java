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

@DataJpaTest
class MemoRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookRecordRepository bookRecordRepository;

    @Autowired
    private MemoRepository memoRepository;

    private final String isbn = "9791190090018";
    private final String uuid = "b1526767-b6bc-427e-9f11-825ed2084cc8";

    private Long memberId;

    @BeforeEach
    void setup() {
        // 유저, 책 세팅
        Member member = memberRepository.save(Member.create("이름", "01"));
        memberId = member.getMemberId();
        Book book = bookRepository.save(Book.create(
                isbn,
                "http://example.com/cover.jpg",
                "제목",
                "작가",
                "과학",
                300,
                "출판사",
                LocalDate.now()));

        // 독서노트 생성
        BookRecord bookRecord = bookRecordRepository.save(BookRecord.create(member, book));

        // 메모 추가
        memoRepository.save(Memo.create(
                bookRecord,
                uuid,
                10,
                LocalDate.of(2024, 12, 1),
                "인상깊음"));

        // DB 동기화 및 영속성 컨텍스트 초기화
        testEntityManager.flush();
        testEntityManager.clear();
    }

    // 유저, 책 정보로 독서노트 객체를 가져오는 함수
    BookRecord getBookRecord() {
        Member member = memberRepository.findByMemberId(memberId);
        Book book = bookRepository.findByIsbn(isbn);

        return bookRecordRepository.findByMemberAndBook(member, book);
    }

    @Test
    @DisplayName("독서노트(부모) 삭제 시 메모(자식)가 삭제된다")
    void deleteTest() {
        // given
        Member member = memberRepository.findByMemberId(memberId);
        Book book = bookRepository.findByIsbn(isbn);
        BookRecord bookRecord = bookRecordRepository.findByMemberAndBook(member, book);

        // when
        bookRecordRepository.delete(bookRecord);

        // then
        // 부모 삭제 확인
        assertThat(bookRecordRepository.findByMemberAndBook(member, book)).isNull();
        // 자식 삭제 확인
        assertThat(memoRepository.findByBookRecordAndUuid(bookRecord, uuid)).isNull();
    }

    @Test
    @DisplayName("독서노트 객체와 uuid 값으로 메모 한 개를 찾는다")
    void findByBookRecordAndUuid() {
        // given
        BookRecord bookRecord = getBookRecord();

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
        BookRecord bookRecord = getBookRecord();

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
        BookRecord bookRecord = getBookRecord();

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
}