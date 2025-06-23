package com.example.cinemabackend.controller;

import com.example.cinemabackend.entity.Hall;
import com.example.cinemabackend.entity.SeatTemplate;
import com.example.cinemabackend.service.IHallService;
import com.example.cinemabackend.service.ISeatTemplateService;
import com.example.cinemabackend.dto.HallWithSeatsRequest;
import com.example.cinemabackend.common.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
@RestController
@RequestMapping("/hall")
@CrossOrigin
public class HallController {

  @Autowired
  private IHallService hallService;

  @Autowired
  private ISeatTemplateService seatTemplateService;

  /**
   * 获取所有影厅列表
   */
  @GetMapping("/all")
  public ResponseEntity<List<Hall>> getAllHalls() {
    try {
      List<Hall> halls = hallService.list();
      return ResponseEntity.ok(halls);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * 获取影厅详情
   */
  @GetMapping("/{id}")
  public ResponseEntity<Hall> getHallDetail(@PathVariable Integer id) {
    try {
      Hall hall = hallService.getById(id);
      return hall != null ? ResponseEntity.ok(hall) : ResponseEntity.notFound().build();
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * 创建影厅
   */
  @PostMapping
  public ResponseEntity<Hall> createHall(@RequestBody Hall hall) {
    try {
      hallService.save(hall);
      return ResponseEntity.ok(hall);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * 更新影厅信息
   */
  @PutMapping("/{id}")
  public ResponseEntity<Hall> updateHall(@PathVariable Integer id, @RequestBody Hall hall) {
    try {
      hall.setHallId(id);
      hallService.updateById(hall);
      return ResponseEntity.ok(hall);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * 删除影厅
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteHall(@PathVariable Integer id) {
    try {
      hallService.removeById(id);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * 分页查询影厅列表
   */
  @GetMapping("/list")
  public ResponseEntity<Page<Hall>> getHallList(
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String type,
      @RequestParam(defaultValue = "hall_id") String sortField,
      @RequestParam(defaultValue = "desc") String sortOrder) {
    try {
      Page<Hall> page = new Page<>(current, size);

      QueryWrapper<Hall> queryWrapper = new QueryWrapper<>();

      // 搜索条件
      if (name != null && !name.trim().isEmpty()) {
        queryWrapper.like("name", name.trim());
      }
      if (type != null && !type.trim().isEmpty()) {
        queryWrapper.eq("type", type.trim());
      }

      // 字段名映射（前端字段名 -> 数据库字段名）
      String dbSortField = sortField;
      if ("hallId".equals(sortField)) {
        dbSortField = "hall_id";
      } else if ("rowCount".equals(sortField)) {
        dbSortField = "row_count";
      } else if ("colCount".equals(sortField)) {
        dbSortField = "col_count";
      } else if ("rowLabels".equals(sortField)) {
        dbSortField = "row_labels";
      }

      // 排序
      if ("asc".equalsIgnoreCase(sortOrder)) {
        queryWrapper.orderByAsc(dbSortField);
      } else {
        queryWrapper.orderByDesc(dbSortField);
      }

      Page<Hall> result = hallService.page(page, queryWrapper);

      // 确保total字段正确设置
      if (result.getTotal() == 0 && result.getRecords().size() > 0) {
        // 如果total为0但有数据，手动计算总数
        QueryWrapper<Hall> countWrapper = new QueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
          countWrapper.like("name", name.trim());
        }
        if (type != null && !type.trim().isEmpty()) {
          countWrapper.eq("type", type.trim());
        }
        long total = hallService.count(countWrapper);
        result.setTotal(total);
      }

      return ResponseEntity.ok(result);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * 一键设置影厅座位类别
   */
  @PutMapping("/{id}/seats/bulk-set-type")
  public ResponseEntity<?> bulkSetSeatType(
      @PathVariable Integer id,
      @RequestParam String seatType) {
    try {
      // 验证影厅是否存在
      Hall hall = hallService.getById(id);
      if (hall == null) {
        return ResponseEntity.notFound().build();
      }

      // 验证座位类型是否有效
      try {
        SeatTemplate.SeatType.valueOf(seatType);
      } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body("无效的座位类型");
      }

      // 批量更新该影厅的所有座位模板
      QueryWrapper<SeatTemplate> queryWrapper = new QueryWrapper<>();
      queryWrapper.eq("hall_id", id);

      SeatTemplate updateTemplate = new SeatTemplate();
      updateTemplate.setSeatType(SeatTemplate.SeatType.valueOf(seatType));

      boolean success = seatTemplateService.update(updateTemplate, queryWrapper);

      if (success) {
        return ResponseEntity.ok().body("座位类型设置成功");
      } else {
        return ResponseEntity.status(500).body("座位类型设置失败");
      }
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).body("设置座位类型失败: " + e.getMessage());
    }
  }

  /**
   * 一次性创建影厅和座位模板
   */
  @PostMapping("/with-seats")
  public ResponseEntity<?> createHallWithSeats(@RequestBody HallWithSeatsRequest request) {
    try {
      // 1. 保存影厅
      Hall hall = new Hall();
      hall.setName(request.getName());
      hall.setType(request.getType());
      hall.setRowCount(request.getRowCount());
      hall.setColCount(request.getColCount());
      hall.setRowLabels(request.getRowLabels());
      hallService.save(hall);

      // 2. 批量保存座位模板
      if (request.getSeats() != null && !request.getSeats().isEmpty()) {
        java.util.List<SeatTemplate> seatTemplates = request.getSeats().stream().map(seatDto -> {
          SeatTemplate st = new SeatTemplate();
          st.setHallId(hall.getHallId());
          st.setRowLabel(seatDto.getRowLabel());
          st.setColNum(seatDto.getColNum());
          st.setSeatType(SeatTemplate.SeatType.valueOf(seatDto.getSeatType()));
          return st;
        }).collect(Collectors.toList());
        seatTemplateService.saveBatch(seatTemplates);
      }
      return ResponseEntity.ok(hall);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).body("创建影厅及座位失败");
    }
  }
}
