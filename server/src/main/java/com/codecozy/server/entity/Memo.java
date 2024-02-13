package com.codecozy.server.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.lang.reflect.Member;

@Entity
@Getter
@Table(name = "memo")
public class Memo {
    @Id
    @ManyToOne
    @JoinColumn(name = "memberId", referencedColumnName = "memberId")
    private Member member;

    @Id
    @ManyToOne
    @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    private Book book;

    @Id
    @Column(length = 40, nullable = false)
    private String uuid;

    private int markPage;

    @Column(length = 10, nullable = false)
    private String date;

    @Column(length = 1000, nullable = false)
    private String memoText;
}
