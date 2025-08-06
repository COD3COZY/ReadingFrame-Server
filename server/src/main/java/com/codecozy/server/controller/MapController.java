package com.codecozy.server.controller;

import com.codecozy.server.dto.request.MarkDetailRequest;
import com.codecozy.server.token.TokenProvider;
import com.codecozy.server.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
public class MapController {

    private final TokenProvider tokenProvider;
    private final MapService mapService;

    // 전체 위치 조회 API
    @GetMapping("/location")
    public ResponseEntity getAllLocation(@RequestHeader("xAuthToken") String token,
            @RequestParam("orderNumber") Integer orderNumber) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return mapService.getAllLocation(memberId, orderNumber);
    }

    // 지도 마크 조회 API
    @GetMapping("/marker")
    public ResponseEntity getAllMarker(@RequestHeader("xAuthToken") String token) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);

        return mapService.getAllMarker(memberId);
    }

    // 마크 세부 조회 API
    @GetMapping("/marker/{id}/detail")
    public ResponseEntity getMarkDetail(@RequestHeader("xAuthToken") String token,
            @PathVariable("id") Long locationId,
            @RequestParam("orderNumber") Integer orderNumber) {
        Long memberId = tokenProvider.getMemberIdFromToken(token);
        MarkDetailRequest request = new MarkDetailRequest(locationId, orderNumber);

        return mapService.getMarkDetail(memberId, request);
    }
}
