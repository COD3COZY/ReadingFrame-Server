package com.codecozy.server.entity;

import com.codecozy.server.composite_key.BadgeKey;
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
@IdClass(BadgeKey.class)
@Table(name = "BADGE")
public class Badge {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "member_id")
    private Member member;

    @Id
    @Column(name = "badge_code", nullable = false)
    private int badgeCode;

    @Column(nullable = false)
    private LocalDate date;

    public static Badge create(Member member, int badgeCode, LocalDate date) {
        return Badge.builder()
                    .member(member)
                    .badgeCode(badgeCode)
                    .date(date)
                    .build();
    }
}

