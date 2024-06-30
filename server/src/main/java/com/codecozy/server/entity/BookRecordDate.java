package com.codecozy.server.entity;

import com.codecozy.server.composite_key.BookRecordDateKey;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(BookRecordDateKey.class)
// 독서노트의 마지막 기록 날짜를 저장하는 DB
@Table(name = "BOOK_RECORD_DATE")
public class BookRecordDate {
    @Id
    @OneToOne
    @JoinColumns({
            @JoinColumn(name = "member_id", referencedColumnName = "member_id"),
            @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    })
    private BookRecord bookRecord;

    @Column(name = "last_date")
    private LocalDateTime lastDate;

    public void setLastDate(LocalDateTime lastDate) { this.lastDate = lastDate; }

    public static BookRecordDate create(BookRecord bookRecord) {
        return BookRecordDate.builder()
                .bookRecord(bookRecord)
                .lastDate(LocalDateTime.now())
                .build();
    }
}
