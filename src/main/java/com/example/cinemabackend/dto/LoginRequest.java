package com.example.cinemabackend.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String loginId; // 可以是手机号、用户名或邮箱
    private String password;
}
