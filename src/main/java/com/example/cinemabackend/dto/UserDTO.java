// src/main/java/com/example/cinemabackend/dto/UserDTO.java
package com.example.cinemabackend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Integer userId;
    private String username;
    private String email;
    private String phone;
    private BigDecimal balance;
    private LocalDateTime regTime;
    private String password;
}