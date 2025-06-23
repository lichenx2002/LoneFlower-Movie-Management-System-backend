package com.example.cinemabackend.service.impl;

import com.example.cinemabackend.dto.OrderDetailDTO;
import com.example.cinemabackend.entity.*;
import com.example.cinemabackend.mapper.OrdersMapper;
import com.example.cinemabackend.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

  private static final Logger logger = LoggerFactory.getLogger(OrdersServiceImpl.class);

  @Autowired
  private IOrderSeatService orderSeatService;

  @Autowired
  private IScheduleSeatService scheduleSeatService;

  @Autowired
  private IScheduleService scheduleService;

  @Autowired
  private ISeatTemplateService seatTemplateService;

  @Autowired
  private IUserService userService;

  @Autowired
  private IMovieService movieService;

  @Autowired
  private IHallService hallService;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Orders createOrder(Integer userId, List<Long> ssIds, BigDecimal totalAmount) {
    logger.info("Creating order for user: {}, seats: {}, total amount: {}", userId, ssIds, totalAmount);

    try {
      // 获取第一个座位的schedule_id
      ScheduleSeat firstSeat = scheduleSeatService.getById(ssIds.get(0));
      if (firstSeat == null) {
        throw new RuntimeException("Seat not found");
      }

      // 创建订单
      Orders order = new Orders();
      order.setUserId(userId);
      order.setScheduleId(firstSeat.getScheduleId()); // 设置schedule_id
      order.setTotalAmount(totalAmount);
      order.setOrderTime(LocalDateTime.now());
      order.setStatus("UNPAID");

      // 保存订单
      save(order);
      logger.info("Order saved with ID: {}", order.getOrderId());

      // 计算每个座位的实际价格
      BigDecimal seatPrice = totalAmount.divide(new BigDecimal(ssIds.size()), 2, BigDecimal.ROUND_HALF_UP);

      // 更新座位状态
      for (Long ssId : ssIds) {
        ScheduleSeat seat = scheduleSeatService.getById(ssId);
        if (seat == null) {
          throw new RuntimeException("Seat not found: " + ssId);
        }
        if (!"AVAILABLE".equals(seat.getStatus())) {
          throw new RuntimeException("Seat is not available: " + ssId);
        }

        // 记录更新前的状态
        logger.info("Before update - Seat ID: {}, Status: {}, User ID: {}, Lock Time: {}",
            ssId, seat.getStatus(), seat.getUserId(), seat.getLockTime());

        seat.setStatus("LOCKED");
        seat.setUserId(userId);
        seat.setLockTime(LocalDateTime.now());

        // 记录更新后的状态
        logger.info("After update - Seat ID: {}, Status: {}, User ID: {}, Lock Time: {}",
            ssId, seat.getStatus(), seat.getUserId(), seat.getLockTime());

        boolean updated = scheduleSeatService.updateById(seat);
        logger.info("Update result for seat {}: {}", ssId, updated);

        // 创建订单-座位关联
        OrderSeat orderSeat = new OrderSeat();
        orderSeat.setOrderId(order.getOrderId());
        orderSeat.setSsId(ssId);
        orderSeat.setActualPrice(seatPrice); // 使用计算出的座位价格
        orderSeatService.save(orderSeat);
      }

      return order;
    } catch (Exception e) {
      logger.error("Error creating order", e);
      throw new RuntimeException("Failed to create order: " + e.getMessage());
    }
  }

  @Override
  @Transactional
  public boolean payOrder(Long orderId) {
    // 1. 获取订单
    Orders order = getById(orderId);
    if (order == null || !"UNPAID".equals(order.getStatus())) {
      return false;
    }

    // 2. 生成支付单号 (使用时间戳 + 订单ID)
    String paymentId = "PAY" + System.currentTimeMillis() + orderId;
    order.setPaymentId(paymentId);
    order.setStatus("PAID");
    updateById(order);

    // 3. 更新座位状态为已占用
    List<OrderSeat> orderSeats = orderSeatService.lambdaQuery()
        .eq(OrderSeat::getOrderId, orderId)
        .list();

    for (OrderSeat orderSeat : orderSeats) {
      ScheduleSeat scheduleSeat = scheduleSeatService.getById(orderSeat.getSsId());
      scheduleSeat.setStatus("OCCUPIED");
      scheduleSeatService.updateById(scheduleSeat);
    }

    return true;
  }

  @Override
  @Transactional
  public int cancelExpiredOrders() {
    // 1. 查找超时的待支付订单（15分钟前创建的）
    LocalDateTime expireTime = LocalDateTime.now().minusMinutes(15);
    List<Orders> expiredOrders = lambdaQuery()
        .eq(Orders::getStatus, "UNPAID")
        .lt(Orders::getOrderTime, expireTime)
        .list();

    int count = 0;
    for (Orders order : expiredOrders) {
      // 2. 更新订单状态为已取消
      order.setStatus("CANCELED");
      updateById(order);

      // 3. 更新相关座位状态为可选
      List<OrderSeat> orderSeats = orderSeatService.lambdaQuery()
          .eq(OrderSeat::getOrderId, order.getOrderId())
          .list();

      for (OrderSeat orderSeat : orderSeats) {
        ScheduleSeat scheduleSeat = scheduleSeatService.getById(orderSeat.getSsId());
        if (scheduleSeat != null) {
          scheduleSeat.setStatus("AVAILABLE");
          scheduleSeatService.updateById(scheduleSeat);
        }
      }
      count++;
    }

    return count;
  }

  @Override
  public OrderDetailDTO getOrderDetail(Long orderId) {
    // 获取订单基本信息
    Orders order = getById(orderId);
    if (order == null) {
      throw new RuntimeException("Order not found");
    }

    // 获取用户信息
    User user = userService.getById(order.getUserId());

    // 获取场次信息
    Schedule schedule = scheduleService.getById(order.getScheduleId());

    // 获取电影信息
    Movie movie = movieService.getById(schedule.getMovieId());

    // 获取影厅信息
    Hall hall = hallService.getById(schedule.getHallId());

    // 获取座位信息
    List<OrderSeat> orderSeats = orderSeatService.lambdaQuery()
        .eq(OrderSeat::getOrderId, orderId)
        .list();

    List<OrderDetailDTO.OrderSeatDTO> seatDTOs = orderSeats.stream()
        .map(orderSeat -> {
          ScheduleSeat scheduleSeat = scheduleSeatService.getById(orderSeat.getSsId());
          SeatTemplate seatTemplate = seatTemplateService.getById(scheduleSeat.getTemplateId());

          OrderDetailDTO.OrderSeatDTO seatDTO = new OrderDetailDTO.OrderSeatDTO();
          seatDTO.setSsId(orderSeat.getSsId());
          seatDTO.setRowLabel(seatTemplate.getRowLabel());
          seatDTO.setColNum(seatTemplate.getColNum());
          seatDTO.setSeatType(seatTemplate.getSeatType().toString());
          seatDTO.setActualPrice(orderSeat.getActualPrice());
          return seatDTO;
        })
        .collect(Collectors.toList());

    // 构建订单详情DTO
    OrderDetailDTO orderDetail = new OrderDetailDTO();
    orderDetail.setOrderId(order.getOrderId());
    orderDetail.setUserId(order.getUserId());
    orderDetail.setUsername(user.getUsername());
    orderDetail.setUserPhone(user.getPhone());
    orderDetail.setUserEmail(user.getEmail());

    orderDetail.setMovieId(movie.getMovieId());
    orderDetail.setMovieTitle(movie.getTitle());
    orderDetail.setMovieEnglishTitle(movie.getEnglishTitle());
    orderDetail.setMoviePoster(movie.getPosterUrl());
    orderDetail.setMovieGenres(movie.getGenres());
    orderDetail.setMovieDuration(movie.getDuration());

    orderDetail.setHallId(hall.getHallId());
    orderDetail.setHallName(hall.getName());
    orderDetail.setHallType(hall.getType());

    orderDetail.setScheduleId(schedule.getScheduleId());
    orderDetail.setStartTime(schedule.getStartTime());
    orderDetail.setBasePrice(schedule.getBasePrice());

    orderDetail.setTotalAmount(order.getTotalAmount());
    orderDetail.setOrderTime(order.getOrderTime());
    orderDetail.setStatus(order.getStatus());
    orderDetail.setPaymentId(order.getPaymentId());

    orderDetail.setSeats(seatDTOs);

    return orderDetail;
  }

  @Override
  public List<OrderDetailDTO> getUserOrders(Integer userId) {
    // 1. 获取用户的所有订单
    List<Orders> orders = lambdaQuery()
        .eq(Orders::getUserId, userId)
        .orderByDesc(Orders::getOrderTime)
        .list();

    // 2. 转换为 OrderDetailDTO
    return orders.stream()
        .map(order -> getOrderDetail(order.getOrderId()))
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean cancelOrder(Long orderId) {
    // 1. 获取订单
    Orders order = getById(orderId);
    if (order == null || !"UNPAID".equals(order.getStatus())) {
      return false;
    }

    // 2. 更新订单状态为已取消
    order.setStatus("CANCELED");
    updateById(order);

    // 3. 更新相关座位状态为可选
    List<OrderSeat> orderSeats = orderSeatService.lambdaQuery()
        .eq(OrderSeat::getOrderId, orderId)
        .list();

    for (OrderSeat orderSeat : orderSeats) {
      ScheduleSeat scheduleSeat = scheduleSeatService.getById(orderSeat.getSsId());
      if (scheduleSeat != null) {
        scheduleSeat.setStatus("AVAILABLE");
        scheduleSeatService.updateById(scheduleSeat);
      }
    }

    return true;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean refundOrder(Long orderId) {
    // 1. 获取订单
    Orders order = getById(orderId);
    if (order == null || !"PAID".equals(order.getStatus())) {
      return false;
    }

    // 2. 更新订单状态为已退款
    order.setStatus("REFUNDED");
    updateById(order);

    // 3. 更新相关座位状态为可选
    List<OrderSeat> orderSeats = orderSeatService.lambdaQuery()
        .eq(OrderSeat::getOrderId, orderId)
        .list();

    for (OrderSeat orderSeat : orderSeats) {
      ScheduleSeat scheduleSeat = scheduleSeatService.getById(orderSeat.getSsId());
      if (scheduleSeat != null) {
        scheduleSeat.setStatus("AVAILABLE");
        scheduleSeatService.updateById(scheduleSeat);
      }
    }

    return true;
  }

  @Override
  public Page<OrderDetailDTO> getOrderList(Page<OrderDetailDTO> page, String orderId, String status, Integer userId,
      String sortField, String sortOrder) {
    try {
      // 1. 构建查询条件
      QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();

      if (orderId != null && !orderId.trim().isEmpty()) {
        queryWrapper.like("order_id", orderId.trim());
      }
      if (status != null && !status.trim().isEmpty()) {
        queryWrapper.eq("status", status.trim());
      }
      if (userId != null) {
        queryWrapper.eq("user_id", userId);
      }

      // 字段名映射（前端字段名 -> 数据库字段名）
      String dbSortField = sortField;
      if ("orderId".equals(sortField)) {
        dbSortField = "order_id";
      } else if ("orderTime".equals(sortField)) {
        dbSortField = "order_time";
      }

      // 排序
      if ("asc".equalsIgnoreCase(sortOrder)) {
        queryWrapper.orderByAsc(dbSortField);
      } else {
        queryWrapper.orderByDesc(dbSortField);
      }

      // 2. 分页查询订单
      Page<Orders> ordersPage = new Page<>(page.getCurrent(), page.getSize());
      Page<Orders> result = page(ordersPage, queryWrapper);

      // 3. 转换为 OrderDetailDTO
      List<OrderDetailDTO> orderDetailDTOs = result.getRecords().stream()
          .map(order -> getOrderDetail(order.getOrderId()))
          .collect(Collectors.toList());

      // 4. 构建返回结果
      Page<OrderDetailDTO> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
      resultPage.setRecords(orderDetailDTOs);

      return resultPage;
    } catch (Exception e) {
      logger.error("Error getting order list", e);
      throw new RuntimeException("Failed to get order list: " + e.getMessage());
    }
  }
}
