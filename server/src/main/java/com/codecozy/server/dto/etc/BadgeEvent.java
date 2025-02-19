package com.codecozy.server.dto.etc;

import com.codecozy.server.context.BadgeActionType;
import java.time.LocalDate;

// 뱃지 획득 조건 검사 이벤트
public record BadgeEvent(
        Long memberId,
        BadgeActionType[] actionType,
        LocalDate date
) {}
