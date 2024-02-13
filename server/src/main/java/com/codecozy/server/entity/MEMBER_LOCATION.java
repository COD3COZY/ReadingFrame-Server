package com.codecozy.server.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.lang.reflect.Member;

@Entity
@Getter
@Table(name = "member_location")
public class MEMBER_LOCATION {
    @Id
    @ManyToOne
    @JoinColumn(name = "memberId", referencedColumnName = "memberId")
    private Member member;

    @Id
    @ManyToOne
    @JoinColumn(name = "locationId", referencedColumnName = "locationId")
    private LocationList locationList;

    @Id
    @Column(length = 40, nullable = false)
    private String uuid;

}
