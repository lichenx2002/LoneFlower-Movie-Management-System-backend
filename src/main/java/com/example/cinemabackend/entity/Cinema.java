package com.example.cinemabackend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author author
 * @since 2025-06-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("cinema")
public class Cinema implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "cinema_id", type = IdType.AUTO)
    private Integer cinemaId;

    private String name;

    private String address;

    private String phone;

    private String description;


}
