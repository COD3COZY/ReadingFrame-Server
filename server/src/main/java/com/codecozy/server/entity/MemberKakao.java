package com.codecozy.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "MEMBER_KAKAO")
public class MemberKakao {
    @Id
    @Column(name = "member_id")
    private Long memberId;

    @Column(length = 320)
    private String email;

    // FK로 들고 온 것을 그대로 PK로 설정하기 위함
    @MapsId
    @OneToOne
    @JoinColumn(name = "member_id", referencedColumnName = "member_id")
    private Member member;

    
}
