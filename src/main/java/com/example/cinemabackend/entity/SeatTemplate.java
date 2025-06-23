package com.example.cinemabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@TableName("seat_template")
public class SeatTemplate {

    @TableId(value = "template_id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "hall_id")
    private Integer hallId;

    @TableField(value = "row_label")
    private String rowLabel;

    @TableField(value = "col_num")
    private Integer colNum;

    @TableField(value = "seat_type")
    private SeatType seatType;

    public enum SeatType {
        NORMAL,
        LOVER_LEFT,
        LOVER_RIGHT,
        VIP
    }

    @Override
    public String toString() {
        return "SeatTemplate{" +
                "id=" + id +
                ", hallId=" + hallId +
                ", rowLabel='" + rowLabel + '\'' +
                ", colNum=" + colNum +
                ", seatType=" + seatType +
                '}';
    }
}
