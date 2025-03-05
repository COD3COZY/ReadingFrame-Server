package com.codecozy.server.aspect;

import com.codecozy.server.annotation.TrackBadgeActivity;
import com.codecozy.server.dto.etc.BadgeEvent;
import com.codecozy.server.dto.etc.CustomUserDetails;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// 유저 활동을 추적하기 위한 aspect
@Aspect
@Component
@RequiredArgsConstructor
public class UserActivityAspect {
    private final ApplicationEventPublisher eventPublisher;

    // 뱃지 획득 검증 이벤트를 발행하기 위한 메소드
    @AfterReturning("@annotation(badgeActivity)")
    private void trackBadgeActivity(TrackBadgeActivity badgeActivity) {
        CustomUserDetails details = (CustomUserDetails) SecurityContextHolder.getContext()
                                                                             .getAuthentication()
                                                                             .getPrincipal();

        eventPublisher.publishEvent(new BadgeEvent(
                Long.parseLong(details.getUsername()),
                badgeActivity.actionType(),
                LocalDate.now()
        ));
    }
}
