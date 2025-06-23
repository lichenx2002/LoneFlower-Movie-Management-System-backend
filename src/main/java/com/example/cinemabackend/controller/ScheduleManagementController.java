package com.example.cinemabackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.cinemabackend.entity.Schedule;
import com.example.cinemabackend.entity.ScheduleSeat;
import com.example.cinemabackend.service.IScheduleService;
import com.example.cinemabackend.service.IScheduleSeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 * 场次管理控制器
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
@RestController
@RequestMapping("/schedule")
@CrossOrigin
public class ScheduleManagementController {

  @Autowired
  private IScheduleService scheduleService;

  @Autowired
  private IScheduleSeatService scheduleSeatService;

  /**
   * 检查时间段冲突
   */
  private boolean hasTimeConflict(Schedule newSchedule) {
    try {
      QueryWrapper<Schedule> queryWrapper = new QueryWrapper<>();
      queryWrapper.eq("hall_id", newSchedule.getHallId())
          .and(wrapper -> wrapper
              .and(w -> w.le("start_time", newSchedule.getStartTime())
                  .ge("end_time", newSchedule.getStartTime()))
              .or(w -> w.le("start_time", newSchedule.getEndTime())
                  .ge("end_time", newSchedule.getEndTime()))
              .or(w -> w.ge("start_time", newSchedule.getStartTime())
                  .le("end_time", newSchedule.getEndTime())));

      // 如果是更新操作，排除当前场次
      if (newSchedule.getScheduleId() != null) {
        queryWrapper.ne("schedule_id", newSchedule.getScheduleId());
      }

      return scheduleService.count(queryWrapper) > 0;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 添加新场次
   */
  @PostMapping
  public ResponseEntity<?> addSchedule(@RequestBody Schedule schedule) {
    try {
      // 验证必填字段
      if (schedule.getMovieId() == null) {
        return ResponseEntity.badRequest().body("电影ID不能为空");
      }
      if (schedule.getHallId() == null) {
        return ResponseEntity.badRequest().body("影厅ID不能为空");
      }
      if (schedule.getStartTime() == null) {
        return ResponseEntity.badRequest().body("开始时间不能为空");
      }
      if (schedule.getEndTime() == null) {
        return ResponseEntity.badRequest().body("结束时间不能为空");
      }
      if (schedule.getBasePrice() == null) {
        return ResponseEntity.badRequest().body("基础价格不能为空");
      }

      // 验证时间逻辑
      if (schedule.getStartTime().isAfter(schedule.getEndTime())) {
        return ResponseEntity.badRequest().body("开始时间不能晚于结束时间");
      }

      // 检查时间段冲突
      if (hasTimeConflict(schedule)) {
        return ResponseEntity.badRequest().body("该影厅在指定时间段已有其他场次安排");
      }

      scheduleService.save(schedule);
      return ResponseEntity.ok(schedule);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).body("添加场次失败: " + e.getMessage());
    }
  }

  /**
   * 根据ID删除场次
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSchedule(@PathVariable Integer id) {
    // 先删除相关的场次座位
    QueryWrapper<ScheduleSeat> seatQueryWrapper = new QueryWrapper<>();
    seatQueryWrapper.eq("schedule_id", id);
    scheduleSeatService.remove(seatQueryWrapper);

    // 再删除场次
    scheduleService.removeById(id);
    return ResponseEntity.ok().build();
  }

  /**
   * 更新场次信息
   */
  @PutMapping("/{id}")
  public ResponseEntity<?> updateSchedule(@PathVariable Integer id, @RequestBody Schedule schedule) {
    schedule.setScheduleId(id);

    // 检查时间段冲突
    if (hasTimeConflict(schedule)) {
      return ResponseEntity.badRequest().body("该影厅在指定时间段已有其他场次安排");
    }

    scheduleService.updateById(schedule);
    return ResponseEntity.ok(schedule);
  }

  /**
   * 根据ID查询场次
   */
  @GetMapping("/{id}")
  public ResponseEntity<Schedule> getSchedule(@PathVariable Integer id) {
    Schedule schedule = scheduleService.getById(id);
    return schedule != null ? ResponseEntity.ok(schedule) : ResponseEntity.notFound().build();
  }

  /**
   * 分页查询场次列表
   */
  @GetMapping("/list")
  public ResponseEntity<Page<Schedule>> listSchedules(
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) Integer movieId,
      @RequestParam(required = false) Integer hallId,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {

    Page<Schedule> page = new Page<>(current, size);
    QueryWrapper<Schedule> queryWrapper = new QueryWrapper<>();

    if (movieId != null) {
      queryWrapper.eq("movie_id", movieId);
    }
    if (hallId != null) {
      queryWrapper.eq("hall_id", hallId);
    }
    if (startDate != null && !startDate.isEmpty()) {
      LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
      queryWrapper.ge("start_time", startDateTime);
    }
    if (endDate != null && !endDate.isEmpty()) {
      LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
      queryWrapper.le("start_time", endDateTime);
    }

    // 按开始时间排序
    queryWrapper.orderByAsc("start_time");

    Page<Schedule> result = scheduleService.page(page, queryWrapper);
    return ResponseEntity.ok(result);
  }

  /**
   * 获取所有场次
   */
  @GetMapping("/all")
  public ResponseEntity<List<Schedule>> getAllSchedules() {
    List<Schedule> schedules = scheduleService.list();
    return ResponseEntity.ok(schedules);
  }
}