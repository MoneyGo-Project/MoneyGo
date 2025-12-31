package com.study.moneygo.notification.dto.response;

import com.study.moneygo.notification.entity.NotificationSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class NotificationSettingResponse {

    private Long settingId;
    private boolean emailEnabled;
    private boolean transferReceivedEmail;
    private boolean transferSentEmail;
    private boolean scheduledTransferEmail;
    private boolean qrPaymentEmail;
    private boolean largeAmountAlertEnabled;
    private BigDecimal largeAmountThreshold;

    public static NotificationSettingResponse of(NotificationSetting setting) {
        return NotificationSettingResponse.builder()
                .settingId(setting.getId())
                .emailEnabled(setting.isEmailEnabled())
                .transferReceivedEmail(setting.isTransferReceivedEmail())
                .transferSentEmail(setting.isTransferSentEmail())
                .scheduledTransferEmail(setting.isScheduledTransferEmail())
                .qrPaymentEmail(setting.isQrPaymentEmail())
                .largeAmountAlertEnabled(setting.isLargeAmountAlertEnabled())
                .largeAmountThreshold(setting.getLargeAmountThreshold())
                .build();
    }
}
