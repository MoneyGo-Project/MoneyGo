package com.study.moneygo.notification.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingUpdateRequest {

    private Boolean emailEnabled;
    private Boolean transferReceivedEmail;
    private Boolean transferSentEmail;
    private Boolean scheduledTransferEmail;
    private Boolean qrPaymentEmail;
    private Boolean largeAmountAlertEnabled;

    @DecimalMin(value = "0.01", message = "금액은 0보다 커야 합니다.")
    private BigDecimal largeAmountThreshold;
}
