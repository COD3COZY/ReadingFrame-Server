package com.codecozy.server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetBadgeResponse {
    private int badgeCode;
    private boolean isGotBadge;
    private String date;

    public void setIsGotBadge(boolean isGotBadge) {
        this.isGotBadge = isGotBadge;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
