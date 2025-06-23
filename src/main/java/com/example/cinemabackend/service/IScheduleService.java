package com.example.cinemabackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.cinemabackend.entity.Schedule;
import java.util.List;
import java.util.Map;
import com.example.cinemabackend.dto.ScheduleDetailDTO;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
public interface IScheduleService extends IService<Schedule> {
  /**
   * 获取电影排期信息
   * 
   * @param movieId 电影ID
   * @return 按日期分组的排期信息
   */
  Map<String, Object> getMovieSchedules(Integer movieId);

  List<Schedule> getMovieSchedules();

  /**
   * 获取排期座位信息
   * 
   * @param scheduleId 排期ID
   * @return 座位信息
   */
  Map<String, Object> getScheduleSeats(Integer scheduleId);

  /**
   * 检查座位是否可用
   * 
   * @param scheduleId 排期ID
   * @param seatIds    座位ID列表
   * @return 是否可用
   */
  boolean checkSeatsAvailable(Integer scheduleId, List<Integer> seatIds);

  /**
   * 锁定座位
   * 
   * @param scheduleId 排期ID
   * @param seatIds    座位ID列表
   * @param userId     用户ID
   * @return 是否锁定成功
   */
  boolean lockSeats(Integer scheduleId, List<Integer> seatIds, Integer userId);

  /**
   * 解锁座位
   * 
   * @param scheduleId 排期ID
   * @param seatIds    座位ID列表
   * @return 是否解锁成功
   */
  boolean unlockSeats(Integer scheduleId, List<Integer> seatIds);

  /**
   * 获取场次详细信息
   * 
   * @param scheduleId 场次ID
   * @return 包含电影、影厅和座位信息的完整场次信息
   */
  ScheduleDetailDTO getScheduleDetail(Integer scheduleId);
}
