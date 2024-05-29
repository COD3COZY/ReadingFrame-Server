package com.codecozy.server.entity;

import com.codecozy.server.composite_key.BookRecordKey;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "recent_date")
    private LocalDate recentDate;

    @ColumnDefault("0")
    @Column(name = "mark_page", nullable = false)
    private int markPage;

    @Column(name = "key_word", length = 15)
    private String keyWord;

    @OneToOne(mappedBy = "bookRecord")
    private BookRecordDate bookRecordDate;

    public void setReadingStatus(int readingStatus) { this.readingStatus = readingStatus; }

    public void setBookType(int bookType) { this.bookType = bookType; }

    public void setIsMine(boolean isMine) { this.isMine = isMine; }

    public void setIsHidden(boolean isHidden) { this.isHidden = isHidden; }

    public void setMarkPage(int markPage) { this.markPage = markPage; }

    public void setKeyWord(String keyWord) { this.keyWord = keyWord; }

    public void setLocationList(LocationList locationList) { this.locationList = locationList; }

    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public void setRecentDate(LocalDate recentDate) { this.recentDate = recentDate; }

    public void deleteLocationList() { this.locationList = null; }

    // 읽고 싶은 책 등록 시 사용하는 create 메소드
    public static BookRecord create(Member member, Book book) {
        return BookRecord.builder()
                .member(member)
                .book(book)
                .readingStatus(0)
                .bookType(-1)
                .locationList(null)
                .isMine(false)
                .isHidden(false)
                .createDate(LocalDateTime.now())
                .startDate(null)
                .recentDate(null)
                .keyWord(null)
                .build();
    }

    public static BookRecord create(Member member, Book book, int readingStatus, int bookType, LocationList locationList, boolean isMine, LocalDate startDate, LocalDate recentDate) {
        return BookRecord.builder()
                .member(member)
                .book(book)
                .readingStatus(readingStatus)
                .bookType(bookType)
                .locationList(locationList)
                .isMine(isMine)
                .isHidden(false)
                .createDate(LocalDateTime.now())
                .startDate(startDate)
                .recentDate(recentDate)
                .keyWord(null)
                .build();
    }
}
