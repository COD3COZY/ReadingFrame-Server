package com.codecozy.server.entity;

import com.codecozy.server.composite_key.BookRecordKey;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(BookRecordKey.class)
@Table(name = "BOOK_RECORD")
public class BookRecord {
    @Id
    @ManyToOne
    @JoinColumn(name = "member_id", referencedColumnName = "member_id")
    private Member member;

    @Id
    @ManyToOne
    @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    private Book book;

    @Column(name = "reading_status", nullable = false)
    private int readingStatus;

    @Column(nullable = false)
    private int bookType;

    @ManyToOne
    @JoinColumn(name = "location_id", referencedColumnName = "location_id")
    private LocationList locationList;

    @Column(name = "is_mine", nullable = false)
    private boolean isMine;

    @Column(name = "is_hidden", nullable = false)
    private boolean isHidden;

    @Column(name = "start_date", length = 10, nullable = false)
    private String startDate;

    @Column(name = "recent_date", length = 10)
    private String recentDate;

    @ColumnDefault("0")
    @Column(name = "mark_page", nullable = false)
    private int markPage;

    @Column(name = "key_word", length = 15)
    private String keyWord;

    public void setReadingStatus(int readingStatus) { this.readingStatus = readingStatus; }

    public void setIsMine(boolean isMine) { this.isMine = isMine; }

    public void setKeyWord(String keyWord) { this.keyWord = keyWord; }

    public void setLocationList(LocationList locationList) { this.locationList = locationList; }

    public void setStartDate(String startDate) { this.startDate = startDate; }

    public void setRecentDate(String recentDate) { this.recentDate = recentDate; }

    public void deleteLocationList() { this.locationList = null; }

    public static BookRecord create(Member member, Book book) {
        return BookRecord.builder()
                .member(member)
                .book(book)
                .readingStatus(0)
                .bookType(-1)
                .locationList(null)
                .isMine(false)
                .isHidden(false)
                .startDate("1970/01/01")
                .recentDate(null)
                .keyWord(null)
                .build();
    }

    public static BookRecord create(Member member, Book book, int readingStatus, int bookType, LocationList locationList, boolean isMine, String startDate, String recentDate) {
        return BookRecord.builder()
                .member(member)
                .book(book)
                .readingStatus(readingStatus)
                .bookType(bookType)
                .locationList(locationList)
                .isMine(isMine)
                .isHidden(false)
                .startDate(startDate)
                .recentDate(recentDate)
                .keyWord(null)
                .build();
    }
}
