package com.livehouse.controller.app;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.result.Result;
import com.livehouse.entity.HouseBuilding;
import com.livehouse.entity.LiveSession;
import com.livehouse.service.HouseBuildingService;
import com.livehouse.service.HomeConfigService;
import com.livehouse.service.LiveSessionService;
import com.livehouse.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/h5/home")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class H5HomeController {

    private final HouseBuildingService houseBuildingService;
    private final LiveSessionService liveSessionService;
    private final HomeConfigService homeConfigService;

    @GetMapping("/index")
    public Result<Map<String, Object>> index() {
        Map<String, Object> result = new HashMap<>();

        String bannerConfig = homeConfigService.getValueByKey("banners");
        List<Map<String, Object>> banners;
        if (bannerConfig != null) {
            JSONArray jsonArray = JsonUtils.parseArray(bannerConfig);
            banners = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                Map<String, Object> map = new HashMap<>();
                map.put("id", json.getLong("id"));
                map.put("title", json.getString("title"));
                map.put("coverImage", json.getString("coverImage"));
                map.put("linkType", json.getString("linkType"));
                map.put("linkId", json.getLong("linkId"));
                banners.add(map);
            }
        } else {
            banners = houseBuildingService.list(
                    new LambdaQueryWrapper<HouseBuilding>()
                            .eq(HouseBuilding::getStatus, 1)
                            .eq(HouseBuilding::getPrivated, 0)
                            .orderByDesc(HouseBuilding::getSortOrder)
                            .last("LIMIT 3")
            ).stream().map(b -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", b.getId());
                m.put("title", b.getBuildingName());
                m.put("coverImage", b.getCoverImage());
                m.put("linkType", "house");
                m.put("linkId", b.getId());
                return m;
            }).toList();
        }
        result.put("banners", banners);

        Page<HouseBuilding> hotPage = houseBuildingService.page(new Page<>(1, 6),
                new LambdaQueryWrapper<HouseBuilding>()
                        .eq(HouseBuilding::getStatus, 1)
                        .eq(HouseBuilding::getPrivated, 0)
                        .orderByDesc(HouseBuilding::getViewCount)
                        .orderByDesc(HouseBuilding::getSortOrder)
        );
        result.put("hotHouses", hotPage.getRecords());

        Page<LiveSession> livePage = liveSessionService.page(new Page<>(1, 4),
                new LambdaQueryWrapper<LiveSession>()
                        .eq(LiveSession::getStatus, 0)
                        .orderByAsc(LiveSession::getStartTime)
        );
        result.put("upcomingLives", livePage.getRecords());

        Page<LiveSession> livingPage = liveSessionService.page(new Page<>(1, 2),
                new LambdaQueryWrapper<LiveSession>()
                        .eq(LiveSession::getStatus, 1)
        );
        result.put("livingLives", livingPage.getRecords());

        return Result.success(result);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> result = new HashMap<>();

        long totalHouse = houseBuildingService.count(
                new LambdaQueryWrapper<HouseBuilding>()
                        .eq(HouseBuilding::getStatus, 1)
                        .eq(HouseBuilding::getPrivated, 0)
        );
        result.put("totalHouse", totalHouse);

        long totalLive = liveSessionService.count(
                new LambdaQueryWrapper<LiveSession>()
                        .eq(LiveSession::getStatus, 1)
        );
        result.put("totalLive", totalLive);

        return Result.success(result);
    }
}
