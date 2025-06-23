package com.example.cinemabackend.service.impl;

import com.example.cinemabackend.entity.Comment;
import com.example.cinemabackend.mapper.CommentMapper;
import com.example.cinemabackend.service.ICommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

}
