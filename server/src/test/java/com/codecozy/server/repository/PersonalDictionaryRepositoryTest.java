package com.codecozy.server.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.BookRecord;
import com.codecozy.server.entity.Member;
import com.codecozy.server.entity.PersonalDictionary;
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
class PersonalDictionaryRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private BookRecordRepository bookRecordRepository;

    @Autowired
    private PersonalDictionaryRepository personalDictionaryRepository;

    private Member member;
    private Book book;
    private BookRecord bookRecord;

    @BeforeEach
    void setup() {
        // 유저, 책, 독서노트 세팅
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
        bookRecord = testEntityManager.persist(BookRecord.create(member, book));

        // 인물사전 1개 추가
        testEntityManager.persist(PersonalDictionary.create(
                bookRecord,
                "토마토",
                123456,
                "멋쟁이 토마토",
                "토마토는 방울토마토였을까 왕토마토였을까 미스테리한 점이 돋보인다"));

        // DB 반영 및 영속성 컨텍스트 초기화
        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Test
    @DisplayName("독서노트(부모)를 삭제하면 인물사전(자식)도 삭제된다")
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
        assertThat(personalDictionaryRepository.findAllByBookRecord(bookRecord)).isEmpty();
    }

    @Test
    @DisplayName("독서노트와 이름으로 인물사전을 찾는다")
    void findByBookRecordAndName() {
        // when
        PersonalDictionary found = personalDictionaryRepository.findByBookRecordAndName(bookRecord,
                "토마토");

        // then
        assertThat(found.getEmoji()).isEqualTo(123456);
        assertThat(found.getPreview()).isEqualTo("멋쟁이 토마토");
    }

    @Test
    @DisplayName("특정 독서노트 내에 있는 모든 인물사전을 찾는다")
    void findAllByBookRecord() {
        // given
        bookRecord = testEntityManager.find(BookRecord.class, testEntityManager.getId(bookRecord));
        // 인물사전 1개 더 추가
        testEntityManager.persist(PersonalDictionary.create(
                bookRecord,
                "당근",
                789123,
                "당근당근",
                "귀여운 면이 있는 친구다"));

        // when
        List<PersonalDictionary> foundList = personalDictionaryRepository.findAllByBookRecord(
                bookRecord);

        // then
        assertThat(foundList).hasSize(2);
        assertThat(foundList).extracting(PersonalDictionary::getName)
                             .containsExactlyInAnyOrder("토마토", "당근");
    }

    @Test
    @DisplayName("독서노트 내에 있는 모든 인물사전 중 이름순(오름차순)으로 3개 불러온다")
    void findTop3ByBookRecordOrderByNameAsc() {
        // given
        bookRecord = testEntityManager.find(BookRecord.class, testEntityManager.getId(bookRecord));
        // 인물사전 2개 더 추가
        testEntityManager.persist(PersonalDictionary.create(
                bookRecord,
                "당근",
                789123,
                "당근당근",
                "귀여운 면이 있는 친구다"));
        testEntityManager.persist(PersonalDictionary.create(
                bookRecord,
                "오이",
                456789,
                "오이오이..!!",
                "할 말은 하는 성격"));

        // when
        List<PersonalDictionary> foundList = personalDictionaryRepository.findTop3ByBookRecordOrderByNameAsc(
                bookRecord);

        // then
        assertThat(foundList).hasSize(3);
        assertThat(foundList).extracting(PersonalDictionary::getName)
                             .containsExactly("당근", "오이", "토마토");
    }
}