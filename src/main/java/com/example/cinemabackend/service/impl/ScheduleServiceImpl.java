package com.example.cinemabackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.cinemabackend.entity.*;
import com.example.cinemabackend.mapper.ScheduleMapper;
import com.example.cinemabackend.mapper.MovieMapper;
import com.example.cinemabackend.mapper.HallMapper;
import com.example.cinemabackend.mapper.SeatTemplateMapper;
import com.example.cinemabackend.dto.ScheduleDetailDTO;
import com.example.cinemabackend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
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
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements IScheduleService {

  @Autowired
  private IHallService hallService;

  @Autowired
  private ISeatTemplateService seatTemplateService;

  @Autowired
  private IScheduleSeatService scheduleSeatService;

  @Autowired
  private MovieMapper movieMapper;

  @Autowired
  private HallMapper hallMapper;

  @Autowired
  private SeatTemplateMapper seatTemplateMapper;

  @Override
  public Map<String, Object> getMovieSchedules(Integer movieId) {
    // 获取未来7天的场次
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime endTime = now.plusDays(7);

    QueryWrapper<Schedule> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("movie_id", movieId)
        .ge("start_time", now)
        .le("start_time", endTime)
        .orderByAsc("start_time");

    List<Schedule> schedules = this.list(queryWrapper);

    // 按日期分组
    Map<String, List<Map<String, Object>>> scheduleMap = new LinkedHashMap<>();

    for (Schedule schedule : schedules) {
      String date = schedule.getStartTime().toLocalDate().toString();

      // 获取影厅信息
      Hall hall = hallService.getById(schedule.getHallId());

      Map<String, Object> scheduleInfo = new HashMap<>();
      scheduleInfo.put("scheduleId", schedule.getScheduleId());
      scheduleInfo.put("startTime", schedule.getStartTime());
      scheduleInfo.put("endTime", schedule.getEndTime());
      scheduleInfo.put("basePrice", schedule.getBasePrice());
      scheduleInfo.put("loverPrice", schedule.getLoverPrice());
      scheduleInfo.put("vipPrice", schedule.getVipPrice());
      scheduleInfo.put("hallId", hall.getHallId());
      scheduleInfo.put("hallName", hall.getName());
      scheduleInfo.put("hallType", hall.getType());

      scheduleMap.computeIfAbsent(date, k -> new ArrayList<>()).add(scheduleInfo);
    }

    Map<String, Object> result = new HashMap<>();
    result.put("schedules", scheduleMap);
    return result;
  }

  @Override
  public List<Schedule> getMovieSchedules() {
    return this.list();
  }

  @Override
  public Map<String, Object> getScheduleSeats(Integer scheduleId) {
    Schedule schedule = this.getById(scheduleId);
    if (schedule == null) {
      return Collections.emptyMap();
    }

    Hall hall = hallService.getById(schedule.getHallId());
    List<ScheduleSeat> seats = scheduleSeatService.getByScheduleId(scheduleId);

    Map<String, Object> result = new HashMap<>();
    result.put("scheduleId", scheduleId);
    result.put("hallId", hall.getHallId());
    result.put("rowCount", hall.getRowCount());
    result.put("colCount", hall.getColCount());
    result.put("seats", seats.stream()
        .map(seat -> {
          Map<String, Object> seatInfo = new HashMap<>();
          seatInfo.put("ssId", seat.getSsId());
          seatInfo.put("templateId", seat.getTemplateId());
          seatInfo.put("status", seat.getStatus());
          seatInfo.put("userId", seat.getUserId());
          seatInfo.put("lockTime", seat.getLockTime());
          return seatInfo;
        })
        .collect(Collectors.toList()));

    return result;
  }

  @Override
  @Transactional
  public boolean lockSeats(Integer scheduleId, List<Integer> seatIds, Integer userId) {
    // 检查座位是否可用
    if (!checkSeatsAvailable(scheduleId, seatIds)) {
      return false;
    }

    // 锁定座位
    List<ScheduleSeat> seats = scheduleSeatService.getByScheduleId(scheduleId);
    seats.stream()
        .filter(seat -> seatIds.contains(seat.getSsId().intValue()))
        .forEach(seat -> {
          seat.setStatus("LOCKED")
              .setUserId(userId)
              .setLockTime(LocalDateTime.now());
          scheduleSeatService.updateById(seat);
        });

    return true;
  }

  @Override
  @Transactional
  public boolean unlockSeats(Integer scheduleId, List<Integer> seatIds) {
    List<ScheduleSeat> seats = scheduleSeatService.getByScheduleId(scheduleId);
    seats.stream()
        .filter(seat -> seatIds.contains(seat.getSsId().intValue()))
        .forEach(seat -> {
          seat.setStatus("AVAILABLE")
              .setUserId(null)
              .setLockTime(null);
          scheduleSeatService.updateById(seat);
        });

    return true;
  }

  @Override
  public boolean checkSeatsAvailable(Integer scheduleId, List<Integer> seatIds) {
    List<ScheduleSeat> seats = scheduleSeatService.getByScheduleId(scheduleId);
    return seats.stream()
        .filter(seat -> seatIds.contains(seat.getSsId().intValue()))
        .allMatch(seat -> "AVAILABLE".equals(seat.getStatus()));
  }

  private double calculateSeatPrice(Schedule schedule, SeatTemplate seat) {
    // 根据座位类型返回对应价格
    switch (seat.getSeatType()) {
      case VIP:
        return schedule.getVipPrice() != null ? schedule.getVipPrice().doubleValue()
            : schedule.getBasePrice().doubleValue() * 1.5;
      case LOVER_LEFT:
      case LOVER_RIGHT:
        return schedule.getLoverPrice() != null ? schedule.getLoverPrice().doubleValue()
            : schedule.getBasePrice().doubleValue() * 1.2;
      default:
        return schedule.getBasePrice().doubleValue();
    }
  }

  /**
   * 获取场次详细信息，包括电影、影厅和座位信息
   */
  @Override
  public ScheduleDetailDTO getScheduleDetail(Integer scheduleId) {
    // 获取场次信息
    Schedule schedule = getById(scheduleId);
    if (schedule == null) {
      throw new RuntimeException("场次不存在");
    }

    // 获取影厅信息
    Hall hall = hallService.getById(schedule.getHallId());
    if (hall == null) {
      throw new RuntimeException("影厅不存在");
    }

    // 获取电影信息
    Movie movie = movieMapper.selectById(schedule.getMovieId());
    if (movie == null) {
      throw new RuntimeException("电影不存在");
    }

    // 获取座位模板
    List<SeatTemplate> templates = seatTemplateService.list(
        new QueryWrapper<SeatTemplate>()
            .eq("hall_id", hall.getHallId()));

    // 获取座位状态
    List<ScheduleSeat> seats = scheduleSeatService.list(
        new QueryWrapper<ScheduleSeat>()
            .eq("schedule_id", scheduleId));

    // 构建座位信息
    List<ScheduleDetailDTO.SeatInfoDTO> seatInfos = new ArrayList<>();
    for (SeatTemplate template : templates) {
      ScheduleDetailDTO.SeatInfoDTO seatInfo = new ScheduleDetailDTO.SeatInfoDTO();
      seatInfo.setRowLabel(template.getRowLabel());
      seatInfo.setColNum(template.getColNum());
      seatInfo.setSeatType(template.getSeatType().name());

      // 设置座位价格
      if (SeatTemplate.SeatType.VIP.equals(template.getSeatType())) {
        seatInfo.setPrice(schedule.getVipPrice());
      } else if (SeatTemplate.SeatType.LOVER_LEFT.equals(template.getSeatType()) ||
          SeatTemplate.SeatType.LOVER_RIGHT.equals(template.getSeatType())) {
        seatInfo.setPrice(schedule.getLoverPrice());
      } else {
        seatInfo.setPrice(schedule.getBasePrice());
      }

      // 查找对应的座位状态
      for (ScheduleSeat seat : seats) {
        if (seat.getTemplateId().equals(template.getId())) {
          seatInfo.setSsId(seat.getSsId());
          seatInfo.setStatus(seat.getStatus());
          seatInfo.setUserId(seat.getUserId());
          seatInfo.setLockTime(seat.getLockTime());
          break;
        }
      }

      // 如果没有找到对应的座位状态，创建一个新的
      if (seatInfo.getSsId() == null) {
        ScheduleSeat newSeat = new ScheduleSeat()
            .setScheduleId(scheduleId)
            .setTemplateId(template.getId())
            .setStatus("AVAILABLE");
        scheduleSeatService.save(newSeat);
        seatInfo.setSsId(newSeat.getSsId());
        seatInfo.setStatus("AVAILABLE");
      }

      seatInfos.add(seatInfo);
    }

    // 构建返回DTO
    ScheduleDetailDTO dto = new ScheduleDetailDTO();
    dto.setSchedule(schedule);
    dto.setMovie(movie);
    dto.setHall(hall);
    dto.setSeats(seatInfos);

    return dto;
  }
}
