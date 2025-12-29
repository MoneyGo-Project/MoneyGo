package com.study.moneygo.qrpayment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QrPayRequest {

    @NotBlank(message = "QR 코드는 필수입니다.")
    private String qrCode;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
