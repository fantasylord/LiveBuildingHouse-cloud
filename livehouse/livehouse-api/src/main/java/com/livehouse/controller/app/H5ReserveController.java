package com.livehouse.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.common.result.Result;
import com.livehouse.entity.AppUser;
import com.livehouse.entity.HouseBuilding;
import com.livehouse.entity.HouseReserve;
import com.livehouse.entity.LiveSession;
import com.livehouse.service.AppUserService;
import com.livehouse.service.HouseBuildingService;
import com.livehouse.service.HouseReserveService;
import com.livehouse.service.LiveSessionService;
import com.livehouse.util.H5AuthUtil;
import com.livehouse.util.JwtUtil;
import com.livehouse.util.PageUtil;
import com.livehouse.vo.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/h5/reserve")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class H5ReserveController {

    private final HouseReserveService houseReserveService;
    private final HouseBuildingService houseBuildingService;
    private final LiveSessionService liveSessionService;
    private final AppUserService appUserService;
    private final JwtUtil jwtUtil;

    @PostMapping("")
    public Result<HouseReserve> create(@RequestBody HouseReserve reserve,
                                       @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long userId = H5AuthUtil.getUserId(token, jwtUtil);
            if (userId != null) {
                reserve.setAppUserId(userId);
                AppUser user = appUserService.getById(userId);
                if (user != null && isBlank(reserve.getPhone())) {
                    reserve.setPhone(user.getPhone());
                }
                if (user != null && isBlank(reserve.getCustomerName())) {
                    reserve.setCustomerName(user.getRealName());
                }
            }
            if (isBlank(reserve.getReserveCode())) {
                reserve.setReserveCode("RSV" + System.currentTimeMillis());
            }
            if (reserve.getVisitStatus() == null) {
                reserve.setVisitStatus(0);
            }
            boolean success = houseReserveService.save(reserve);
            return success ? Result.success(reserve) : Result.error("预约失败");
        } catch (Exception e) {
            log.error("Create H5 reservation failed", e);
            return Result.error("预约失败");
        }
    }

    @GetMapping("/my")
    public Result<PageResponse<Map<String, Object>>> myList(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        Long userId = H5AuthUtil.getUserId(token, jwtUtil);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        AppUser user = appUserService.getById(userId);
        LambdaQueryWrapper<HouseReserve> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseReserve::getDeleted, 0)
                .and(query -> {
                    query.eq(HouseReserve::getAppUserId, userId);
                    if (user != null && !isBlank(user.getPhone())) {
                        query.or().eq(HouseReserve::getPhone, user.getPhone());
                    }
                })
                .orderByDesc(HouseReserve::getCreateTime);

        Page<HouseReserve> page = houseReserveService.page(new Page<>(pageNum, pageSize), wrapper);
        Page<Map<String, Object>> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(page.getRecords().stream().map(this::buildReserveItem).toList());
        return Result.success(PageUtil.buildPageResponse(resultPage));
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                               @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = H5AuthUtil.getUserId(token, jwtUtil);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        AppUser user = appUserService.getById(userId);
        HouseReserve reserve = houseReserveService.getById(id);
        if (reserve == null) {
            return Result.error(404, "预约不存在");
        }

        boolean owner = userId.equals(reserve.getAppUserId())
                || (user != null && !isBlank(user.getPhone()) && user.getPhone().equals(reserve.getPhone()));
        if (!owner) {
            return Result.error(403, "无权操作");
        }
        if (reserve.getVisitStatus() != null && reserve.getVisitStatus() >= 2) {
            return Result.error("当前预约状态不可取消");
        }

        reserve.setVisitStatus(3);
        houseReserveService.updateById(reserve);
        return Result.success();
    }

    @GetMapping("/check/{buildingId}/{phone}")
    public Result<Boolean> checkTodayReservation(@PathVariable Long buildingId, @PathVariable String phone) {
        try {
            boolean hasReservation = houseReserveService.lambdaQuery()
                    .eq(HouseReserve::getBuildingId, buildingId)
                    .eq(HouseReserve::getPhone, phone)
                    .eq(HouseReserve::getDeleted, 0)
                    .exists();
            return Result.success(hasReservation);
        } catch (Exception e) {
            log.error("Check H5 reservation failed", e);
            return Result.success(false);
        }
    }

    private Map<String, Object> buildReserveItem(HouseReserve reserve) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", reserve.getId());
        item.put("reserveCode", reserve.getReserveCode());
        item.put("buildingId", reserve.getBuildingId());
        item.put("unitId", reserve.getUnitId());
        item.put("liveSessionId", reserve.getLiveSessionId());
        item.put("customerName", reserve.getCustomerName());
        item.put("phone", reserve.getPhone());
        item.put("visitTime", reserve.getVisitTime());
        item.put("visitType", reserve.getVisitType());
        item.put("visitStatus", reserve.getVisitStatus());
        item.put("createTime", reserve.getCreateTime());

        HouseBuilding building = reserve.getBuildingId() == null ? null : houseBuildingService.getById(reserve.getBuildingId());
        LiveSession live = reserve.getLiveSessionId() == null ? null : liveSessionService.getById(reserve.getLiveSessionId());
        item.put("buildingName", building == null ? null : building.getBuildingName());
        item.put("buildingCover", building == null ? null : building.getCoverImage());
        item.put("liveName", live == null ? null : live.getSessionName());
        return item;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
