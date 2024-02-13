package com.codecozy.server.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "location_list")
public class LocationList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String locationId;

    @Column(length = 50, nullable = false)
    private String placeName;

    @Column(length = 200, nullable = false)
    private String address;

    @Column(nullable = false)
    private long latitude;

    @Column(nullable = false)
    private long longitude;
}
