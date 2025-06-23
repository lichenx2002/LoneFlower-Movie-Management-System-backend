package com.example.cinemabackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.cinemabackend.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.math.BigDecimal;
import com.example.cinemabackend.dto.OrderDetailDTO;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
public interface IOrdersService extends IService<Orders> {
  /**
   * 创建订单
   * 
   * @param userId      用户ID
   * @param ssIds       座位ID列表
   * @param totalAmount 总金额
   * @return 订单信息
   */
  Orders createOrder(Integer userId, List<Long> ssIds, BigDecimal totalAmount);

  /**
   * 获取订单详情
   * 
   * @param orderId 订单ID
   * @return 订单详情
   */
  OrderDetailDTO getOrderDetail(Long orderId);

  /**
   * 支付订单
   * 
   * @param orderId 订单ID
   * @return 是否支付成功
   */
  boolean payOrder(Long orderId);

  /**
   * 取消超时订单
   * 
   * @return 取消的订单数量
   */
  int cancelExpiredOrders();

  /**
   * 获取用户订单列表
   * 
   * @param userId 用户ID
   * @return 订单列表
   */
  List<OrderDetailDTO> getUserOrders(Integer userId);

  /**
   * 管理员专用：分页查询订单列表
   * 
   * @param page      分页参数
   * @param orderId   订单ID（可选）
   * @param status    订单状态（可选）
   * @param userId    用户ID（可选）
   * @param sortField 排序字段
   * @param sortOrder 排序方式
   * @return 分页订单列表
   */
  Page<OrderDetailDTO> getOrderList(Page<OrderDetailDTO> page, String orderId, String status, Integer userId,
      String sortField, String sortOrder);

  /**
   * 取消订单
   * 
   * @param orderId 订单ID
   * @return 是否取消成功
   */
  boolean cancelOrder(Long orderId);

  /**
   * 退票（退款）
   * 
   * @param orderId 订单ID
   * @return 是否退票成功
   */
  boolean refundOrder(Long orderId);
}
