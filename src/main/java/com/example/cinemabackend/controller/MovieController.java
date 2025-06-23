package com.example.cinemabackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.cinemabackend.entity.Movie;
import com.example.cinemabackend.entity.Schedule;
import com.example.cinemabackend.service.IMovieService;
import com.example.cinemabackend.service.IScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 电影管理控制器
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
@RestController
@RequestMapping("/movie")
public class MovieController {

  @Autowired
  private IMovieService movieService;

  @Autowired
  private IScheduleService scheduleService;

  /**
   * 添加新电影
   */
  @PostMapping
  public ResponseEntity<Movie> addMovie(@RequestBody Movie movie) {
    movieService.save(movie);
    return ResponseEntity.ok(movie);
  }

  /**
   * 根据ID删除电影
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMovie(@PathVariable Integer id) {
    movieService.removeById(id);
    return ResponseEntity.ok().build();
  }

  /**
   * 更新电影信息
   */
  @PutMapping("/{id}")
  public ResponseEntity<Movie> updateMovie(@PathVariable Integer id, @RequestBody Movie movie) {
    movie.setMovieId(id);
    movieService.updateById(movie);
    return ResponseEntity.ok(movie);
  }

  /**
   * 根据ID查询电影
   */
  @GetMapping("/{id}")
  public ResponseEntity<Movie> getMovie(@PathVariable Integer id) {
    Movie movie = movieService.getById(id);
    return movie != null ? ResponseEntity.ok(movie) : ResponseEntity.notFound().build();
  }

  /**
   * 分页查询电影列表
   */
  @GetMapping("/list")
  public ResponseEntity<Page<Movie>> listMovies(
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) String title) {

    Page<Movie> page = new Page<>(current, size);
    QueryWrapper<Movie> queryWrapper = new QueryWrapper<>();

    if (title != null && !title.isEmpty()) {
      queryWrapper.like("title", title);
    }

    Page<Movie> result = movieService.page(page, queryWrapper);
    return ResponseEntity.ok(result);
  }

  /**
   * 获取所有电影
   */
  @GetMapping("/all")
  public ResponseEntity<List<Movie>> getAllMovies() {
    List<Movie> movies = movieService.list();
    return ResponseEntity.ok(movies);
  }

  /**
   * 获取电影场次信息
   */
  @GetMapping("/{id}/schedules")
  public ResponseEntity<Map<String, Object>> getMovieSchedules(@PathVariable Integer id) {
    Map<String, Object> result = scheduleService.getMovieSchedules(id);
    return ResponseEntity.ok(result);
  }

  /**
   * 获取上映中的电影
   */
  @GetMapping("/on-shelf")
  public ResponseEntity<List<Movie>> getOnShelfMovies() {
    List<Movie> movies = movieService.lambdaQuery()
        .eq(Movie::getStatus, Movie.MovieStatus.ON_SHELF)
        .list();
    return ResponseEntity.ok(movies);
  }

  /**
   * 获取即将上映的电影
   */
  @GetMapping("/coming-soon")
  public ResponseEntity<List<Movie>> getComingSoonMovies() {
    List<Movie> movies = movieService.lambdaQuery()
        .eq(Movie::getStatus, Movie.MovieStatus.COMING_SOON)
        .list();
    return ResponseEntity.ok(movies);
  }
}
