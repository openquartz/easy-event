package com.openquartz.easyevent.admin.controller;

import com.openquartz.easyevent.admin.model.PageResult;
import com.openquartz.easyevent.admin.model.BusEventEntity;
import com.openquartz.easyevent.admin.model.query.EventQuery;
import com.openquartz.easyevent.admin.service.EventAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Event Management", description = "API for managing events")
@RequiredArgsConstructor
public class EventController {

    private final EventAdminService eventAdminService;

    @GetMapping("/list")
    @Operation(summary = "List Events", description = "Paginated list of events with filtering")
    public PageResult<BusEventEntity> listEvents(EventQuery query) {
        return eventAdminService.pageEvents(query);
    }

    @PostMapping("/retry")
    @Operation(summary = "Retry Events", description = "Manually retry specific events")
    public void retryEvents(@RequestBody List<Long> eventIds) {
        eventAdminService.retryEvents(eventIds);
    }
}
