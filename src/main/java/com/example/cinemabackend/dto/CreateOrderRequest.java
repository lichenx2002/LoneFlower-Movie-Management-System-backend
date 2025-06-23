package com.example.cinemabackend.dto;

import java.util.List;
import java.math.BigDecimal;

public class CreateOrderRequest {
  private Integer userId;
  private List<Integer> ssIds;
  private BigDecimal totalAmount;

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public List<Integer> getSsIds() {
    return ssIds;
  }

  public void setSsIds(List<Integer> ssIds) {
    this.ssIds = ssIds;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }
}