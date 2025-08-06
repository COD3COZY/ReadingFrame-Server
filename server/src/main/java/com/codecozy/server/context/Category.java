package com.codecozy.server.context;

import java.util.Map;
import java.util.HashMap;

public enum Category {
    LITERATURE("문학", 0),
    ESSAYS("에세이", 1),
    HUMAN_SOCIAL("인문사회", 2),
    SCIENCE("과학", 3),
    SELF_IMPROVEMENT("자기계발", 4),
    FOREIGN("원서", 5),
    ART("예술", 6),
    ETC("기타", 7);

    private final String name;
    private final int value;

    Category(String name, int value) {
        this.name = name;
        this.value = value;
    }

    private String getName() {
        return name;
    }

    private int getValue() {
        return value;
    }

    private static final Map<String, Integer> nameToValueMap = new HashMap<>();

    static {
        for (Category category : Category.values()) {
            // categoryName과 해당하는 int 코드값 매핑
            nameToValueMap.put(category.getName(), category.getValue());
        }
    }

    // categoryName별 코드값 반환
    public static int getValueByName(String name) {
        return nameToValueMap.get(name);
    }

    // 알라딘 API 내 카테고리 이름을 -> 리딩프레임 버전 카테고리 이름으로 변환하는 메소드
    public static String extractCategory(String categoryFullName) {
        if (categoryFullName.contains("소설")) {
            return LITERATURE.name;
        }

        if (categoryFullName.contains("인문")) {
            return HUMAN_SOCIAL.name;
        }

        for (String category : nameToValueMap.keySet()) {
            if (categoryFullName.contains(category)) {
                return category;
            }
        }

        return ETC.name;
    }
}
