package com.livehouse.controller.admin.sys;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.constants.SystemConstants;
import com.livehouse.common.result.Result;
import com.livehouse.dto.LoginDTO;
import com.livehouse.dto.UserPageRequest;
import com.livehouse.entity.SysRole;
import com.livehouse.entity.SysUser;
import com.livehouse.mapper.SysRoleMapper;
import com.livehouse.service.SysUserService;
import com.livehouse.util.PageUtil;
import com.livehouse.vo.LoginVO;
import com.livehouse.vo.PageResponse;
import com.livehouse.vo.UserPageResponse;
import com.livehouse.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/system/user")
@RequiredArgsConstructor
@Tag(name = "系统用户管理", description = "系统用户相关接口")
public class SysUserController {

    private final SysUserService sysUserService;
    private final SysRoleMapper sysRoleMapper;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录，返回JWT Token")
    public Result<LoginVO> login(
            @Parameter(description = "登录信息", required = true)
            @Valid @RequestBody LoginDTO loginDTO) {
        log.info("用户登录: {}", loginDTO.getUsername());
        LoginVO loginVO = sysUserService.login(loginDTO);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息", description = "根据Token获取当前登录用户信息")
    public Result<UserVO> getUserInfo(
            @Parameter(description = "JWT Token", required = true)
            @RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        Long userId = sysUserService.getUserIdFromToken(jwtToken);
        UserVO userVO = sysUserService.getUserInfo(userId);
        return Result.success(userVO);
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户列表", description = "获取所有用户列表")
    public Result<List<SysUser>> getUserList() {
        return Result.success(sysUserService.list());
    }

    /**
     * 分页查询用户列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询用户列表", description = "分页查询用户列表，支持关键字搜索和排序")
    public Result<PageResponse<UserPageResponse>> getUserPage(
            @Parameter(description = "分页查询条件", required = true)
            @RequestBody UserPageRequest pageRequest) {
        // 构建分页对象
        Page<SysUser> page = PageUtil.buildPage(pageRequest);
        
        // 构建查询条件
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        PageUtil.buildQuery(queryWrapper, pageRequest, "username", "real_name", "phone");
        
        // 添加其他查询条件
        if (pageRequest.getRoleId() != null) {
            queryWrapper.eq("role_id", pageRequest.getRoleId());
        }
        if (pageRequest.getStatus() != null) {
            queryWrapper.eq("status", pageRequest.getStatus());
        }

        // 执行分页查询
        IPage<SysUser> userPage = sysUserService.page(page, queryWrapper);

        Map<Long, String> roleNameMap = userPage.getRecords().stream()
            .map(SysUser::getRoleId)
            .filter(roleId -> roleId != null)
            .distinct()
            .collect(Collectors.collectingAndThen(
                Collectors.toList(),
                roleIds -> roleIds.isEmpty()
                    ? Map.of()
                    : sysRoleMapper.selectBatchIds(roleIds).stream()
                        .collect(Collectors.toMap(SysRole::getId, role -> role.getRoleName() == null ? "" : role.getRoleName()))
            ));
        
        // 转换为响应VO
        List<UserPageResponse> records = userPage.getRecords().stream()
            .map(user -> {
                UserPageResponse response = new UserPageResponse();
                BeanUtils.copyProperties(user, response);
                response.setRoleName(roleNameMap.getOrDefault(user.getRoleId(), ""));
                return response;
            })
            .collect(Collectors.toList());
        
        // 构建分页响应
        PageResponse<UserPageResponse> pageResponse = PageResponse.of(
            (int) userPage.getCurrent(),
            (int) userPage.getSize(),
            userPage.getTotal(),
            records
        );
        
        return Result.success(pageResponse);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息")
    public Result<SysUser> getUserById(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id) {
        return Result.success(sysUserService.getById(id));
    }

    /**
     * 新增用户
     */
    @PostMapping
    @Operation(summary = "新增用户", description = "创建新用户")
    public Result<Object> addUser(
            @Parameter(description = "用户信息", required = true)
            @RequestBody SysUser user) {
        sysUserService.save(user);
        return Result.success("新增成功");
    }

    /**
     * 更新用户
     */
    @PutMapping
    @Operation(summary = "更新用户", description = "更新用户信息")
    public Result<Object> updateUser(
            @Parameter(description = "用户信息", required = true)
            @RequestBody SysUser user) {
        // 禁止修改内置管理员
        if (user.getId() != null && user.getId().equals(SystemConstants.SUPER_ADMIN_USER_ID)) {
            return Result.fail("内置管理员不允许修改");
        }
        sysUserService.updateById(user);
        return Result.success("更新成功");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "根据ID删除用户")
    public Result<Object> deleteUser(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id) {
        // 禁止删除内置管理员
        if (id.equals(SystemConstants.SUPER_ADMIN_USER_ID)) {
            return Result.fail("内置管理员不允许删除");
        }
        sysUserService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{id}/resetPassword")
    @Operation(summary = "重置密码", description = "重置用户密码为默认密码123456")
    public Result<Object> resetPassword(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id) {
        sysUserService.resetPassword(id);
        return Result.success("密码重置成功");
    }

    /**
     * 修改当前用户密码
     */
    @PutMapping("/changePassword")
    @Operation(summary = "修改密码", description = "修改当前用户密码")
    public Result<Object> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        sysUserService.changePassword(oldPassword, newPassword);
        return Result.success("密码修改成功");
    }
}
