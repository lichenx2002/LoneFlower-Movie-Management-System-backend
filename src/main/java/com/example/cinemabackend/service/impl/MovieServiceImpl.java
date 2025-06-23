package com.example.cinemabackend.service.impl;

import com.example.cinemabackend.entity.Movie;
import com.example.cinemabackend.mapper.MovieMapper;
import com.example.cinemabackend.service.IMovieService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
@Service
public class MovieServiceImpl extends ServiceImpl<MovieMapper, Movie> implements IMovieService {

}
