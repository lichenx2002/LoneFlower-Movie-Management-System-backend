package com.example.cinemabackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.cinemabackend.entity.SeatTemplate;
import com.example.cinemabackend.mapper.SeatTemplateMapper;
import com.example.cinemabackend.service.ISeatTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
@Slf4j
@RestController
@RequestMapping("/seat-templates")
@CrossOrigin
public class SeatTemplateController {

  @Autowired
  private ISeatTemplateService seatTemplateService;

  /**
   * 获取影厅的座位模板列表
   */
  @GetMapping("/hall/{hallId}")
  public ResponseEntity<List<SeatTemplate>> getSeatTemplatesByHallId(@PathVariable Integer hallId) {
    List<SeatTemplate> templates = seatTemplateService.getBaseMapper().selectList(
        new QueryWrapper<SeatTemplate>().eq("hall_id", hallId));
    return ResponseEntity.ok(templates);
  }

  /**
   * 初始化影厅座位模板
   */
  @PostMapping("/hall/{hallId}/init")
  public ResponseEntity<List<SeatTemplate>> initHallSeatTemplates(@PathVariable Integer hallId) {
    try {
      // 先删除该影厅的现有座位模板
      seatTemplateService.remove(new LambdaQueryWrapper<SeatTemplate>()
          .eq(SeatTemplate::getHallId, hallId));

      List<SeatTemplate> templates = new ArrayList<>();

      // 生成A排座位
      for (int i = 1; i <= 24; i++) {
        SeatTemplate template = new SeatTemplate()
            .setHallId(hallId)
            .setRowLabel("A")
            .setColNum(i)
            .setSeatType(SeatTemplate.SeatType.NORMAL);

        // 逐个保存并获取ID
        boolean success = seatTemplateService.save(template);
        if (success) {
          templates.add(template);
          log.info("保存座位模板成功: {}", template);
        } else {
          log.error("保存座位模板失败: {}", template);
        }
      }

      log.info("所有座位模板保存完成，共 {} 个", templates.size());
      return ResponseEntity.ok(templates);
    } catch (Exception e) {
      log.error("初始化座位模板失败", e);
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * 获取座位模板详情
   */
  @GetMapping("/{id}")
  public ResponseEntity<SeatTemplate> getSeatTemplateDetail(@PathVariable Integer id) {
    try {
      SeatTemplate template = seatTemplateService.getById(id);
      return template != null ? ResponseEntity.ok(template) : ResponseEntity.notFound().build();
    } catch (Exception e) {
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * 创建座位模板
   */
  @PostMapping
  public ResponseEntity<SeatTemplate> createSeatTemplate(@RequestBody SeatTemplate template) {
    try {
      seatTemplateService.save(template);
      return ResponseEntity.ok(template);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * 批量创建座位模板
   */
  @PostMapping("/batch")
  public boolean createSeatTemplates(@RequestBody List<SeatTemplate> seatTemplates) {
    return seatTemplateService.saveBatch(seatTemplates);
  }

  /**
   * 更新座位模板
   */
  @PutMapping("/{id}")
  public ResponseEntity<SeatTemplate> updateSeatTemplate(@PathVariable Integer id, @RequestBody SeatTemplate template) {
    try {
      template.setId(id);
      seatTemplateService.updateById(template);
      return ResponseEntity.ok(template);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * 删除座位模板
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSeatTemplate(@PathVariable Integer id) {
    try {
      seatTemplateService.removeById(id);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * 删除影厅的所有座位模板
   */
  @DeleteMapping("/hall/{hallId}")
  public boolean deleteHallSeatTemplates(@PathVariable Integer hallId) {
    return seatTemplateService.remove(
        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SeatTemplate>()
            .eq("hall_id", hallId));
  }
}
