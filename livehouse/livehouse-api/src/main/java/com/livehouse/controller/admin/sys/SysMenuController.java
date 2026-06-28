package com.livehouse.controller.admin.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.livehouse.common.constants.SystemConstants;
import com.livehouse.common.result.Result;
import com.livehouse.entity.SysMenu;
import com.livehouse.entity.SysRoleMenu;
import com.livehouse.entity.SysUser;
import com.livehouse.mapper.SysMenuMapper;
import com.livehouse.mapper.SysRoleMenuMapper;
import com.livehouse.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/system/menu")
@RequiredArgsConstructor
@Tag(name = "菜单管理", description = "菜单权限相关接口")
public class SysMenuController {

    private final SysMenuMapper menuMapper;
    private final SysUserMapper userMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    /**
     * 获取菜单列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取菜单列表", description = "获取所有菜单列表")
    public Result<List<SysMenu>> getMenuList(
            @Parameter(description = "搜索关键词")
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getDeleted, SystemConstants.DELETED_NO)
                .orderByAsc(SysMenu::getParentId, SysMenu::getSortOrder);
        
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(SysMenu::getMenuName, keyword);
        }
        
        List<SysMenu> menus = menuMapper.selectList(queryWrapper);
        return Result.success(menus);
    }

    /**
     * 获取菜单树
     */
    @GetMapping("/tree")
    @Operation(summary = "获取菜单树", description = "获取树形结构的菜单列表")
    public Result<List<SysMenu>> getMenuTree() {
        List<SysMenu> menus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getDeleted, SystemConstants.DELETED_NO)
                        .orderByAsc(SysMenu::getSortOrder)
        );
        List<SysMenu> tree = buildMenuTree(menus, SystemConstants.ROOT_MENU_PARENT_ID);
        return Result.success(tree);
    }

    /**
     * 根据用户ID获取菜单
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户菜单", description = "根据用户ID获取其可访问的菜单树")
    public Result<List<SysMenu>> getMenusByUserId(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        // 获取用户信息
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return Result.success(Collections.emptyList());
        }

        // 超级管理员默认拥有所有菜单权限（白名单）
        if (userId.equals(SystemConstants.SUPER_ADMIN_USER_ID) || user.getRoleId().equals(SystemConstants.SUPER_ADMIN_ROLE_ID)) {
            List<SysMenu> allMenus = menuMapper.selectList(
                    new LambdaQueryWrapper<SysMenu>()
                            .eq(SysMenu::getDeleted, SystemConstants.DELETED_NO)
                            .eq(SysMenu::getVisible, SystemConstants.MENU_VISIBLE)
                            .orderByAsc(SysMenu::getParentId, SysMenu::getSortOrder)
            );
            List<SysMenu> tree = buildMenuTree(allMenus, SystemConstants.ROOT_MENU_PARENT_ID);
            return Result.success(tree);
        }

        // 获取角色关联的菜单
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, user.getRoleId())
        );
        List<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());

        if (menuIds.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        // 获取所有可见菜单
        List<SysMenu> allMenus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getDeleted, SystemConstants.DELETED_NO)
                        .eq(SysMenu::getVisible, SystemConstants.MENU_VISIBLE)
                        .orderByAsc(SysMenu::getParentId, SysMenu::getSortOrder)
        );

        // 收集用户分配的菜单及其所有父菜单
        Set<Long> neededMenuIds = new HashSet<>(menuIds);
        Map<Long, SysMenu> menuMap = allMenus.stream()
                .collect(Collectors.toMap(SysMenu::getId, m -> m));
        
        // 递归添加父菜单
        for (Long menuId : new ArrayList<>(neededMenuIds)) {
            SysMenu menu = menuMap.get(menuId);
            while (menu != null && menu.getParentId() != null && !menu.getParentId().equals(SystemConstants.ROOT_MENU_PARENT_ID)) {
                neededMenuIds.add(menu.getParentId());
                menu = menuMap.get(menu.getParentId());
            }
        }

        List<SysMenu> userMenus = allMenus.stream()
                .filter(menu -> neededMenuIds.contains(menu.getId()))
                .collect(Collectors.toList());

        // 构建菜单树
        List<SysMenu> tree = buildMenuTree(userMenus, SystemConstants.ROOT_MENU_PARENT_ID);

        return Result.success(tree);
    }

    /**
     * 获取菜单详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取菜单详情", description = "根据ID获取菜单详细信息")
    public Result<SysMenu> getMenuById(
            @Parameter(description = "菜单ID", required = true)
            @PathVariable Long id) {
        SysMenu menu = menuMapper.selectById(id);
        return Result.success(menu);
    }

    /**
     * 新增菜单
     */
    @PostMapping
    @Operation(summary = "新增菜单", description = "创建新菜单")
    public Result<Object> addMenu(
            @Parameter(description = "菜单信息", required = true)
            @RequestBody SysMenu menu) {
        menuMapper.insert(menu);
        return Result.success("新增成功");
    }

    /**
     * 更新菜单
     */
    @PutMapping
    @Operation(summary = "更新菜单", description = "更新菜单信息")
    public Result<Object> updateMenu(
            @Parameter(description = "菜单信息", required = true)
            @RequestBody SysMenu menu) {
        menuMapper.updateById(menu);
        return Result.success("更新成功");
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单", description = "根据ID删除菜单")
    public Result<Object> deleteMenu(
            @Parameter(description = "菜单ID", required = true)
            @PathVariable Long id) {
        // 检查是否有子菜单
        Long count = menuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getParentId, id)
                        .eq(SysMenu::getDeleted, SystemConstants.DELETED_NO)
        );
        if (count > 0) {
            return Result.fail("请先删除子菜单");
        }
        menuMapper.deleteById(id);
        return Result.success("删除成功");
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
}
