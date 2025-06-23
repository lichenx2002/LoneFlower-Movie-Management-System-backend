package com.example.cinemabackend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailDTO {
  // 订单基本信息
  private Long orderId;
  private BigDecimal totalAmount;
  private LocalDateTime orderTime;
  private String status;
  private String paymentId;

  // 用户信息
  private Integer userId;
  private String username;
  private String userPhone;
  private String userEmail;

  // 电影信息
  private Integer movieId;
  private String movieTitle;
  private String movieEnglishTitle;
  private String moviePoster;
  private String movieGenres;
  private Integer movieDuration;

  // 影厅信息
  private Integer hallId;
  private String hallName;
  private String hallType;

  // 场次信息
  private Integer scheduleId;
  private LocalDateTime startTime;
  private BigDecimal basePrice;

  // 座位信息
  private List<OrderSeatDTO> seats;

  @Data
  public static class OrderSeatDTO {
    private Long ssId;
    private String rowLabel;
    private Integer colNum;
    private String seatType;
    private BigDecimal actualPrice;
  }
}