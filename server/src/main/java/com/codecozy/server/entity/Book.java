package com.codecozy.server.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "BOOK")
public class Book {
    @Id
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

    @Column(name = "total_page", nullable = false)
    private int totalPage;

    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE)
    private List<Bookmark> bookmarks;

    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE)
    @Column(name = "book_records")
    private List<BookRecord> bookRecords;

    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE)
    @Column(name = "book_reviews")
    private List<BookReview> bookReviews;

    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE)
    @Column(name = "keyword_reviews")
    private List<KeywordReview> keywordReviews;

    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE)
    private List<Memo> memos;

    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE)
    @Column(name = "personal_dictionaries")
    private List<PersonalDictionary> personalDictionaries;

    public Book() { }

    @Builder
    public Book(String isbn) { this.isbn = isbn; }

    public static Book create(String isbn) {
        return Book.builder()
                .isbn(isbn)
                .build();
    }
}
