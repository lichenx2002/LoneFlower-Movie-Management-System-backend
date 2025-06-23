// src/main/java/com/example/cinemabackend/controller/UserController.java
package com.example.cinemabackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.cinemabackend.dto.*;
import com.example.cinemabackend.entity.User;
import com.example.cinemabackend.mapper.UserMapper;
import com.example.cinemabackend.util.JwtUtil;
import com.example.cinemabackend.util.MD5Util;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            // 查找用户（支持手机号、用户名或邮箱登录）
            User user = userMapper.selectOne(
                    new QueryWrapper<User>()
                            .eq("phone", request.getLoginId())
                            .or()
                            .eq("username", request.getLoginId())
                            .or()
                            .eq("email", request.getLoginId()));

            if (user == null || !user.getPassword().equals(MD5Util.encrypt(request.getPassword()))) {
                return ResponseEntity.badRequest().build();
            }

            // 生成token
            String token = jwtUtil.generateToken(user.getUserId());

            // 转换为DTO
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);

            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUser(userDTO);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("登录失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody RegisterRequest request) {
        try {
            // 检查手机号是否已存在
            if (userMapper.selectCount(new QueryWrapper<User>().eq("phone", request.getPhone())) > 0) {
                return ResponseEntity.badRequest().build();
            }

            // 创建新用户
            User user = new User();
            user.setPhone(request.getPhone());
            user.setPassword(MD5Util.encrypt(request.getPassword()));
            user.setBalance(new BigDecimal("0.00"));
            user.setRegTime(LocalDateTime.now());

            userMapper.insert(user);

            // 转换为DTO
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);

            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            log.error("注册失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/update-profile")
    public ResponseEntity<UserDTO> updateProfile(@RequestHeader("Authorization") String token,
            @RequestBody UserDTO updateRequest) {
        try {
            // 验证token
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(401).build();
            }

            Integer userId = jwtUtil.getUserIdFromToken(token);
            User user = userMapper.selectById(userId);

            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            // 更新用户信息
            if (updateRequest.getUsername() != null) {
                // 检查用户名是否已被其他用户使用
                if (!updateRequest.getUsername().equals(user.getUsername()) &&
                        userMapper.selectCount(
                                new QueryWrapper<User>().eq("username", updateRequest.getUsername())) > 0) {
                    return ResponseEntity.badRequest().build();
                }
                user.setUsername(updateRequest.getUsername());
            }

            if (updateRequest.getEmail() != null) {
                // 检查邮箱是否已被其他用户使用
                if (!updateRequest.getEmail().equals(user.getEmail()) &&
                        userMapper.selectCount(new QueryWrapper<User>().eq("email", updateRequest.getEmail())) > 0) {
                    return ResponseEntity.badRequest().build();
                }
                user.setEmail(updateRequest.getEmail());
            }

            userMapper.updateById(user);

            // 转换为DTO
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);

            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            log.error("更新个人信息失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile(@RequestHeader("Authorization") String token) {
        try {
            // 验证token
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(401).build();
            }
            Integer userId = jwtUtil.getUserIdFromToken(token);
            User user = userMapper.selectById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据用户ID修改用户信息
     */
    @PutMapping("/update/{userId}")
    public ResponseEntity<UserDTO> updateUserById(@PathVariable Integer userId, @RequestBody UserDTO updateRequest) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            // 更新用户名，校验唯一性
            if (updateRequest.getUsername() != null) {
                if (!updateRequest.getUsername().equals(user.getUsername()) &&
                        userMapper.selectCount(
                                new QueryWrapper<User>().eq("username", updateRequest.getUsername()).ne("user_id",
                                        userId)) > 0) {
                    return ResponseEntity.badRequest().body(null);
                }
                user.setUsername(updateRequest.getUsername());
            }

            // 更新邮箱，校验唯一性
            if (updateRequest.getEmail() != null) {
                if (!updateRequest.getEmail().equals(user.getEmail()) &&
                        userMapper.selectCount(
                                new QueryWrapper<User>().eq("email", updateRequest.getEmail()).ne("user_id",
                                        userId)) > 0) {
                    return ResponseEntity.badRequest().body(null);
                }
                user.setEmail(updateRequest.getEmail());
            }

            userMapper.updateById(user);

            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            log.error("根据ID更新用户信息失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 获取所有用户信息（不返回密码）
     */
    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            List<User> users = userMapper.selectList(null);
            List<UserDTO> userDTOs = users.stream().map(user -> {
                UserDTO dto = new UserDTO();
                BeanUtils.copyProperties(user, dto);
                return dto;
            }).toList();
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            log.error("获取所有用户信息失败", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 管理员修改用户信息（可选改密码）
     */
    @PutMapping("/admin-update/{userId}")
    public ResponseEntity<UserDTO> adminUpdateUserById(@PathVariable Integer userId,
            @RequestBody UserDTO updateRequest) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            // 更新用户名，校验唯一性
            if (updateRequest.getUsername() != null) {
                if (!updateRequest.getUsername().equals(user.getUsername()) &&
                        userMapper.selectCount(
                                new QueryWrapper<User>().eq("username", updateRequest.getUsername()).ne("user_id",
                                        userId)) > 0) {
                    return ResponseEntity.badRequest().body(null);
                }
                user.setUsername(updateRequest.getUsername());
            }
            // 更新邮箱，校验唯一性
            if (updateRequest.getEmail() != null) {
                if (!updateRequest.getEmail().equals(user.getEmail()) &&
                        userMapper.selectCount(
                                new QueryWrapper<User>().eq("email", updateRequest.getEmail()).ne("user_id",
                                        userId)) > 0) {
                    return ResponseEntity.badRequest().body(null);
                }
                user.setEmail(updateRequest.getEmail());
            }
            // 更新手机号
            if (updateRequest.getPhone() != null) {
                user.setPhone(updateRequest.getPhone());
            }
            // 更新余额
            if (updateRequest.getBalance() != null) {
                user.setBalance(updateRequest.getBalance());
            }
            // 更新密码（如有）
            if (updateRequest.getPassword() != null && !updateRequest.getPassword().isEmpty()) {
                user.setPassword(MD5Util.encrypt(updateRequest.getPassword()));
            }
            userMapper.updateById(user);
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            log.error("管理员修改用户信息失败", e);
            return ResponseEntity.status(500).build();
        }
    }
}