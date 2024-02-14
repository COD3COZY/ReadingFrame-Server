package com.codecozy.server.entity;

import com.codecozy.server.composite_key.MemberLocationKey;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
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
    @Column(name = "location_list")
    private LocationList locationList;

    @Id
    @Column(length = 40, nullable = false)
    private String uuid;
}
