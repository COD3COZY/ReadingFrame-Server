package com.codecozy.server.listener;

import com.codecozy.server.dto.etc.BadgeEvent;
import com.codecozy.server.service.BadgeService;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivityEventListener {
    private final BadgeService badgeService;

    @EventListener
    public void handleBadgeActivity(BadgeEvent event) {
        // 비동기로 뱃지 조건 검증 수행
        CompletableFuture.runAsync(() -> {
            badgeService.verifyBadgeActivity(event);
        }).exceptionally(ex -> {
            // Exception 발생 시 콘솔에 출력하도록 함
            ex.printStackTrace();
            return null;
        });
    }
}
