package com.codecozy.server.entity;

import com.codecozy.server.composite_key.MemoKey;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(MemoKey.class)
@Table(name = "MEMO")
public class Memo {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "member_id", referencedColumnName = "member_id"),
            @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    })
    private BookRecord bookRecord;

    @Id
    @Column(length = 40, nullable = false)
    private String uuid;

    @Column(name = "mark_page")
    private int markPage;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "memo_text", length = 1000, nullable = false)
    private String memoText;

    public static Memo create(BookRecord bookRecord, String uuid, int markPage, LocalDate date, String memoText) {
        return Memo.builder()
                .bookRecord(bookRecord)
                .uuid(uuid)
                .markPage(markPage)
                .date(date)
                .memoText(memoText)
                .build();
    }
}
