package com.example.cinemabackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.cinemabackend.entity.SeatTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
@Mapper
public interface SeatTemplateMapper extends BaseMapper<SeatTemplate> {

  @Select("SELECT * FROM seat_template WHERE hall_id = #{hallId}")
  List<SeatTemplate> selectByHallId(Integer hallId);
}
