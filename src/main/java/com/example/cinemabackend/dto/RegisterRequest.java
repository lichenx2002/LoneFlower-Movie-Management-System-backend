package com.example.cinemabackend.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String phone;
    private String password;
}