package com.codecozy.server.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Table(name = "MEMBER_APPLE")
public class MemberApple {
    @Id
    @Column(name = "member_id")
    private Long memberId;

    // 1:1 관계에서 member_id를 그대로 PK로 사용
    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "member_id")
    private Member member;

    @Column(length = 250, nullable = false)
    private String userIdentifier;

    @Column(length = 1000, nullable = false)
    private String idToken;

    public static MemberApple create(Member member, String userIdentifier, String idToken) {
        return MemberApple.builder()
                .member(member)
                .userIdentifier(userIdentifier)
                .idToken(idToken)
                .build();
    }
}
