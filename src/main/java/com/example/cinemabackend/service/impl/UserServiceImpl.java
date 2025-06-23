package com.example.cinemabackend.service.impl;

import com.example.cinemabackend.entity.User;
import com.example.cinemabackend.mapper.UserMapper;
import com.example.cinemabackend.service.IUserService;
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
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
