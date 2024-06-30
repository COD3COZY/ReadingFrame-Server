package com.codecozy.server.entity;

import com.codecozy.server.composite_key.MemberLocationKey;
import jakarta.persistence.*;
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
@IdClass(MemberLocationKey.class)
@Table(name = "MEMBER_LOCATION")
public class MemberLocation {
    @Id
    @ManyToOne
    @JoinColumn(name = "member_id", referencedColumnName = "member_id")
    private Member member;

    @Id
    @ManyToOne
    @JoinColumn(name = "location_id", referencedColumnName = "location_id")
    private LocationList locationList;

    @Id
    @Column(nullable = false)
    private LocalDateTime date;

    public static MemberLocation create(Member member, LocationList locationList, LocalDateTime date) {
        return MemberLocation.builder()
                .member(member)
                .locationList(locationList)
                .date(date)
                .build();
    }
}
