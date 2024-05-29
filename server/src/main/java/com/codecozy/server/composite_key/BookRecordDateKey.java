package com.codecozy.server.composite_key;

import com.codecozy.server.entity.BookRecord;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class BookRecordDateKey implements Serializable {
    private BookRecord bookRecord;
}
