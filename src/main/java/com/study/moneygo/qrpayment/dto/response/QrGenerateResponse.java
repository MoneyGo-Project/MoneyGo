package com.study.moneygo.qrpayment.dto.response;

import com.study.moneygo.qrpayment.entity.QrPayment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class QrGenerateResponse {

    private Long qrPaymentId;
    private String qrCode;
    private BigDecimal amount;
    private String description;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public static QrGenerateResponse of(QrPayment qrPayment) {
        return QrGenerateResponse.builder()
                .qrPaymentId(qrPayment.getId())
                .qrCode(qrPayment.getQrCode())
                .amount(qrPayment.getAmount())
                .description(qrPayment.getDescription())
                .status(qrPayment.getStatus().name())
                .expiresAt(qrPayment.getExpiresAt())
                .createdAt(qrPayment.getCreatedAt())
                .build();
    }
}
