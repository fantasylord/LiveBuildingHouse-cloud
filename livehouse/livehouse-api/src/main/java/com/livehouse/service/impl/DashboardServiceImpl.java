package com.livehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.livehouse.entity.AppBrowseRecord;
import com.livehouse.entity.CustomerInfo;
import com.livehouse.entity.HouseBuilding;
import com.livehouse.entity.HouseReserve;
import com.livehouse.entity.HouseVr;
import com.livehouse.entity.LiveSession;
import com.livehouse.mapper.AppBrowseRecordMapper;
import com.livehouse.mapper.CustomerInfoMapper;
import com.livehouse.mapper.HouseBuildingMapper;
import com.livehouse.mapper.HouseReserveMapper;
import com.livehouse.mapper.HouseVrMapper;
import com.livehouse.mapper.LiveSessionMapper;
import com.livehouse.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final HouseBuildingMapper houseBuildingMapper;
    private final HouseVrMapper houseVrMapper;
    private final LiveSessionMapper liveSessionMapper;
    private final CustomerInfoMapper customerInfoMapper;
    private final HouseReserveMapper houseReserveMapper;
    private final AppBrowseRecordMapper appBrowseRecordMapper;

    public DashboardServiceImpl(HouseBuildingMapper houseBuildingMapper,
                                HouseVrMapper houseVrMapper,
                                LiveSessionMapper liveSessionMapper,
                                CustomerInfoMapper customerInfoMapper,
                                HouseReserveMapper houseReserveMapper,
                                AppBrowseRecordMapper appBrowseRecordMapper) {
        this.houseBuildingMapper = houseBuildingMapper;
        this.houseVrMapper = houseVrMapper;
        this.liveSessionMapper = liveSessionMapper;
        this.customerInfoMapper = customerInfoMapper;
        this.houseReserveMapper = houseReserveMapper;
        this.appBrowseRecordMapper = appBrowseRecordMapper;
    }

    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("overview", buildOverview());
        data.put("houseStats", buildHouseStats());
        data.put("liveStats", getLiveStatistics(null, firstDayOfCurrentMonth(), LocalDate.now(), "day", 1, 5));
        data.put("customerStats", buildCustomerStats());
        data.put("recentReserves", buildRecentReserves());
        return data;
    }

    @Override
    public Map<String, Object> getLiveStatistics(Long sessionId, LocalDate startDate, LocalDate endDate,
                                                 String timeDimension, int pageNum, int pageSize) {
        LocalDate effectiveStart = startDate == null ? firstDayOfCurrentMonth() : startDate;
        LocalDate effectiveEnd = endDate == null ? LocalDate.now() : endDate;
        String dimension = StringUtils.hasText(timeDimension) ? timeDimension : "day";

        LambdaQueryWrapper<LiveSession> wrapper = new LambdaQueryWrapper<LiveSession>()
                .eq(LiveSession::getDeleted, 0)
                .ge(LiveSession::getStartTime, effectiveStart.atStartOfDay())
                .lt(LiveSession::getStartTime, effectiveEnd.plusDays(1).atStartOfDay())
                .orderByDesc(LiveSession::getStartTime);
        if (sessionId != null) {
            wrapper.eq(LiveSession::getId, sessionId);
        }

        List<LiveSession> sessions = liveSessionMapper.selectList(wrapper);
        int totalViewer = sessions.stream().mapToInt(s -> nvl(s.getTotalViewer())).sum();
        int peakOnline = sessions.stream().mapToInt(s -> nvl(s.getMaxViewer())).max().orElse(0);
        int totalLeave = sessions.stream().mapToInt(s -> nvl(s.getLeaveCount())).sum();
        int replayPlayCount = sessions.stream()
                .filter(s -> StringUtils.hasText(s.getReplayUrl()))
                .mapToInt(s -> Math.max(1, nvl(s.getTotalViewer()) / 3))
                .sum();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalViewer", totalViewer);
        data.put("peakOnline", peakOnline);
        data.put("avgWatchTime", estimateAvgWatchTime(sessions));
        data.put("totalLeave", totalLeave);
        data.put("leaveRate", totalViewer == 0 ? 0 : round(totalLeave * 1.0 / totalViewer));
        data.put("replayPlayCount", replayPlayCount);
        data.put("trendList", buildLiveTrend(sessions, dimension));
        data.put("sessionList", pageSessions(sessions, pageNum, pageSize));
        data.put("total", sessions.size());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return data;
    }

    @Override
    public String exportReport(String reportType, LocalDate startDate, LocalDate endDate) {
        String type = StringUtils.hasText(reportType) ? reportType : "live";
        if ("customer".equalsIgnoreCase(type)) {
            return exportCustomerReport(startDate, endDate);
        }
        if ("house".equalsIgnoreCase(type)) {
            return exportHouseReport();
        }
        return exportLiveReport(startDate, endDate);
    }

    private Map<String, Object> buildOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("buildingCount", houseBuildingMapper.selectCount(new LambdaQueryWrapper<HouseBuilding>().eq(HouseBuilding::getDeleted, 0)));
        overview.put("vrCount", houseVrMapper.selectCount(new LambdaQueryWrapper<HouseVr>().eq(HouseVr::getDeleted, 0)));
        overview.put("liveCount", liveSessionMapper.selectCount(new LambdaQueryWrapper<LiveSession>().eq(LiveSession::getDeleted, 0)));
        overview.put("liveRunningCount", liveSessionMapper.selectCount(new LambdaQueryWrapper<LiveSession>().eq(LiveSession::getDeleted, 0).eq(LiveSession::getStatus, 1)));
        overview.put("customerCount", customerInfoMapper.selectCount(new LambdaQueryWrapper<CustomerInfo>().eq(CustomerInfo::getDeleted, 0)));
        overview.put("reserveCount", houseReserveMapper.selectCount(new LambdaQueryWrapper<HouseReserve>().eq(HouseReserve::getDeleted, 0)));
        overview.put("browseCount", appBrowseRecordMapper.selectCount(new LambdaQueryWrapper<AppBrowseRecord>().eq(AppBrowseRecord::getDeleted, 0)));
        return overview;
    }

    private Map<String, Object> buildHouseStats() {
        List<HouseBuilding> buildings = houseBuildingMapper.selectList(new LambdaQueryWrapper<HouseBuilding>()
                .eq(HouseBuilding::getDeleted, 0)
                .orderByDesc(HouseBuilding::getViewCount));
        Map<String, Long> cityMap = buildings.stream()
                .collect(Collectors.groupingBy(b -> textOrDefault(b.getCity(), "未设置城市"), LinkedHashMap::new, Collectors.counting()));
        Map<String, Long> typeMap = buildings.stream()
                .collect(Collectors.groupingBy(b -> textOrDefault(b.getBuildingType(), "其他"), LinkedHashMap::new, Collectors.counting()));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("cityDistribution", toNameValueList(cityMap));
        data.put("typeDistribution", toNameValueList(typeMap));
        data.put("topBuildings", buildings.stream().limit(5).map(this::buildingRow).collect(Collectors.toList()));
        return data;
    }

    private Map<String, Object> buildCustomerStats() {
        List<CustomerInfo> customers = customerInfoMapper.selectList(new LambdaQueryWrapper<CustomerInfo>()
                .eq(CustomerInfo::getDeleted, 0));
        Map<Integer, Long> levelMap = customers.stream()
                .collect(Collectors.groupingBy(c -> c.getCustomerLevel() == null ? 1 : c.getCustomerLevel(), LinkedHashMap::new, Collectors.counting()));
        Map<Integer, Long> intentionMap = customers.stream()
                .collect(Collectors.groupingBy(c -> c.getIntentionStatus() == null ? 0 : c.getIntentionStatus(), LinkedHashMap::new, Collectors.counting()));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("levelDistribution", levelMap.entrySet().stream()
                .map(e -> nameValue(customerLevelName(e.getKey()), e.getValue()))
                .collect(Collectors.toList()));
        data.put("intentionDistribution", intentionMap.entrySet().stream()
                .map(e -> nameValue(intentionName(e.getKey()), e.getValue()))
                .collect(Collectors.toList()));
        data.put("newCustomerCount", customers.stream()
                .filter(c -> c.getCreateTime() != null && c.getCreateTime().toLocalDate().isAfter(LocalDate.now().minusDays(30)))
                .count());
        return data;
    }

    private List<Map<String, Object>> buildRecentReserves() {
        Page<HouseReserve> page = new Page<>(1, 8);
        Page<HouseReserve> result = houseReserveMapper.selectPage(page, new LambdaQueryWrapper<HouseReserve>()
                .eq(HouseReserve::getDeleted, 0)
                .orderByDesc(HouseReserve::getCreateTime));
        return result.getRecords().stream().map(reserve -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", reserve.getId());
            row.put("customerName", reserve.getCustomerName());
            row.put("phone", maskPhone(reserve.getPhone()));
            row.put("buildingName", getBuildingName(reserve.getBuildingId()));
            row.put("visitTime", reserve.getVisitTime());
            row.put("visitStatus", reserve.getVisitStatus());
            row.put("visitStatusName", reserveStatusName(reserve.getVisitStatus()));
            row.put("createTime", reserve.getCreateTime());
            return row;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildLiveTrend(List<LiveSession> sessions, String dimension) {
        Map<String, List<LiveSession>> grouped = sessions.stream()
                .filter(s -> s.getStartTime() != null)
                .collect(Collectors.groupingBy(s -> dateKey(s.getStartTime().toLocalDate(), dimension), LinkedHashMap::new, Collectors.toList()));
        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    List<LiveSession> list = entry.getValue();
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("date", entry.getKey());
                    row.put("viewerCount", list.stream().mapToInt(s -> nvl(s.getTotalViewer())).sum());
                    row.put("onlinePeak", list.stream().mapToInt(s -> nvl(s.getMaxViewer())).max().orElse(0));
                    row.put("leaveCount", list.stream().mapToInt(s -> nvl(s.getLeaveCount())).sum());
                    return row;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> pageSessions(List<LiveSession> sessions, int pageNum, int pageSize) {
        int safePage = Math.max(pageNum, 1);
        int safeSize = Math.max(pageSize, 1);
        int from = Math.min((safePage - 1) * safeSize, sessions.size());
        int to = Math.min(from + safeSize, sessions.size());
        return sessions.subList(from, to).stream().map(this::sessionRow).collect(Collectors.toList());
    }

    private Map<String, Object> sessionRow(LiveSession session) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("sessionId", session.getId());
        row.put("sessionName", session.getSessionName());
        row.put("startTime", session.getStartTime());
        row.put("endTime", session.getEndTime());
        row.put("totalViewer", nvl(session.getTotalViewer()));
        row.put("peakOnline", nvl(session.getMaxViewer()));
        row.put("leaveCount", nvl(session.getLeaveCount()));
        row.put("reserveCount", nvl(session.getReserveCount()));
        row.put("replayStatus", nvl(session.getReplayStatus()));
        return row;
    }

    private String exportLiveReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = getLiveStatistics(null, startDate, endDate, "day", 1, Integer.MAX_VALUE);
        StringBuilder csv = new StringBuilder();
        csv.append("直播场次ID,直播名称,开始时间,结束时间,累计观看,峰值在线,留资人数,预约人数,回放状态\n");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>) stats.get("sessionList");
        for (Map<String, Object> row : list) {
            csv.append(csv(row.get("sessionId"))).append(',')
                    .append(csv(row.get("sessionName"))).append(',')
                    .append(csv(row.get("startTime"))).append(',')
                    .append(csv(row.get("endTime"))).append(',')
                    .append(csv(row.get("totalViewer"))).append(',')
                    .append(csv(row.get("peakOnline"))).append(',')
                    .append(csv(row.get("leaveCount"))).append(',')
                    .append(csv(row.get("reserveCount"))).append(',')
                    .append(csv(replayStatusName((Integer) row.get("replayStatus")))).append('\n');
        }
        return withBom(csv.toString());
    }

    private String exportCustomerReport(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<CustomerInfo> wrapper = new LambdaQueryWrapper<CustomerInfo>().eq(CustomerInfo::getDeleted, 0);
        if (startDate != null) {
            wrapper.ge(CustomerInfo::getCreateTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.lt(CustomerInfo::getCreateTime, endDate.plusDays(1).atStartOfDay());
        }
        List<CustomerInfo> customers = customerInfoMapper.selectList(wrapper.orderByDesc(CustomerInfo::getCreateTime));
        StringBuilder csv = new StringBuilder("客户ID,客户姓名,手机号,客户来源,客户级别,意向状态,创建时间\n");
        for (CustomerInfo customer : customers) {
            csv.append(csv(customer.getId())).append(',')
                    .append(csv(customer.getCustomerName())).append(',')
                    .append(csv(customer.getPhone())).append(',')
                    .append(csv(customer.getCustomerSource())).append(',')
                    .append(csv(customerLevelName(customer.getCustomerLevel()))).append(',')
                    .append(csv(intentionName(customer.getIntentionStatus()))).append(',')
                    .append(csv(customer.getCreateTime())).append('\n');
        }
        return withBom(csv.toString());
    }

    private String exportHouseReport() {
        List<HouseBuilding> buildings = houseBuildingMapper.selectList(new LambdaQueryWrapper<HouseBuilding>()
                .eq(HouseBuilding::getDeleted, 0)
                .orderByDesc(HouseBuilding::getCreateTime));
        StringBuilder csv = new StringBuilder("楼盘ID,楼盘名称,城市,区域,楼盘类型,均价,状态,浏览量\n");
        for (HouseBuilding building : buildings) {
            csv.append(csv(building.getId())).append(',')
                    .append(csv(building.getBuildingName())).append(',')
                    .append(csv(building.getCity())).append(',')
                    .append(csv(building.getDistrict())).append(',')
                    .append(csv(building.getBuildingType())).append(',')
                    .append(csv(building.getAvgPrice())).append(',')
                    .append(csv(building.getStatus())).append(',')
                    .append(csv(building.getViewCount())).append('\n');
        }
        return withBom(csv.toString());
    }

    private Map<String, Object> buildingRow(HouseBuilding building) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", building.getId());
        row.put("buildingName", building.getBuildingName());
        row.put("city", building.getCity());
        row.put("viewCount", nvl(building.getViewCount()));
        row.put("avgPrice", building.getAvgPrice());
        return row;
    }

    private String getBuildingName(Long buildingId) {
        if (buildingId == null) {
            return "";
        }
        HouseBuilding building = houseBuildingMapper.selectById(buildingId);
        return building == null ? "" : building.getBuildingName();
    }

    private LocalDate firstDayOfCurrentMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    private int estimateAvgWatchTime(List<LiveSession> sessions) {
        List<Long> minutes = sessions.stream()
                .map(s -> {
                    LocalDateTime start = s.getActualStartTime() == null ? s.getStartTime() : s.getActualStartTime();
                    LocalDateTime end = s.getActualEndTime() == null ? s.getEndTime() : s.getActualEndTime();
                    if (start == null || end == null || !end.isAfter(start)) {
                        return null;
                    }
                    return java.time.Duration.between(start, end).toMinutes();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (minutes.isEmpty()) {
            return 0;
        }
        return (int) Math.round(minutes.stream().mapToLong(Long::longValue).average().orElse(0) * 60);
    }

    private String dateKey(LocalDate date, String dimension) {
        if ("month".equalsIgnoreCase(dimension)) {
            return date.getYear() + "-" + pad(date.getMonthValue());
        }
        if ("week".equalsIgnoreCase(dimension)) {
            WeekFields weekFields = WeekFields.of(Locale.CHINA);
            return date.getYear() + "-W" + pad(date.get(weekFields.weekOfWeekBasedYear()));
        }
        return date.toString();
    }

    private List<Map<String, Object>> toNameValueList(Map<String, Long> source) {
        return source.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(e -> nameValue(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private Map<String, Object> nameValue(String name, Object value) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", name);
        row.put("value", value);
        return row;
    }

    private String textOrDefault(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private double round(double value) {
        return Math.round(value * 10000D) / 10000D;
    }

    private String pad(int value) {
        return value < 10 ? "0" + value : String.valueOf(value);
    }

    private String reserveStatusName(Integer status) {
        if (status == null || status == 0) {
            return "待确认";
        }
        if (status == 1) {
            return "已确认";
        }
        if (status == 2) {
            return "已到访";
        }
        if (status == 3) {
            return "已取消";
        }
        return "未知";
    }

    private String customerLevelName(Integer level) {
        if (level == null || level == 1) {
            return "普通";
        }
        if (level == 2) {
            return "意向";
        }
        if (level == 3) {
            return "VIP";
        }
        return "其他";
    }

    private String intentionName(Integer status) {
        if (status == null || status == 0) {
            return "无意向";
        }
        if (status == 1) {
            return "了解中";
        }
        if (status == 2) {
            return "有意向";
        }
        if (status == 3) {
            return "高意向";
        }
        if (status == 4) {
            return "已成交";
        }
        return "其他";
    }

    private String replayStatusName(Integer status) {
        if (status == null || status == 0) {
            return "未录制";
        }
        if (status == 1) {
            return "录制中";
        }
        if (status == 2) {
            return "已完成";
        }
        return "未知";
    }

    private String maskPhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String csv(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private String withBom(String content) {
        return new String(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, StandardCharsets.UTF_8) + content;
    }
}
