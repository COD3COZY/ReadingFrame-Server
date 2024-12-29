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
    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "member_id", referencedColumnName = "member_id"),
            @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    })
    private BookRecord bookRecord;

    @Column(name = "select_review_code", length = 14, nullable = false)
    private String selectReviewCode;

    public void setSelectReviewCode(String selectReviewCode) { this.selectReviewCode = selectReviewCode; }

    public static SelectReview create(BookRecord bookRecord, String selectReviewCode) {
        return SelectReview.builder()
                           .bookRecord(bookRecord)
                           .selectReviewCode(selectReviewCode)
                           .build();
    }
}
