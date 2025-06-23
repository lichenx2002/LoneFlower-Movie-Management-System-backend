package com.example.cinemabackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.cinemabackend.entity.Comment;
import com.example.cinemabackend.service.ICommentService;
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
 * @since 2025-06-14
 */
@RestController
@RequestMapping("/comment")
@CrossOrigin
public class CommentController {
  @Autowired
  private ICommentService commentService;

  // 发布评论
  @PostMapping("/add")
  public ResponseEntity<Comment> addComment(@RequestBody Comment comment) {
    commentService.save(comment);
    return ResponseEntity.ok(comment);
  }

  // 获取所有评论（可选：根据电影ID筛选）
  @GetMapping("/all")
  public ResponseEntity<List<Comment>> getAllComments(
      @RequestParam(value = "movieId", required = false) Integer movieId) {
    List<Comment> comments;
    if (movieId != null) {
      comments = commentService.lambdaQuery().eq(Comment::getMovieId, movieId).list();
    } else {
      comments = commentService.list();
    }
    return ResponseEntity.ok(comments);
  }

  // 管理员专用：分页查询评论列表
  @GetMapping("/list")
  public ResponseEntity<Page<Comment>> getCommentList(
      @RequestParam(defaultValue = "1") Integer current,
      @RequestParam(defaultValue = "10") Integer size,
      @RequestParam(required = false) String content,
      @RequestParam(required = false) Integer movieId,
      @RequestParam(required = false) Integer userId,
      @RequestParam(defaultValue = "comment_id") String sortField,
      @RequestParam(defaultValue = "desc") String sortOrder) {
    try {
      Page<Comment> page = new Page<>(current, size);

      QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();

      // 搜索条件
      if (content != null && !content.trim().isEmpty()) {
        queryWrapper.like("content", content.trim());
      }
      if (movieId != null) {
        queryWrapper.eq("movie_id", movieId);
      }
      if (userId != null) {
        queryWrapper.eq("user_id", userId);
      }

      // 字段名映射（前端字段名 -> 数据库字段名）
      String dbSortField = sortField;
      if ("commentId".equals(sortField)) {
        dbSortField = "comment_id";
      }

      // 排序
      if ("asc".equalsIgnoreCase(sortOrder)) {
        queryWrapper.orderByAsc(dbSortField);
      } else {
        queryWrapper.orderByDesc(dbSortField);
      }

      Page<Comment> result = commentService.page(page, queryWrapper);

      // 确保total字段正确设置
      if (result.getTotal() == 0 && result.getRecords().size() > 0) {
        // 如果total为0但有数据，手动计算总数
        QueryWrapper<Comment> countWrapper = new QueryWrapper<>();
        if (content != null && !content.trim().isEmpty()) {
          countWrapper.like("content", content.trim());
        }
        if (movieId != null) {
          countWrapper.eq("movie_id", movieId);
        }
        if (userId != null) {
          countWrapper.eq("user_id", userId);
        }
        long total = commentService.count(countWrapper);
        result.setTotal(total);
      }

      return ResponseEntity.ok(result);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(500).build();
    }
  }

  // 删除评论
  @DeleteMapping("/{commentId}")
  public ResponseEntity<?> deleteComment(@PathVariable Integer commentId) {
    boolean removed = commentService.removeById(commentId);
    if (removed) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
