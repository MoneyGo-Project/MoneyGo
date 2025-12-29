package com.study.moneygo.qrpayment.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QrGenerateRequest {

    @NotNull(message = "금액은 필수입니다.")
    @DecimalMin(value = "0.01", message = "금액은 0보다 커야합니다.")
    @DecimalMax(value = "1000000.00", message = "QR 결제 최대 금액은 100만원입니다.")
    private BigDecimal amount;

    @Size(max = 200, message = "설명은 200자 이내로 입력해주세요.")
    private String description;
}
