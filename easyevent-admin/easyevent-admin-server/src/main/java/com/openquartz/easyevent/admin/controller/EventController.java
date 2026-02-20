package com.openquartz.easyevent.admin.controller;

import com.openquartz.easyevent.admin.annotation.Permission;
import com.openquartz.easyevent.admin.annotation.RateLimit;
import com.openquartz.easyevent.admin.model.BusEventDetail;
import com.openquartz.easyevent.admin.model.BusEventEntity;
import com.openquartz.easyevent.admin.model.PageResult;
import com.openquartz.easyevent.admin.model.query.EventQuery;
import com.openquartz.easyevent.admin.service.EventAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping("/{eventId}/details")
    @Operation(summary = "Get Event Details", description = "Get detailed information of a single event")
    @RateLimit(limit = 10)
    @Permission
    public BusEventDetail getEventDetails(@PathVariable Long eventId) {
        BusEventDetail event = eventAdminService.getEventDetail(eventId);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }
        return event;
    }

    @PostMapping("/retry")
    @Operation(summary = "Retry Events", description = "Manually retry specific events")
    public void retryEvents(@RequestBody List<Long> eventIds) {
        eventAdminService.retryEvents(eventIds);
    }
}
