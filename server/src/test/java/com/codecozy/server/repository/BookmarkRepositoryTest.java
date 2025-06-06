package com.codecozy.server.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.Bookmark;
import com.codecozy.server.entity.LocationInfo;
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
class BookmarkRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private BookRecordRepository bookRecordRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    private final String uuid = "b1526767-b6bc-427e-9f11-825ed2084cc8";

    private Member member;
    private Book book;
    private LocationInfo location;
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

        // 위치 세팅
        location = testEntityManager.persist(LocationInfo.create(
                "학교",
                "서울시",
                52,
                62));

        // 독서노트 생성
        bookRecord = testEntityManager.persist(BookRecord.create(member, book));

        // 책갈피 추가 (3개)
        testEntityManager.persist(Bookmark.create(
                bookRecord,
                uuid,
                10,
                location,
                LocalDate.of(2024, 12, 1)));
        testEntityManager.persist(Bookmark.create(
                bookRecord,
                "1a7599b0-a79b-4410-b2ce-d03a6a4c9bb9",
                25,
                location,
                LocalDate.of(2024, 12, 5)));
        testEntityManager.persist(Bookmark.create(
                bookRecord,
                "05e1dcbc-e630-41ec-898a-1b22474fbb78",
                50,
                location,
                LocalDate.of(2024, 12, 12)));

        // DB 동기화 및 영속성 컨텍스트 초기화
        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Test
    @DisplayName("독서노트(부모) 삭제 시 책갈피(자식)가 삭제된다")
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
        assertThat(bookmarkRepository.findAllByBookRecord(bookRecord)).isEmpty();
    }

    @Test
    @DisplayName("특정 독서노트와 uuid로 책갈피 찾기")
    void findByBookRecordAndUuid() {
        // when
        Bookmark bookmark = bookmarkRepository.findByBookRecordAndUuid(bookRecord, uuid);

        // then
        assertThat(bookmark.getMarkPage()).isEqualTo(10);
        assertThat(bookmark.getDate()).isEqualTo(LocalDate.of(2024, 12, 1));
    }

    @Test
    @DisplayName("특정 유저의 모든 책갈피 찾기")
    void findAllByBookRecordMember() {
        // when
        List<Bookmark> bookmarks = bookmarkRepository.findAllByBookRecordMember(member);

        // then
        assertThat(bookmarks).hasSize(3);
        assertThat(bookmarks).extracting(Bookmark::getMarkPage)
                             .containsExactlyInAnyOrder(10, 25, 50);
    }

    @Test
    @DisplayName("특정 독서노트의 모든 책갈피 찾기")
    void findAllByBookRecord() {
        // when
        List<Bookmark> bookmarks = bookmarkRepository.findAllByBookRecord(bookRecord);

        // then
        assertThat(bookmarks).hasSize(3);
        assertThat(bookmarks).extracting(Bookmark::getMarkPage)
                             .containsExactlyInAnyOrder(10, 25, 50);
    }

    @Test
    @DisplayName("특정 유저, 특정 위치에 있는 모든 책갈피 찾기")
    void findAllByBookRecordMemberAndLocationList() {
        // when
        List<Bookmark> bookmarks = bookmarkRepository.findAllByBookRecordMemberAndLocationInfo(
                member, location);

        // then
        assertThat(bookmarks).hasSize(3);
        assertThat(bookmarks).extracting(Bookmark::getMarkPage)
                             .containsExactlyInAnyOrder(10, 25, 50);
    }

    @Test
    @DisplayName("특정 독서노트의 최근 책갈피 3개 불러오기")
    void findTop3ByBookRecordOrderByDateDesc() {
        // given
        bookRecord = testEntityManager.find(BookRecord.class,
                testEntityManager.getId(bookRecord));
        // 책갈피 하나 더 추가
        testEntityManager.persist(Bookmark.create(
                bookRecord,
                "934db224-c79f-49c9-92ba-711154632176",
                100,
                location,
                LocalDate.of(2024, 12, 20)));

        // when
        List<Bookmark> bookmarks = bookmarkRepository.findTop3ByBookRecordOrderByDateDesc(
                bookRecord);

        // then
        assertThat(bookmarks).hasSize(3);
        assertThat(bookmarks).extracting(Bookmark::getMarkPage)
                             .containsExactlyInAnyOrder(25, 50, 100);
    }
}