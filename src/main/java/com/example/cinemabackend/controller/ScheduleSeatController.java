package com.example.cinemabackend.controller;

import com.example.cinemabackend.entity.ScheduleSeat;
import com.example.cinemabackend.service.IScheduleSeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
@RestController
@RequestMapping("/schedule-seats")
@CrossOrigin
public class ScheduleSeatController {

  @Autowired
  private IScheduleSeatService scheduleSeatService;

  /**
   * 获取场次的座位状态列表
   */
  @GetMapping("/schedule/{scheduleId}")
  public List<ScheduleSeat> getScheduleSeats(@PathVariable Integer scheduleId) {
    return scheduleSeatService.list(
        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ScheduleSeat>()
            .eq("schedule_id", scheduleId));
  }

  /**
   * 获取座位状态详情
   */
  @GetMapping("/{id}")
  public ScheduleSeat getScheduleSeatDetail(@PathVariable Integer id) {
    return scheduleSeatService.getById(id);
  }

  /**
   * 更新座位状态
   */
  @PutMapping("/{id}")
  public boolean updateScheduleSeat(@PathVariable Integer id, @RequestBody ScheduleSeat scheduleSeat) {
    scheduleSeat.setSsId(1L);
    return scheduleSeatService.updateById(scheduleSeat);
  }

  /**
   * 批量更新座位状态
   */
  @PutMapping("/batch")
  public boolean updateScheduleSeats(@RequestBody List<ScheduleSeat> scheduleSeats) {
    return scheduleSeatService.updateBatchById(scheduleSeats);
  }

  /**
   * 删除座位状态
   */
  @DeleteMapping("/{id}")
  public boolean deleteScheduleSeat(@PathVariable Integer id) {
    return scheduleSeatService.removeById(id);
  }

  /**
   * 删除场次的所有座位状态
   */
  @DeleteMapping("/schedule/{scheduleId}")
  public boolean deleteScheduleSeats(@PathVariable Integer scheduleId) {
    return scheduleSeatService.remove(
        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ScheduleSeat>()
            .eq("schedule_id", scheduleId));
  }

  @PostMapping("/lock")
  public ResponseEntity<Boolean> lockSeats(
      @RequestParam Integer scheduleId,
      @RequestBody List<Integer> seatIds,
      @RequestParam Integer userId) {
    ScheduleSeat scheduleSeat = new ScheduleSeat();
    scheduleSeat.setSsId(1L);
    scheduleSeat.setScheduleId(scheduleId);
    scheduleSeat.setStatus("LOCKED");
    scheduleSeat.setUserId(userId);
    scheduleSeat.setLockTime(LocalDateTime.now());
    return ResponseEntity.ok(scheduleSeatService.save(scheduleSeat));
  }
}
