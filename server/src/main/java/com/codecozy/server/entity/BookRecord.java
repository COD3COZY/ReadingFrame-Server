package com.codecozy.server.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.lang.reflect.Member;

@Entity
@Getter
@Table(name = "book_record")
public class BookRecord {
    @Id
    @ManyToOne
    @JoinColumn(name = "memberId", referencedColumnName = "memberId")
    private Member member;

    @Id
    @ManyToOne
    @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    private Book book;

    @Column(nullable = false)
    private int readingStatus;

    @Column(nullable = false)
    private int bookType;

    @ManyToOne
    @JoinColumn(name = "locationId", referencedColumnName = "locationId")
    private LocationList locationList;

    @Column(nullable = false)
    private boolean isMine;

    @Column(nullable = false)
    private boolean isHidden;

    @Column(length = 10, nullable = false)
    private String startDate;

    @Column(length = 10)
    private String recentDate;
}
