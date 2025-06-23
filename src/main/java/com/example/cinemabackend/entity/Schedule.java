package com.example.cinemabackend.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * <p>
 * 
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("schedule")
public class Schedule implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer scheduleId;

    private Integer movieId;

    private Integer hallId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal basePrice;

    private BigDecimal vipPrice;

    private BigDecimal loverPrice;


    @TableField(exist = false)
    private Movie movie;

    @TableField(exist = false)
    private Hall hall;
}
