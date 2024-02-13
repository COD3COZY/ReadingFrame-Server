package com.codecozy.server.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.lang.reflect.Member;

@Entity
@Getter
@Table(name = "bookmark")
public class Bookmark {
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

    @Column(nullable = false)
    private int markPage;

    @ManyToOne
    @JoinColumn(name = "locationId", referencedColumnName = "locationId")
    private LocationList locationList;

    @Column(length = 10, nullable = false)
    private String date;
}
