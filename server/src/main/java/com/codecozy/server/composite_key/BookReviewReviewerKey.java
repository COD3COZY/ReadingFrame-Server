package com.codecozy.server.composite_key;

import com.codecozy.server.entity.BookReview;
import com.codecozy.server.entity.Member;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class BookReviewReviewerKey implements Serializable {
    private BookReview bookReview;
    private Member member;
}
