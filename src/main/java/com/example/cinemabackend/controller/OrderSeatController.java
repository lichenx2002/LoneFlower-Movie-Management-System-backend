package com.example.cinemabackend.controller;

import com.example.cinemabackend.dto.OrderDetailDTO;
import com.example.cinemabackend.service.IOrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
@RestController
@RequestMapping("/order-seat")
@CrossOrigin
public class OrderSeatController {

  @Autowired
  private IOrdersService ordersService;

  @GetMapping("/{orderId}/detail")
  public OrderDetailDTO getOrderDetail(@PathVariable Long orderId) {
    return ordersService.getOrderDetail(orderId);
  }
}
