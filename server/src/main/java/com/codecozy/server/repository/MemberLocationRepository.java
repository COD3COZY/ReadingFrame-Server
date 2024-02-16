package com.codecozy.server.repository;

import com.codecozy.server.entity.LocationList;
import com.codecozy.server.entity.Member;
import com.codecozy.server.entity.MemberLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberLocationRepository extends JpaRepository<MemberLocation, Long> {
    MemberLocation findByMemberAndLocationList(Member member, LocationList locationList);
    List<MemberLocation> findAllByOrderByDateAsc();
}