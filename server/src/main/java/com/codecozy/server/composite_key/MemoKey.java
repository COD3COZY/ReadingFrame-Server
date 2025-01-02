package com.codecozy.server.composite_key;

import com.codecozy.server.entity.BookRecord;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class MemoKey implements Serializable {
    private BookRecord bookRecord;
    private String uuid;
}
