package com.codecozy.server.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.lang.reflect.Member;

@Entity
@Getter
@Table(name = "personal_dictionary")
public class PersonalDictionary {
    @Id
    @ManyToOne
    @JoinColumn(name = "memberId", referencedColumnName = "memberId")
    private Member member;

    @Id
    @ManyToOne
    @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    private Book book;

    @Id
    @Column(length = 20, nullable = false)
    private String name;

    @Column(nullable = false)
    private int emoji;

    @Column(length = 30)
    private String preview;

    @Column(length = 1000)
    private String description;
}
