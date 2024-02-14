package com.codecozy.server.composite_key;

import com.codecozy.server.entity.Book;
import com.codecozy.server.entity.Member;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class BookRecordKey implements Serializable {
    private Member member;
    private Book book;
}
