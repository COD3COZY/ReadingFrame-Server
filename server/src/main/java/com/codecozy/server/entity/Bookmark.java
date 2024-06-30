package com.codecozy.server.entity;

import com.codecozy.server.composite_key.BookmarkKey;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(BookmarkKey.class)
@Table(name = "BOOKMARK")
public class Bookmark {
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

    @Column(name = "mark_page", nullable = false)
    private int markPage;

    @ManyToOne
    @JoinColumn(name = "location_id", referencedColumnName = "location_id")
    private LocationList locationList;

    @Column(nullable = false)
    private LocalDate date;

    public static Bookmark create(Member member, Book book, String uuid, int markPage, LocationList locationList, LocalDate date) {
        return Bookmark.builder()
                .member(member)
                .book(book)
                .uuid(uuid)
                .markPage(markPage)
                .locationList(locationList)
                .date(date)
                .build();
    }
}
