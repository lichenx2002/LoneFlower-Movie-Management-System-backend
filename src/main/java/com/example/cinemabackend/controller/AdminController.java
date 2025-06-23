package com.example.cinemabackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.cinemabackend.dto.AdminDTO;
import com.example.cinemabackend.dto.LoginRequest;
import com.example.cinemabackend.dto.LoginResponse;
import com.example.cinemabackend.entity.Admin;
import com.example.cinemabackend.mapper.AdminMapper;
import com.example.cinemabackend.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author author
 * @since 2025-06-14
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@CrossOrigin
public class AdminController {

  @Autowired
  private AdminMapper adminMapper;

  @Autowired
  private JwtUtil jwtUtil;

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    try {
      Admin admin = adminMapper.selectOne(
          new QueryWrapper<Admin>()
              .eq("username", request.getLoginId()));

      String encryptedPassword = com.example.cinemabackend.util.MD5Util.encrypt(request.getPassword());
      if (admin == null || !admin.getPassword().equals(encryptedPassword)) {
        return ResponseEntity.badRequest().build();
      }

      // 生成token（可用adminId）
      String token = jwtUtil.generateToken(admin.getAdminId());

      AdminDTO adminDTO = new AdminDTO();
      BeanUtils.copyProperties(admin, adminDTO);

      LoginResponse response = new LoginResponse();
      response.setToken(token);
      response.setAdmin(adminDTO);

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("管理员登录失败", e);
      return ResponseEntity.status(500).build();
    }
  }

  @PostMapping("/register")
  public ResponseEntity<AdminDTO> register(@RequestBody AdminDTO request) {
    try {
      // 检查用户名是否已存在
      if (adminMapper.selectCount(new QueryWrapper<Admin>().eq("username", request.getUsername())) > 0) {
        return ResponseEntity.badRequest().build();
      }

      Admin admin = new Admin();
      admin.setUsername(request.getUsername());
      admin.setPassword(com.example.cinemabackend.util.MD5Util.encrypt(request.getPassword()));
      admin.setName(request.getName());
      admin.setLastLogin(null);

      adminMapper.insert(admin);

      AdminDTO adminDTO = new AdminDTO();
      BeanUtils.copyProperties(admin, adminDTO);

      return ResponseEntity.ok(adminDTO);
    } catch (Exception e) {
      log.error("管理员注册失败", e);
      return ResponseEntity.status(500).build();
    }
  }
}
