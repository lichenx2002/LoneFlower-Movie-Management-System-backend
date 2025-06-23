package com.example.cinemabackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 影厅表
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("hall")
public class Hall implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "hall_id", type = IdType.AUTO)
    private Integer hallId;

    private String name;

    private String type;

    private Integer rowCount;

    private Integer colCount;

    private String rowLabels;

    private Integer cinemaId;
}
