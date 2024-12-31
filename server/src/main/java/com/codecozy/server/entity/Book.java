package com.codecozy.server.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(length = 100)
    private String publisher;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE)
    private List<BookRecord> bookRecords;

    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE)
    private List<PersonalDictionary> personalDictionaries;

    public Book(String isbn) {
        this.isbn = isbn;
    }

    public static Book create(String isbn, String cover, String title, String author, String category, int totalPage,
                              String publisher, LocalDate publicationDate) {
        return Book.builder()
                .isbn(isbn)
                .cover(cover)
                .title(title)
                .author(author)
                .category(category)
                .totalPage(totalPage)
                .publisher(publisher)
                .publicationDate(publicationDate)
                .build();
    }
}
