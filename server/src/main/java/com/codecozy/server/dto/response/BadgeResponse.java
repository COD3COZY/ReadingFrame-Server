package com.codecozy.server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BadgeResponse {
    private int badgeCode;
    private Boolean isGotBadge;
    private String date;

    public void setIsGotBadge(Boolean isGotBadge) {
        this.isGotBadge = isGotBadge;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
