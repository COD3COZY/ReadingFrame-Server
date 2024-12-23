package com.codecozy.server.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SelectReviewTest {

    @Test
    @DisplayName("SelectReview 객체 생성 테스트")
    void create() {
        // given
        Member member = Member.create("닉네임", "01");
        Book book = Book.create(
                "isbn",
                "표지",
                "제목",
                "작가",
                "장르",
                300,
                "출판사",
                LocalDate.now());

        // when
        SelectReview selectReview = SelectReview.create(BookRecord.create(member, book),
                "1,2,3");

        // then
        // 멤버 정보, 책 정보, 선택 리뷰 코드 정보 검증
        assertThat(selectReview.getBookRecord().getMember()).isEqualTo(member);
        assertThat(selectReview.getBookRecord().getBook()).isEqualTo(book);
        assertThat(selectReview.getSelectReviewCode()).isEqualTo("1,2,3");
    }
}