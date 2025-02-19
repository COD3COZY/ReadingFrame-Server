package com.codecozy.server.annotation;

import com.codecozy.server.context.BadgeActionType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 뱃지 획득 조건을 추적하기 위해 사용하는 어노테이션
 * - action type 종류는 BadgeActionType 내 주석 참고
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackBadgeActivity {
    BadgeActionType[] actionType();
}
