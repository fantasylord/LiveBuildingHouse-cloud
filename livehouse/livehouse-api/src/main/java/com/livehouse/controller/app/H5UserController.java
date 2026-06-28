package com.livehouse.controller.app;

import com.livehouse.common.result.Result;
import com.livehouse.entity.AppUser;
import com.livehouse.service.AppUserService;
import com.livehouse.util.JwtUtil;
import com.livehouse.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/h5/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class H5UserController {

    private final AppUserService appUserService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String password = params.get("password");

        AppUser user = appUserService.lambdaQuery()
                .eq(AppUser::getPhone, phone)
                .eq(AppUser::getStatus, 1)
                .one();

        if (user == null) {
            return Result.error("用户不存在");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error("密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        appUserService.updateById(user);

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setPhone(user.getPhone());
        loginVO.setAvatar(user.getAvatar());

        return Result.success(loginVO);
    }

    @PostMapping("/register")
    public Result<LoginVO> register(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String password = params.get("password");
        String username = params.get("username");
        String realName = params.get("realName");

        // 检查手机号是否已注册
        boolean exists = appUserService.lambdaQuery()
                .eq(AppUser::getPhone, phone)
                .exists();

        if (exists) {
            return Result.error("该手机号已注册");
        }

        AppUser user = new AppUser();
        user.setUsername(username != null ? username : phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setRealName(realName != null ? realName : "用户");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDeleted(0);

        boolean success = appUserService.save(user);
        if (!success) {
            return Result.error("注册失败");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setPhone(user.getPhone());
        loginVO.setAvatar(user.getAvatar());

        return Result.success(loginVO);
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.error(401, "未登录");
        }

        try {
            token = token.substring(7);
            Map<String, Object> claims = jwtUtil.parseToken(token);
            if (claims == null) {
                return Result.error(401, "登录已过期");
            }

            Long userId = Long.valueOf(claims.get("userId").toString());
            AppUser user = appUserService.getById(userId);
            if (user == null) {
                return Result.error(401, "用户不存在");
            }

            Map<String, Object> info = new HashMap<>();
            info.put("userId", user.getId());
            info.put("username", user.getUsername());
            info.put("realName", user.getRealName());
            info.put("phone", user.getPhone());
            info.put("email", user.getEmail());
            info.put("avatar", user.getAvatar());

            return Result.success(info);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error(401, "登录已过期");
        }
    }

    @PutMapping("/info")
    public Result<Map<String, Object>> updateUserInfo(@RequestHeader(value = "Authorization", required = false) String token,
                                                      @RequestBody Map<String, String> params) {
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.error(401, "未登录");
        }

        try {
            Map<String, Object> claims = jwtUtil.parseToken(token.substring(7));
            if (claims == null) {
                return Result.error(401, "登录已过期");
            }

            Long userId = Long.valueOf(claims.get("userId").toString());
            AppUser user = appUserService.getById(userId);
            if (user == null) {
                return Result.error(401, "用户不存在");
            }

            String realName = params.get("realName");
            String phone = params.get("phone");
            String email = params.get("email");
            String avatar = params.get("avatar");
            String city = params.get("city");
            String province = params.get("province");
            if (realName != null) {
                user.setRealName(realName);
            }
            if (phone != null) {
                user.setPhone(phone);
            }
            if (email != null) {
                user.setEmail(email);
            }
            if (avatar != null) {
                user.setAvatar(avatar);
            }
            if (city != null) {
                user.setCity(city);
            }
            if (province != null) {
                user.setProvince(province);
            }
            user.setUpdateTime(LocalDateTime.now());
            appUserService.updateById(user);

            Map<String, Object> info = new HashMap<>();
            info.put("userId", user.getId());
            info.put("username", user.getUsername());
            info.put("realName", user.getRealName());
            info.put("phone", user.getPhone());
            info.put("email", user.getEmail());
            info.put("avatar", user.getAvatar());
            info.put("city", user.getCity());
            info.put("province", user.getProvince());
            return Result.success(info);
        } catch (Exception e) {
            log.error("Update H5 user info failed", e);
            return Result.error(401, "登录已过期");
        }
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @PostMapping("/sendCode")
    public Result<Void> sendCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        // TODO: 集成短信发送服务
        log.info("发送验证码到手机号: {}", phone);
        return Result.success();
    }
}
