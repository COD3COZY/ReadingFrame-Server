package com.codecozy.server.entity;

import com.codecozy.server.composite_key.PersonalDictionaryKey;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@IdClass(PersonalDictionaryKey.class)
@Table(name = "PERSONAL_DICTIONARY")
public class PersonalDictionary {
    @Id
    @ManyToOne
    @JoinColumn(name = "member_id", referencedColumnName = "member_id")
    private Member member;

    @Id
    @ManyToOne
    @JoinColumn(name = "isbn", referencedColumnName = "isbn")
    private Book book;

    @Id
    @Column(length = 20, nullable = false)
    private String name;

    @Column(nullable = false)
    private int emoji;

    @Column(length = 30)
    private String preview;

    @Column(length = 1000)
    private String description;
}
