package com.livehouse.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.result.Result;
import com.livehouse.entity.AppFavorite;
import com.livehouse.mapper.AppFavoriteMapper;
import com.livehouse.util.H5AuthUtil;
import com.livehouse.util.JwtUtil;
import com.livehouse.util.PageUtil;
import com.livehouse.vo.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/h5/favorite")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class H5FavoriteController {

    private final AppFavoriteMapper favoriteMapper;
    private final JwtUtil jwtUtil;

    @PostMapping("/toggle")
    public Result<Map<String, Object>> toggle(@RequestBody AppFavorite payload,
                                              @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = H5AuthUtil.getUserId(token, jwtUtil);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        if (payload.getTargetId() == null || isBlank(payload.getTargetType())) {
            return Result.error("收藏对象不能为空");
        }

        AppFavorite existed = favoriteMapper.selectAny(userId, payload.getTargetType(), payload.getTargetId());
        boolean favorited;
        if (existed == null) {
            payload.setUserId(userId);
            favoriteMapper.insert(payload);
            favorited = true;
        } else if (existed.getDeleted() == null || existed.getDeleted() == 0) {
            favoriteMapper.deleteById(existed.getId());
            favorited = false;
        } else {
            existed.setTargetTitle(payload.getTargetTitle());
            existed.setTargetCover(payload.getTargetCover());
            existed.setTargetDesc(payload.getTargetDesc());
            favoriteMapper.restore(existed);
            favorited = true;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("favorited", favorited);
        return Result.success(data);
    }

    @GetMapping("/list")
    public Result<PageResponse<AppFavorite>> list(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(value = "targetType", required = false) String targetType) {
        Long userId = H5AuthUtil.getUserId(token, jwtUtil);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        LambdaQueryWrapper<AppFavorite> wrapper = new LambdaQueryWrapper<AppFavorite>()
                .eq(AppFavorite::getUserId, userId)
                .eq(AppFavorite::getDeleted, 0)
                .orderByDesc(AppFavorite::getUpdateTime)
                .orderByDesc(AppFavorite::getCreateTime);
        if (!isBlank(targetType)) {
            wrapper.eq(AppFavorite::getTargetType, targetType);
        }
        Page<AppFavorite> page = new Page<>(pageNum, pageSize);
        return Result.success(PageUtil.buildPageResponse(favoriteMapper.selectPage(page, wrapper)));
    }

    @GetMapping("/check")
    public Result<Map<String, Object>> check(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam("targetType") String targetType,
            @RequestParam("targetId") Long targetId) {
        Long userId = H5AuthUtil.getUserId(token, jwtUtil);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        boolean favorited = favoriteMapper.selectCount(new LambdaQueryWrapper<AppFavorite>()
                .eq(AppFavorite::getUserId, userId)
                .eq(AppFavorite::getTargetType, targetType)
                .eq(AppFavorite::getTargetId, targetId)
                .eq(AppFavorite::getDeleted, 0)) > 0;
        Map<String, Object> data = new HashMap<>();
        data.put("favorited", favorited);
        return Result.success(data);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = H5AuthUtil.getUserId(token, jwtUtil);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        AppFavorite favorite = favoriteMapper.selectById(id);
        if (favorite == null || !userId.equals(favorite.getUserId())) {
            return Result.error(404, "收藏不存在");
        }
        favoriteMapper.deleteById(id);
        return Result.success();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
