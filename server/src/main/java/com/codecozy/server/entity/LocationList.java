package com.codecozy.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @OneToMany(mappedBy = "locationList", cascade = CascadeType.REMOVE)
    @Column(name = "member_locations")
    private List<MemberLocation> memberLocations;

    @OneToMany(mappedBy = "locationList", cascade = CascadeType.REMOVE)
    private List<Bookmark> bookmarks;

    @OneToMany(mappedBy = "locationList", cascade = CascadeType.REMOVE)
    @Column(name = "book_records")
    private List<BookRecord> bookRecords;

    public void setPlaceName(String placeName) { this.placeName = placeName; }
    public void setAddress(String address) { this.address = address; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public static LocationList create(String placeName, String address, double latitude, double longitude) {
        return LocationList.builder()
                .placeName(placeName)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
