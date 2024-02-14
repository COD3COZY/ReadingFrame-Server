package com.codecozy.server.entity;

import com.codecozy.server.composite_key.BookmarkKey;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
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
    @Column(name = "location_list")
    private LocationList locationList;

    @Column(length = 10, nullable = false)
    private String date;
}
