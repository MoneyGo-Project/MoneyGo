package com.study.moneygo.account.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountLockRequest {

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
