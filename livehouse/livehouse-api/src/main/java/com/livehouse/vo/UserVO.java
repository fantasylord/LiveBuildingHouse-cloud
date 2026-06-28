package com.livehouse.vo;

import com.livehouse.entity.SysRole;
import com.livehouse.entity.SysUser;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息VO
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String realName;

    private String phone;

    private String email;

    private String avatar;

    private Long roleId;

    private String roleName;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static UserVO fromEntity(SysUser user, SysRole role) {
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setRealName(user.getRealName() != null ? user.getRealName() : "");
        userVO.setPhone(user.getPhone() != null ? user.getPhone() : "");
        userVO.setEmail(user.getEmail() != null ? user.getEmail() : "");
        userVO.setAvatar(user.getAvatar() != null ? user.getAvatar() : "");
        userVO.setRoleId(user.getRoleId());
        userVO.setRoleName(role != null && role.getRoleName() != null ? role.getRoleName() : "");
        userVO.setStatus(user.getStatus());
        userVO.setLastLoginTime(user.getLastLoginTime());
        userVO.setLastLoginIp(user.getLastLoginIp() != null ? user.getLastLoginIp() : "");
        userVO.setRemark(user.getRemark() != null ? user.getRemark() : "");
        userVO.setCreateTime(user.getCreateTime());
        userVO.setUpdateTime(user.getUpdateTime());
        return userVO;
    }
}