package com.livehouse.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.livehouse.common.result.Result;
import com.livehouse.entity.AppUserSetting;
import com.livehouse.mapper.AppUserSettingMapper;
import com.livehouse.util.H5AuthUtil;
import com.livehouse.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/h5/settings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class H5SettingController {

    private final AppUserSettingMapper settingMapper;
    private final JwtUtil jwtUtil;

    @GetMapping("")
    public Result<AppUserSetting> get(@RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = H5AuthUtil.getUserId(token, jwtUtil);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(getOrCreate(userId));
    }

    @PutMapping("")
    public Result<AppUserSetting> update(@RequestBody AppUserSetting payload,
                                         @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = H5AuthUtil.getUserId(token, jwtUtil);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        AppUserSetting setting = getOrCreate(userId);
        setting.setMessageNotify(defaultFlag(payload.getMessageNotify(), setting.getMessageNotify()));
        setting.setSmsNotify(defaultFlag(payload.getSmsNotify(), setting.getSmsNotify()));
        setting.setBrowseHistory(defaultFlag(payload.getBrowseHistory(), setting.getBrowseHistory()));
        settingMapper.updateById(setting);
        return Result.success(setting);
    }

    private AppUserSetting getOrCreate(Long userId) {
        AppUserSetting setting = settingMapper.selectOne(new LambdaQueryWrapper<AppUserSetting>()
                .eq(AppUserSetting::getUserId, userId)
                .last("LIMIT 1"));
        if (setting != null) {
            return setting;
        }
        setting = new AppUserSetting();
        setting.setUserId(userId);
        setting.setMessageNotify(1);
        setting.setSmsNotify(1);
        setting.setBrowseHistory(1);
        settingMapper.insert(setting);
        return setting;
    }

    private Integer defaultFlag(Integer next, Integer current) {
        return next == null ? current : next;
    }
}
