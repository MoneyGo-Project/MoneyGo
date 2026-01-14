package com.study.moneygo.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDeleteRequest {

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    private String reason; // 탈퇴 사유는 선택으로
}
