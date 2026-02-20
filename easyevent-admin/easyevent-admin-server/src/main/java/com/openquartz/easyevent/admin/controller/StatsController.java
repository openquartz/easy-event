package com.openquartz.easyevent.admin.controller;

import com.openquartz.easyevent.admin.dao.BusEventDao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@Tag(name = "Statistics", description = "API for monitoring statistics")
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final BusEventDao busEventDao;

    @GetMapping("/test-count-by-state")
    @Operation(summary = "Test Count By State", description = "Test countByState method")
    public Map<String, Object> testCountByState() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> countByState = busEventDao.countByState();
            result.put("success", true);
            result.put("data", countByState);
            result.put("size", countByState.size());
        } catch (Exception e) {
            log.error("Error in testCountByState", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/test-trend")
    @Operation(summary = "Test Trend", description = "Test countTrendLast24Hours method")
    public Map<String, Object> testTrend() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> trend = busEventDao.countTrendLast24Hours();
            result.put("success", true);
            result.put("data", trend);
            result.put("size", trend.size());
        } catch (Exception e) {
            log.error("Error in testTrend", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/test-latency-ranking")
    @Operation(summary = "Test Latency Ranking", description = "Test subscriberLatencyRanking method")
    public Map<String, Object> testLatencyRanking() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> ranking = busEventDao.subscriberLatencyRanking();
            result.put("success", true);
            result.put("data", ranking);
            result.put("size", ranking.size());
        } catch (Exception e) {
            log.error("Error in testLatencyRanking", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get Dashboard Stats", description = "Returns aggregated statistics for the dashboard")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Count by state
            log.info("Testing countByState...");
            List<Map<String, Object>> countByState = busEventDao.countByState();
            result.put("stateDistribution", countByState);
            log.info("countByState successful, size: {}", countByState.size());
        } catch (Exception e) {
            log.error("Error in countByState", e);
            result.put("stateDistribution_error", e.getMessage());
        }
        
        try {
            // Trend
            log.info("Testing countTrendLast24Hours...");
            List<Map<String, Object>> trend = busEventDao.countTrendLast24Hours();
            result.put("trend", trend);
            log.info("countTrendLast24Hours successful, size: {}", trend.size());
        } catch (Exception e) {
            log.error("Error in countTrendLast24Hours", e);
            result.put("trend_error", e.getMessage());
        }
        
        try {
            // Latency Ranking
            log.info("Testing subscriberLatencyRanking...");
            List<Map<String, Object>> ranking = busEventDao.subscriberLatencyRanking();
            result.put("latencyRanking", ranking);
            log.info("subscriberLatencyRanking successful, size: {}", ranking.size());
        } catch (Exception e) {
            log.error("Error in subscriberLatencyRanking", e);
            result.put("latencyRanking_error", e.getMessage());
        }
        
        return result;
    }
}