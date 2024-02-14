package com.codecozy.server.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Entity
@Getter
@Table(name = "LOCATION_LIST")
public class LocationList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private long locationId;

    @Column(name = "place_name", length = 50, nullable = false)
    private String placeName;

    @Column(length = 200, nullable = false)
    private String address;

    @Column(nullable = false)
    private long latitude;

    @Column(nullable = false)
    private long longitude;

    @OneToMany(mappedBy = "locationList", cascade = CascadeType.REMOVE)
    @Column(name = "member_locations")
    private List<MemberLocation> memberLocations;

    @OneToMany(mappedBy = "locationList", cascade = CascadeType.REMOVE)
    private List<Bookmark> bookmarks;

    @OneToMany(mappedBy = "locationList", cascade = CascadeType.REMOVE)
    @Column(name = "book_records")
    private List<BookRecord> bookRecords;
}
