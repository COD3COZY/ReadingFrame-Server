package com.codecozy.server.composite_key;

import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.Member;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class BookRecordDateKey implements Serializable {
    private Member member;
    private Book book;
}
