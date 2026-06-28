package com.livehouse.vo;

import lombok.Data;

/**
 * 登录响应VO
 */
@Data
public class LoginVO {

    private Long userId;

    private String username;

    private String realName;

    private String phone;

    private String email;

    private String avatar;

    private Long roleId;

    private String roleName;

    private String token;
}