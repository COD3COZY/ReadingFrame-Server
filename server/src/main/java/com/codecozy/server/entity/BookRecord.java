package com.codecozy.server.entity;

import com.codecozy.server.composite_key.BookRecordKey;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "member_id")
    private Member member;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    private Book book;

    @Column(name = "reading_status", nullable = false)
    private int readingStatus;

    @Column(nullable = false)
    private int bookType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", referencedColumnName = "location_id")
    private LocationInfo locationInfo;

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

    @Column(name = "first_review_date")
    private LocalDate firstReviewDate;

    @Column(name = "last_review_date")
    private LocalDateTime lastReviewDate;

    @ColumnDefault("0")
    @Column(name = "mark_page", nullable = false)
    private int markPage;

    @Column(name = "key_word", length = 15)
    private String keyWord;

    @OneToOne(mappedBy = "bookRecord", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private BookReview bookReview;

    @OneToOne(mappedBy = "bookRecord", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private SelectReview selectReview;

    @OneToMany(mappedBy = "bookRecord", cascade = CascadeType.REMOVE)
    private List<Bookmark> bookmarks;

    @OneToMany(mappedBy = "bookRecord", cascade = CascadeType.REMOVE)
    private List<Memo> memos;

    @OneToMany(mappedBy = "bookRecord", cascade = CascadeType.REMOVE)
    private List<PersonalDictionary> personalDictionaries;

    public void setReadingStatus(int readingStatus) { this.readingStatus = readingStatus; }

    public void setBookType(int bookType) { this.bookType = bookType; }

    public void setIsMine(boolean isMine) { this.isMine = isMine; }

    public void setIsHidden(boolean isHidden) { this.isHidden = isHidden; }

    public void setMarkPage(int markPage) { this.markPage = markPage; }

    public void setKeyWord(String keyWord) { this.keyWord = keyWord; }

    public void setLocationInfo(LocationInfo locationInfo) { this.locationInfo = locationInfo; }

    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public void setRecentDate(LocalDate recentDate) { this.recentDate = recentDate; }

    public void setFirstReviewDate(LocalDate firstReviewDate) { this.firstReviewDate = firstReviewDate; }

    public void setLastReviewDate(LocalDateTime lastReviewDate) { this.lastReviewDate = lastReviewDate; }

    public void deleteLocationInfo() { this.locationInfo = null; }

    // 읽고 싶은 책 등록 시 사용하는 create 메소드
    public static BookRecord create(Member member, Book book) {
        return BookRecord.builder()
                .member(member)
                .book(book)
                .readingStatus(0)
                .bookType(-1)
                .isMine(false)
                .isHidden(false)
                .createDate(LocalDateTime.now())
                .build();
    }

    public static BookRecord create(Member member, Book book, int readingStatus, int bookType, LocationInfo locationInfo, boolean isMine, LocalDate startDate, LocalDate recentDate) {
        return BookRecord.builder()
                .member(member)
                .book(book)
                .readingStatus(readingStatus)
                .bookType(bookType)
                .locationInfo(locationInfo)
                .isMine(isMine)
                .isHidden(false)
                .createDate(LocalDateTime.now())
                .startDate(startDate)
                .recentDate(recentDate)
                .build();
    }
}
