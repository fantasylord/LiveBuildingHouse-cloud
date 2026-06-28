package com.livehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.livehouse.dto.LoginDTO;
import com.livehouse.entity.SysUser;
import com.livehouse.vo.LoginVO;
import com.livehouse.vo.UserVO;

/**
 * 用户服务接口
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 获取用户信息
     */
    UserVO getUserInfo(Long userId);

    /**
     * 验证token
     */
    boolean validateToken(String token);

    /**
     * 获取用户ID从token
     */
    Long getUserIdFromToken(String token);

    /**
     * 重置用户密码为默认密码
     */
    void resetPassword(Long userId);

    /**
     * 修改当前用户密码
     */
    void changePassword(String oldPassword, String newPassword);
}