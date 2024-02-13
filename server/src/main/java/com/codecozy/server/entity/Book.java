package com.codecozy.server.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(length = 13)
    private String isbn;

    @Column(length = 1000, nullable = false)
    private String cover;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 100, nullable = false)
    private String author;

    @Column(length = 4, nullable = false)
    private String category;

    @Column(nullable = false)
    private int totalPage;
}
