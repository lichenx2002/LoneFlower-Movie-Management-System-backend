package com.example.cinemabackend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminDTO {
  private Integer adminId;
  private String username;
  private String name;
  private LocalDateTime lastLogin;
  private String password;
}