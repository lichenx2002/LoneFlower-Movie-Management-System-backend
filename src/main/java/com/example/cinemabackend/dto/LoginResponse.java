package com.example.cinemabackend.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private UserDTO user;
    private AdminDTO admin;
}