package com.livehouse.service;

import java.time.LocalDate;
import java.util.Map;

public interface DashboardService {

    Map<String, Object> getDashboardData();

    Map<String, Object> getLiveStatistics(Long sessionId, LocalDate startDate, LocalDate endDate,
                                          String timeDimension, int pageNum, int pageSize);

    String exportReport(String reportType, LocalDate startDate, LocalDate endDate);
}
