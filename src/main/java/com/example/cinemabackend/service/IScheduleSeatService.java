package com.example.cinemabackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.cinemabackend.entity.ScheduleSeat;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
public interface IScheduleSeatService extends IService<ScheduleSeat> {
  /**
   * 获取排期的所有座位
   * 
   * @param scheduleId 排期ID
   * @return 座位列表
   */
  List<ScheduleSeat> getByScheduleId(Integer scheduleId);
}
