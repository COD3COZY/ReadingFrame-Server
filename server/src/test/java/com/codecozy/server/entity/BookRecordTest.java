package com.codecozy.server.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BookRecordTest {
    @Test
    @DisplayName("독서노트의 첫 리뷰 날짜 등록 테스트")
    public void testFirstReviewDate() {
        // given
        LocalDate today = LocalDate.now();
        BookRecord bookRecord = new BookRecord();

        // when
        bookRecord.setFirstReviewDate(today);

        // then
        assertThat(bookRecord.getFirstReviewDate())
                .isBeforeOrEqualTo(today) // 오늘 날짜를 포함한 이전 날짜
                .isNotNull(); // null 값이 아닌지 확인
    }

    @Test
    @DisplayName("독서노트의 최근 수정 날짜(recentDate) 업데이트 테스트")
    public void testUpdateRecentDate() {
        // given
        LocalDate testDay = LocalDate.of(2024, 12, 17);
        BookRecord bookRecord = new BookRecord();

        // when
        bookRecord.setRecentDate(testDay);

        // then
        assertThat(bookRecord.getRecentDate())
                .isEqualTo(testDay) // 테스트 날짜와 같은지
                .isNotNull();
    }
}
