package com.livehouse.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.result.Result;
import com.livehouse.entity.AppBrowseRecord;
import com.livehouse.entity.AppUserSetting;
import com.livehouse.mapper.AppBrowseRecordMapper;
import com.livehouse.mapper.AppUserSettingMapper;
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

import java.util.Map;

@RestController
@RequestMapping("/api/h5/history")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class H5HistoryController {

    private final AppBrowseRecordMapper browseRecordMapper;
    private final AppUserSettingMapper settingMapper;
    private final JwtUtil jwtUtil;

    @PostMapping("")
    public Result<Void> record(@RequestBody AppBrowseRecord payload,
                               @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = H5AuthUtil.getUserId(token, jwtUtil);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        if (!isHistoryEnabled(userId) || payload.getTargetId() == null || isBlank(payload.getTargetType())) {
            return Result.success();
        }

        AppBrowseRecord existed = browseRecordMapper.selectAny(userId, payload.getTargetType(), payload.getTargetId());
        if (existed == null) {
            payload.setUserId(userId);
            browseRecordMapper.insert(payload);
        } else if (existed.getDeleted() != null && existed.getDeleted() == 1) {
            existed.setTargetTitle(payload.getTargetTitle());
            existed.setTargetCover(payload.getTargetCover());
            existed.setTargetDesc(payload.getTargetDesc());
            browseRecordMapper.restore(existed);
        } else {
            existed.setTargetTitle(payload.getTargetTitle());
            existed.setTargetCover(payload.getTargetCover());
            existed.setTargetDesc(payload.getTargetDesc());
            browseRecordMapper.updateById(existed);
        }
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResponse<AppBrowseRecord>> list(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(value = "targetType", required = false) String targetType) {
        Long userId = H5AuthUtil.getUserId(token, jwtUtil);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        LambdaQueryWrapper<AppBrowseRecord> wrapper = new LambdaQueryWrapper<AppBrowseRecord>()
                .eq(AppBrowseRecord::getUserId, userId)
                .eq(AppBrowseRecord::getDeleted, 0)
                .orderByDesc(AppBrowseRecord::getUpdateTime)
                .orderByDesc(AppBrowseRecord::getCreateTime);
        if (!isBlank(targetType)) {
            wrapper.eq(AppBrowseRecord::getTargetType, targetType);
        }
        Page<AppBrowseRecord> page = new Page<>(pageNum, pageSize);
        return Result.success(PageUtil.buildPageResponse(browseRecordMapper.selectPage(page, wrapper)));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = H5AuthUtil.getUserId(token, jwtUtil);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        AppBrowseRecord record = browseRecordMapper.selectById(id);
        if (record == null || !userId.equals(record.getUserId())) {
            return Result.error(404, "记录不存在");
        }
        browseRecordMapper.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/clear")
    public Result<Void> clear(@RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = H5AuthUtil.getUserId(token, jwtUtil);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        browseRecordMapper.delete(new LambdaQueryWrapper<AppBrowseRecord>().eq(AppBrowseRecord::getUserId, userId));
        return Result.success();
    }

    private boolean isHistoryEnabled(Long userId) {
        AppUserSetting setting = settingMapper.selectOne(new LambdaQueryWrapper<AppUserSetting>()
                .eq(AppUserSetting::getUserId, userId)
                .last("LIMIT 1"));
        return setting == null || setting.getBrowseHistory() == null || setting.getBrowseHistory() == 1;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
