package com.example.cinemabackend.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.math.BigInteger;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 电影实体类
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("movie")
public class Movie implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "movie_id", type = IdType.AUTO)
    private Integer movieId;

    private String title;

    private String englishTitle;

    private String director;

    private String genres;

    private String actors;

    private Integer duration;

    private LocalDate releaseDate;

    private String releaseLocation;

    private String posterUrl;

    private String trailerUrl;

    private String description;

    private BigDecimal avgRating;

    private BigInteger boxOffice;

    private Integer wantToWatch;

    private MovieStatus status;

    public enum MovieStatus {
        ON_SHELF("上映"),      // 正在上映，可以购票
        OFF_SHELF("下架"),     // 已下架，不能购票
        COMING_SOON("即将上映"); // 即将上映，可以预约

        private final String description;

        MovieStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

}
