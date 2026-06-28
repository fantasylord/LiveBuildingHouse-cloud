package com.livehouse.controller.admin.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.livehouse.common.result.Result;
import com.livehouse.entity.HomeConfig;
import com.livehouse.service.HomeConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/home-config")
@RequiredArgsConstructor
public class HomeConfigController {

    private final HomeConfigService homeConfigService;

    @GetMapping("/list")
    public Result<List<HomeConfig>> list() {
        List<HomeConfig> list = homeConfigService.list(new LambdaQueryWrapper<HomeConfig>()
                .orderByAsc(HomeConfig::getSortOrder)
                .orderByAsc(HomeConfig::getCreateTime));
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<HomeConfig> getById(@PathVariable Long id) {
        HomeConfig config = homeConfigService.getById(id);
        if (config == null) {
            return Result.error(404, "配置不存在");
        }
        return Result.success(config);
    }

    @PostMapping("")
    public Result<HomeConfig> create(@RequestBody HomeConfig config) {
        homeConfigService.save(config);
        return Result.success(config);
    }

    @PutMapping("/{id}")
    public Result<HomeConfig> update(@PathVariable Long id, @RequestBody HomeConfig config) {
        config.setId(id);
        homeConfigService.updateById(config);
        return Result.success(config);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        homeConfigService.removeById(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<HomeConfig> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        HomeConfig config = homeConfigService.getById(id);
        if (config == null) {
            return Result.error(404, "配置不存在");
        }
        config.setStatus(status);
        homeConfigService.updateById(config);
        return Result.success(config);
    }
}