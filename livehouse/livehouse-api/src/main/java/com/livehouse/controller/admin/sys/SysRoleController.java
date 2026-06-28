package com.livehouse.controller.admin.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.livehouse.common.constants.SystemConstants;
import com.livehouse.common.result.Result;
import com.livehouse.dto.RoleMenuAssignDTO;
import com.livehouse.entity.SysMenu;
import com.livehouse.entity.SysRole;
import com.livehouse.entity.SysRoleMenu;
import com.livehouse.mapper.SysMenuMapper;
import com.livehouse.mapper.SysRoleMapper;
import com.livehouse.mapper.SysRoleMenuMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/system/role")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色权限相关接口")
public class SysRoleController {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;

    /**
     * 获取角色列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取角色列表", description = "获取角色列表，支持关键词搜索")
    public Result<List<SysRole>> getRoleList(
            @Parameter(description = "搜索关键词")
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDeleted, 0);
        
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like(SysRole::getRoleName, keyword)
                    .or()
                    .like(SysRole::getRoleCode, keyword));
        }
        
        List<SysRole> roles = roleMapper.selectList(queryWrapper);
        return Result.success(roles);
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取角色详情", description = "根据ID获取角色详细信息")
    public Result<SysRole> getRoleById(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long id) {
        SysRole role = roleMapper.selectById(id);
        return Result.success(role);
    }

    /**
     * 获取角色已分配的菜单ID列表
     */
    @GetMapping("/{id}/menus")
    @Operation(summary = "获取角色菜单", description = "获取角色已分配的菜单ID列表")
    public Result<List<Long>> getRoleMenus(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long id) {
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, id)
        );
        List<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
        return Result.success(menuIds);
    }

    /**
     * 分配菜单给角色
     */
    @PutMapping("/authMenu")
    @Operation(summary = "分配菜单", description = "为角色分配菜单权限")
    public Result<Object> assignMenu(
            @Parameter(description = "角色菜单分配信息", required = true)
            @Valid @RequestBody RoleMenuAssignDTO dto) {
        log.info("为角色 {} 分配菜单", dto.getRoleId());

        // 禁止修改超级管理员角色的菜单权限
        if (dto.getRoleId().equals(SystemConstants.SUPER_ADMIN_ROLE_ID)) {
            return Result.fail("超级管理员角色不允许修改菜单权限");
        }

        // 删除原有关联
        roleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, dto.getRoleId())
        );

        // 批量插入新关联
        if (dto.getMenuIds() != null && !dto.getMenuIds().isEmpty()) {
            List<SysRoleMenu> list = dto.getMenuIds().stream()
                    .map(menuId -> {
                        SysRoleMenu roleMenu = new SysRoleMenu();
                        roleMenu.setRoleId(dto.getRoleId());
                        roleMenu.setMenuId(menuId);
                        return roleMenu;
                    })
                    .collect(Collectors.toList());
            list.forEach(roleMenuMapper::insert);
        }

        return Result.success("分配成功");
    }

    /**
     * 获取角色可访问的菜单树
     */
    @GetMapping("/{id}/menuTree")
    @Operation(summary = "获取角色菜单树", description = "获取角色可访问的菜单树结构")
    public Result<List<SysMenu>> getRoleMenuTree(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long id) {
        // 获取角色已分配的菜单ID
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, id)
        );
        List<Long> assignedMenuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());

        // 获取所有菜单
        List<SysMenu> allMenus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getDeleted, 0)
        );

        // 过滤出已分配的菜单
        List<SysMenu> assignedMenus = allMenus.stream()
                .filter(menu -> assignedMenuIds.contains(menu.getId()))
                .collect(Collectors.toList());

        // 构建树形结构
        List<SysMenu> tree = buildMenuTree(assignedMenus, 0L);

        return Result.success(tree);
    }

    /**
     * 构建菜单树
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> parentId.equals(menu.getParentId()))
                .peek(menu -> menu.setChildren(buildMenuTree(menus, menu.getId())))
                .collect(Collectors.toList());
    }

    /**
     * 新增角色
     */
    @PostMapping
    @Operation(summary = "新增角色", description = "创建新角色")
    public Result<Object> addRole(
            @Parameter(description = "角色信息", required = true)
            @RequestBody SysRole role) {
        roleMapper.insert(role);
        return Result.success("新增成功");
    }

    /**
     * 更新角色
     */
    @PutMapping
    @Operation(summary = "更新角色", description = "更新角色信息")
    public Result<Object> updateRole(
            @Parameter(description = "角色信息", required = true)
            @RequestBody SysRole role) {
        // 禁止修改超级管理员角色
        if (role.getId() != null && role.getId().equals(SystemConstants.SUPER_ADMIN_ROLE_ID)) {
            return Result.fail("超级管理员角色不允许修改");
        }
        roleMapper.updateById(role);
        return Result.success("更新成功");
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色", description = "根据ID删除角色")
    public Result<Object> deleteRole(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long id) {
        // 禁止删除超级管理员角色
        if (id.equals(SystemConstants.SUPER_ADMIN_ROLE_ID)) {
            return Result.fail("超级管理员角色不允许删除");
        }
        // 删除角色菜单关联
        roleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, id)
        );
        // 删除角色
        roleMapper.deleteById(id);
        return Result.success("删除成功");
    }
}