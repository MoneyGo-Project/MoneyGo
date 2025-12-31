package com.study.moneygo.notification.controller;

import com.study.moneygo.notification.dto.request.NotificationSettingUpdateRequest;
import com.study.moneygo.notification.dto.response.NotificationSettingResponse;
import com.study.moneygo.notification.service.NotificationSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/notification-settings")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    /*
     내 알림 설정 조회
     */
    @GetMapping
    public ResponseEntity<NotificationSettingResponse> getMySetting() {
        log.info("알림 설정 조회 요청");
        NotificationSettingResponse response = notificationSettingService.getMySetting();
        return ResponseEntity.ok(response);
    }

    /*
     알림 설정 업데이트
     */
    @PatchMapping
    public ResponseEntity<NotificationSettingResponse> updateMySetting(
            @Valid @RequestBody NotificationSettingUpdateRequest request) {
        log.info("알림 설정 업데이트 요청");
        NotificationSettingResponse response = notificationSettingService.updateMySetting(request);
        return ResponseEntity.ok(response);
    }
}
