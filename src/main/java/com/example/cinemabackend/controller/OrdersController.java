package com.example.cinemabackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.cinemabackend.common.Result;
import com.example.cinemabackend.dto.CreateOrderRequest;
import com.example.cinemabackend.dto.OrderDetailDTO;
import com.example.cinemabackend.entity.Orders;
import com.example.cinemabackend.service.IOrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
@CrossOrigin
public class OrdersController {
    private static final Logger logger = LoggerFactory.getLogger(OrdersController.class);

    @Autowired
    private IOrdersService ordersService;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        logger.info("Received order creation request: {}", request);

        try {
            if (request.getUserId() == null || request.getSsIds() == null || request.getSsIds().isEmpty()
                    || request.getTotalAmount() == null) {
                return ResponseEntity.badRequest().body("Missing required parameters");
            }

            Orders order = ordersService.createOrder(
                    request.getUserId(),
                    request.getSsIds().stream().map(Long::valueOf).collect(Collectors.toList()),
                    new BigDecimal(request.getTotalAmount().toString()));

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            logger.error("Error creating order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create order: " + e.getMessage());
        }
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<?> payOrder(@PathVariable Long orderId) {
        try {
            boolean success = ordersService.payOrder(orderId);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body("支付失败");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserOrders(@PathVariable Integer userId) {
        try {
            List<OrderDetailDTO> orders = ordersService.getUserOrders(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error getting user orders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("获取订单列表失败：" + e.getMessage());
        }
    }

    // 管理员专用：分页查询订单列表
    @GetMapping("/list")
    public ResponseEntity<Page<OrderDetailDTO>> getOrderList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer userId,
            @RequestParam(defaultValue = "order_id") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        try {
            Page<OrderDetailDTO> page = new Page<>(current, size);
            Page<OrderDetailDTO> result = ordersService.getOrderList(page, orderId, status, userId, sortField,
                    sortOrder);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error getting order list", e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        try {
            boolean success = ordersService.cancelOrder(orderId);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body("取消订单失败");
            }
        } catch (Exception e) {
            logger.error("Error canceling order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("取消订单失败：" + e.getMessage());
        }
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<?> refundOrder(@PathVariable Long orderId) {
        try {
            boolean success = ordersService.refundOrder(orderId);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body("退票失败");
            }
        } catch (Exception e) {
            logger.error("Error refunding order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("退票失败：" + e.getMessage());
        }
    }
}