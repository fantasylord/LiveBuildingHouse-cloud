package com.livehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livehouse.common.exception.BusinessException;
import com.livehouse.dto.LoginDTO;
import com.livehouse.entity.SysRole;
import com.livehouse.entity.SysUser;
import com.livehouse.mapper.SysRoleMapper;
import com.livehouse.mapper.SysUserMapper;
import com.livehouse.service.SysUserService;
import com.livehouse.util.JwtUtil;
import com.livehouse.vo.LoginVO;
import com.livehouse.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final SysRoleMapper roleMapper;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 查询用户
        SysUser user = baseMapper.selectOne(
            new QueryWrapper<SysUser>()
                .eq("username", loginDTO.getUsername())
                .eq("deleted", 0)
        );

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证状态
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已禁用");
        }

        // 获取角色信息
        SysRole role = roleMapper.selectById(user.getRoleId());

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 组装返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setPhone(user.getPhone());
        loginVO.setEmail(user.getEmail());
        loginVO.setAvatar(user.getAvatar());
        loginVO.setRoleId(user.getRoleId());
        loginVO.setRoleName(role != null ? role.getRoleName() : "");
        loginVO.setToken(token);

        return loginVO;
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        SysUser user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        SysRole role = roleMapper.selectById(user.getRoleId());

        return UserVO.fromEntity(user, role);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    public Long getUserIdFromToken(String token) {
        return jwtUtil.getUserId(token);
    }

    @Override
    public boolean save(SysUser user) {
        // 设置默认密码为 123456
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode("123456"));
        }
        // 设置默认状态为启用
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        return super.save(user);
    }

    @Override
    public void resetPassword(Long userId) {
        SysUser user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 重置为默认密码 123456
        String defaultPassword = passwordEncoder.encode("123456");
        user.setPassword(defaultPassword);
        baseMapper.updateById(user);
        log.info("用户 {} 的密码已重置", user.getUsername());
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        // 从当前登录用户获取用户ID（需要通过ThreadLocal或请求上下文获取）
        // 这里暂时抛出异常，实际需要从安全上下文获取
        throw new BusinessException("该功能需要完善安全上下文");
    }
}