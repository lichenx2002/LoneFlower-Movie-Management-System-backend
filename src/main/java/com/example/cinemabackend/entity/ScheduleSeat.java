package com.example.cinemabackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * <p>
 * 场次座位状态表 - 用于展示场次座位完整信息
 * 包含：场次信息、影厅信息、电影信息、座位信息、座位状态
 * </p>
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("schedule_seat")
public class ScheduleSeat implements Serializable {

    private static final long serialVersionUID = 1L;

    // 场次座位状态表主键
    @TableId(value = "ss_id", type = IdType.AUTO)
    private Long ssId;

    // 关联场次ID
    private Integer scheduleId;

    // 关联座位模板ID
    private Integer templateId;

    // 座位状态：AVAILABLE-可选, OCCUPIED-已售出, LOCKED-已锁定
    private String status;

    // 锁定时间
    private LocalDateTime lockTime;

    // 锁定用户ID
    private Integer userId;

    // 计算得到的座位价格
    @TableField(exist = false)
    private BigDecimal price;

    // ===== 关联信息 =====

    // 场次信息
    @TableField(exist = false)
    private Schedule schedule;

    // 影厅信息
    @TableField(exist = false)
    private Hall hall;

    // 电影信息
    @TableField(exist = false)
    private Movie movie;

    // 座位模板信息
    @TableField(exist = false)
    private SeatTemplate seatTemplate;

    // 锁定用户信息
    @TableField(exist = false)
    private User user;

    // ===== 座位位置信息（从seatTemplate中获取） =====
    @TableField(exist = false)
    private String rowLabel; // 排号（A-Z）

    @TableField(exist = false)
    private Integer colNum; // 座位号

    @TableField(exist = false)
    private String seatType; // 座位类型：NORMAL, LOVER_LEFT, LOVER_RIGHT, VIP
}
