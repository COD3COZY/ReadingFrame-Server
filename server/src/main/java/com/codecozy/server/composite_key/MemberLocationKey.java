package com.codecozy.server.composite_key;

import com.codecozy.server.entity.LocationInfo;
import com.codecozy.server.entity.Member;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class MemberLocationKey implements Serializable {
    private Member member;
    private LocationInfo locationInfo;
    private LocalDateTime date;
}
