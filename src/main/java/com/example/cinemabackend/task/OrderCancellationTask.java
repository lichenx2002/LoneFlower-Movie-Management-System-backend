package com.example.cinemabackend.task;

import com.example.cinemabackend.service.IOrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderCancellationTask {
  private static final Logger logger = LoggerFactory.getLogger(OrderCancellationTask.class);

  @Autowired
  private IOrdersService ordersService;

  // 每5分钟执行一次
  @Scheduled(fixedRate = 300000)
  public void cancelExpiredOrders() {
    logger.info("Starting scheduled task to cancel expired orders");
    try {
      int count = ordersService.cancelExpiredOrders();
      logger.info("Successfully cancelled {} expired orders", count);
    } catch (Exception e) {
      logger.error("Error in cancelExpiredOrders task: {}", e.getMessage(), e);
    }
  }
}