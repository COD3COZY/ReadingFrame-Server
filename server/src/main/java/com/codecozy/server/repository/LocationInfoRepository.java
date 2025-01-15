package com.codecozy.server.repository;

import com.codecozy.server.entity.LocationInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationInfoRepository extends JpaRepository<LocationInfo, Long> {
    LocationInfo findByLocationId(long locationId);
    LocationInfo findByPlaceName(String placeName);
}
