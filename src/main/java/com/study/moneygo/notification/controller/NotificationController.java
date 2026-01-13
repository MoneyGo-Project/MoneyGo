package com.study.moneygo.notification.controller;

import com.study.moneygo.notification.dto.response.NotificationResponse;
import com.study.moneygo.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /*
     내 알림 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("알림 목록 조회 요청");
        Page<NotificationResponse> notifications = notificationService.getMyNotifications(pageable);
        return ResponseEntity.ok(notifications);
    }

    /*
     읽지 않은 알림 목록 조회
     */
    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("읽지 않은 알림 목록 조회 요청");
        Page<NotificationResponse> notifications = notificationService.getUnreadNotifications(pageable);
        return ResponseEntity.ok(notifications);
    }

    /*
     읽지 않은 알림 개수 조회
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(Map.of("count", count));
    }

    /*
     알림 읽음 처리
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        log.info("알림 읽음 처리 요청: notificationId={}", notificationId);
        notificationService.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }

    /*
     모든 알림 읽음 처리
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        log.info("모든 알림 읽음 처리 요청");
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    /*
    알림 삭제
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        log.info("알림 삭제 요청: notificationId={}", notificationId);
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }

    /*
    읽은 알림 전체 삭제
     */
    @DeleteMapping("/read-all")
    public ResponseEntity<Void> deleteAllReadNotifications() {
        log.info("읽은 알림 전체 삭제 요청");
        notificationService.deleteAllReadNotifications();
        return ResponseEntity.noContent().build();
    }
}
