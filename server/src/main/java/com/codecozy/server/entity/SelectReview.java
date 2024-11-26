package com.codecozy.server.entity;

import com.codecozy.server.composite_key.SelectReviewKey;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(SelectReviewKey.class)
@Table(name = "SELECT_REVIEW")
public class SelectReview {
    @Id
    @ManyToOne
    @JoinColumn(name = "member_id", referencedColumnName = "member_id")
    private Member member;

    @Id
    @ManyToOne
    @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    private Book book;

    @Column(name = "select_review_code", length = 14, nullable = false)
    private String selectReviewCode;

    public void setSelectReviewCode(String selectReviewCode) { this.selectReviewCode = selectReviewCode; }

    public static SelectReview create(Member member, Book book, String selectReviewCode) {
        return SelectReview.builder()
                           .member(member)
                           .book(book)
                           .selectReviewCode(selectReviewCode)
                           .build();
    }
}
