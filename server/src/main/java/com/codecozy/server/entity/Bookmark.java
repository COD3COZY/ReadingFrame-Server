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
    @JoinColumns({
            @JoinColumn(name = "member_id", referencedColumnName = "member_id"),
            @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    })
    private BookRecord bookRecord;

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

    public static Bookmark create(BookRecord bookRecord, String uuid, int markPage, LocationList locationList, LocalDate date) {
        return Bookmark.builder()
                .bookRecord(bookRecord)
                .uuid(uuid)
                .markPage(markPage)
                .locationList(locationList)
                .date(date)
                .build();
    }
}
