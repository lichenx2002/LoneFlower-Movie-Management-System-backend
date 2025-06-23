package com.example.cinemabackend.controller;

import com.example.cinemabackend.entity.Cinema;
import com.example.cinemabackend.service.ICinemaService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author author
 * @since 2025-06-23
 */
@RestController
@RequestMapping("/cinema")
@CrossOrigin
public class CinemaController {

  @Autowired
  private ICinemaService cinemaService;

  /**
   * 获取所有影院列表
   */
  @GetMapping("/all")
  public ResponseEntity<List<Cinema>> getAllCinemas() {
    try {
      List<Cinema> cinemas = cinemaService.list();
      return ResponseEntity.ok(cinemas);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * 获取影院详情
   */
  @GetMapping("/{id}")
  public ResponseEntity<Cinema> getCinemaDetail(@PathVariable Integer id) {
    try {
      Cinema cinema = cinemaService.getById(id);
      return cinema != null ? ResponseEntity.ok(cinema) : ResponseEntity.notFound().build();
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * 创建影院
   */
  @PostMapping
  public ResponseEntity<Cinema> createCinema(@RequestBody Cinema cinema) {
    try {
      cinemaService.save(cinema);
      return ResponseEntity.ok(cinema);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * 更新影院信息
   */
  @PutMapping("/{id}")
  public ResponseEntity<Cinema> updateCinema(@PathVariable Integer id, @RequestBody Cinema cinema) {
    try {
      cinema.setCinemaId(id);
      cinemaService.updateById(cinema);
      return ResponseEntity.ok(cinema);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * 删除影院
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCinema(@PathVariable Integer id) {
    try {
      cinemaService.removeById(id);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).build();
    }
  }

  /**
   * 分页查询影院列表
   */
  @GetMapping("/list")
  public ResponseEntity<Page<Cinema>> getCinemaList(
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) String name,
      @RequestParam(defaultValue = "cinema_id") String sortField,
      @RequestParam(defaultValue = "desc") String sortOrder) {
    try {
      Page<Cinema> page = new Page<>(current, size);
      QueryWrapper<Cinema> queryWrapper = new QueryWrapper<>();
      if (name != null && !name.trim().isEmpty()) {
        queryWrapper.like("name", name.trim());
      }
      // 排序
      if ("asc".equalsIgnoreCase(sortOrder)) {
        queryWrapper.orderByAsc(sortField);
      } else {
        queryWrapper.orderByDesc(sortField);
      }
      Page<Cinema> result = cinemaService.page(page, queryWrapper);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).build();
    }
  }
}
