package com.codecozy.server.repository;

import com.codecozy.server.composite_key.MemberLocationKey;
import com.codecozy.server.entity.LocationInfo;
import com.codecozy.server.entity.Member;
import com.codecozy.server.entity.MemberLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberLocationRepository extends JpaRepository<MemberLocation, MemberLocationKey> {
    long countAllByMember(Member member);
    MemberLocation findByMemberAndLocationInfo(Member member, LocationInfo locationInfo);
    List<MemberLocation> findByMemberOrderByDateAsc(Member member);
    List<MemberLocation> findAllByMember(Member member);
    Long countByLocationInfo(LocationInfo locationInfo);
}
