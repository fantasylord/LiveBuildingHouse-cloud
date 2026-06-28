package com.livehouse.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 角色菜单分配请求DTO
 */
@Data
public class RoleMenuAssignDTO {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    private List<Long> menuIds;
}