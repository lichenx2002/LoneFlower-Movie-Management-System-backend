package com.example.cinemabackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cinemabackend.entity.ScheduleSeat;
import com.example.cinemabackend.mapper.ScheduleSeatMapper;
import com.example.cinemabackend.service.IScheduleSeatService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
@Service
public class ScheduleSeatServiceImpl extends ServiceImpl<ScheduleSeatMapper, ScheduleSeat>
    implements IScheduleSeatService {

  @Override
  public List<ScheduleSeat> getByScheduleId(Integer scheduleId) {
    QueryWrapper<ScheduleSeat> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("schedule_id", scheduleId)
        .orderByAsc("row", "col");
    return this.list(queryWrapper);
  }
}
