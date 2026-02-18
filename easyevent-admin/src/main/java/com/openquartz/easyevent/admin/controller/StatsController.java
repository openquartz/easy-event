package com.openquartz.easyevent.admin.controller;

import com.openquartz.easyevent.admin.dao.BusEventDao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@Tag(name = "Statistics", description = "API for monitoring statistics")
@RequiredArgsConstructor
public class StatsController {

    private final BusEventDao busEventDao;

    @GetMapping("/dashboard")
    @Operation(summary = "Get Dashboard Stats", description = "Returns aggregated statistics for the dashboard")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> result = new HashMap<>();
        
        // Count by state
        List<Map<String, Object>> countByState = busEventDao.countByState();
        result.put("stateDistribution", countByState);
        
        // Trend
        List<Map<String, Object>> trend = busEventDao.countTrendLast24Hours();
        result.put("trend", trend);
        
        // Latency Ranking
        List<Map<String, Object>> ranking = busEventDao.subscriberLatencyRanking();
        result.put("latencyRanking", ranking);
        
        return result;
    }
}
