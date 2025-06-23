package com.example.cinemabackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.cinemabackend.entity.Movie;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
@Mapper
public interface MovieMapper extends BaseMapper<Movie> {

}
