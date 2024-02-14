package com.codecozy.server.entity;

import com.codecozy.server.composite_key.MemoKey;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@IdClass(MemoKey.class)
@Table(name = "MEMO")
public class Memo {
    @Id
    @ManyToOne
    @JoinColumn(name = "member_id", referencedColumnName = "member_id")
    private Member member;

    @Id
    @ManyToOne
    @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    private Book book;

    @Id
    @Column(length = 40, nullable = false)
    private String uuid;

    @Column(name = "mark_page")
    private int markPage;

    @Column(length = 10, nullable = false)
    private String date;

    @Column(name = "memo_text", length = 1000, nullable = false)
    private String memoText;
}

