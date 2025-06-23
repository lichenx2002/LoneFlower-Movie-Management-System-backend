package com.example.cinemabackend.dto;

import com.example.cinemabackend.entity.Movie;
import com.example.cinemabackend.entity.Hall;
import com.example.cinemabackend.entity.Schedule;
import com.example.cinemabackend.entity.ScheduleSeat;
import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class ScheduleDetailDTO {
  // 场次基本信息
  private Schedule schedule;

  // 电影信息
  private Movie movie;

  // 影厅信息
  private Hall hall;

  // 座位信息列表
  private List<SeatInfoDTO> seats;

  @Data
  public static class SeatInfoDTO {
    private Long ssId; // 场次座位ID
    private String rowLabel; // 排号
    private Integer colNum; // 座位号
    private String seatType; // 座位类型
    private String status; // 座位状态
    private BigDecimal price; // 座位价格
    private Integer userId; // 锁定用户ID
    private LocalDateTime lockTime; // 锁定时间
  }
}