package com.example.cinemabackend.controller;

import com.example.cinemabackend.dto.ScheduleDetailDTO;
import com.example.cinemabackend.service.IScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
@RestController
@RequestMapping("/schedules")
@CrossOrigin
public class ScheduleController {

  @Autowired
  private IScheduleService scheduleService;

  /**
   * 获取电影场次信息
   */
  @GetMapping("/movie/{movieId}")
  public ResponseEntity<Map<String, Object>> getMovieSchedules(@PathVariable Integer movieId) {
    return ResponseEntity.ok(scheduleService.getMovieSchedules(movieId));
  }

  /**
   * 获取场次详细信息
   * 
   * @param scheduleId 场次ID
   * @return 包含电影、影厅和座位信息的完整场次信息
   */
  @GetMapping("/{scheduleId}/detail")
  public ResponseEntity<ScheduleDetailDTO> getScheduleDetail(@PathVariable Integer scheduleId) {
    ScheduleDetailDTO detail = scheduleService.getScheduleDetail(scheduleId);
    return ResponseEntity.ok(detail);
  }

  /**
   * 获取场次座位信息
   */
  @GetMapping("/{scheduleId}/seats")
  public ResponseEntity<Map<String, Object>> getScheduleSeats(@PathVariable Integer scheduleId) {
    return ResponseEntity.ok(scheduleService.getScheduleSeats(scheduleId));
  }

  /**
   * 锁定座位
   */
  @PostMapping("/{scheduleId}/seats/lock")
  public ResponseEntity<Boolean> lockSeats(
      @PathVariable Integer scheduleId,
      @RequestBody List<Integer> seatIds,
      @RequestParam Integer userId) {
    return ResponseEntity.ok(scheduleService.lockSeats(scheduleId, seatIds, userId));
  }

  /**
   * 解锁座位
   */
  @PostMapping("/{scheduleId}/seats/unlock")
  public ResponseEntity<Boolean> unlockSeats(
      @PathVariable Integer scheduleId,
      @RequestBody List<Integer> seatIds) {
    return ResponseEntity.ok(scheduleService.unlockSeats(scheduleId, seatIds));
  }

  /**
   * 检查座位是否可用
   */
  @PostMapping("/{scheduleId}/seats/check")
  public ResponseEntity<Boolean> checkSeatsAvailable(
      @PathVariable Integer scheduleId,
      @RequestBody List<Integer> seatIds) {
    return ResponseEntity.ok(scheduleService.checkSeatsAvailable(scheduleId, seatIds));
  }
}
