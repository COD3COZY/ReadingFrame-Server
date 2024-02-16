package com.codecozy.server.repository;

import com.codecozy.server.entity.LocationList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<LocationList, Long> {
    LocationList findByLocationId(long locationId);
    LocationList findByLatitudeAndLongitude(long latitude, long longitude);
}
