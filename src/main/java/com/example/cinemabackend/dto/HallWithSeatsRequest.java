package com.example.cinemabackend.dto;

import lombok.Data;
import java.util.List;

@Data
public class HallWithSeatsRequest {
  private String name;
  private String type;
  private Integer rowCount;
  private Integer colCount;
  private String rowLabels;
  private List<SeatDto> seats;

  @Data
  public static class SeatDto {
    private String rowLabel;
    private Integer colNum;
    private String seatType;
  }
}