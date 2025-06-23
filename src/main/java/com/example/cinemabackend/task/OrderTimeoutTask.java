package com.example.cinemabackend.task;

import com.example.cinemabackend.service.IOrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderTimeoutTask {

  @Autowired
  private IOrdersService ordersService;

  /**
   * 每分钟执行一次，检查并取消超时订单
   */
  @Scheduled(fixedRate = 60000)
  public void checkAndCancelExpiredOrders() {
    int cancelledCount = ordersService.cancelExpiredOrders();
    if (cancelledCount > 0) {
      System.out.println("已取消 " + cancelledCount + " 个超时订单");
    }
  }
}