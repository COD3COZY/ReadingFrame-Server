package com.codecozy.server.entity;

import com.codecozy.server.composite_key.PersonalDictionaryKey;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(PersonalDictionaryKey.class)
@Table(name = "PERSONAL_DICTIONARY")
public class PersonalDictionary {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "member_id", referencedColumnName = "member_id"),
            @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    })
    private BookRecord bookRecord;

    @Id
    @Column(length = 20, nullable = false)
    private String name;

    @Column(nullable = false)
    private int emoji;

    @Column(length = 30)
    private String preview;

    @Column(length = 1000)
    private String description;

    public static PersonalDictionary create(BookRecord bookRecord, String name, int emoji, String preview, String description) {
        return PersonalDictionary.builder()
                .bookRecord(bookRecord)
                .name(name)
                .emoji(emoji)
                .preview(preview)
                .description(description)
                .build();
    }
}
