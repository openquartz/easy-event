package com.openquartz.easyevent.admin.controller;

import com.openquartz.easyevent.admin.annotation.Permission;
import com.openquartz.easyevent.admin.annotation.RateLimit;
import com.openquartz.easyevent.admin.model.BusEventDetail;
import com.openquartz.easyevent.admin.model.BusEventEntity;
import com.openquartz.easyevent.admin.model.PageResult;
import com.openquartz.easyevent.admin.model.query.EventQuery;
import com.openquartz.easyevent.admin.model.vo.BusEventDetailVO;

import com.openquartz.easyevent.admin.model.vo.BusEventVO;
import com.openquartz.easyevent.admin.service.EventAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    public PageResult<BusEventVO> listEvents(EventQuery query) {
        PageResult<BusEventEntity> result = eventAdminService.pageEvents(query);
        List<BusEventVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), voList, result.getPage(), result.getSize());
    }

    @GetMapping("/{eventId}/details")
    @Operation(summary = "Get Event Details", description = "Get detailed information of a single event")
    @RateLimit(limit = 10)
    @Permission
    public BusEventDetailVO getEventDetails(@PathVariable Long eventId) {
        BusEventDetail event = eventAdminService.getEventDetail(eventId);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }
        return convertToDetailVO(event);
    }

    @PutMapping("/{eventId}")
    @Operation(summary = "Update Event", description = "Update event details")
    @Permission
    public void updateEvent(@PathVariable Long eventId, @RequestBody BusEventDetail eventDetail) {
        eventDetail.setId(eventId);
        eventAdminService.updateEvent(eventDetail);
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary = "Delete Event", description = "Delete an event")
    @Permission
    public void deleteEvent(@PathVariable Long eventId) {
        eventAdminService.deleteEvent(eventId);
    }

    @PostMapping("/retry")
    @Operation(summary = "Retry Events", description = "Manually retry specific events")
    public void retryEvents(@RequestBody List<Long> eventIds) {
        eventAdminService.retryEvents(eventIds);
    }

    private BusEventVO convertToVO(BusEventEntity entity) {
        BusEventVO vo = new BusEventVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private BusEventDetailVO convertToDetailVO(BusEventDetail detail) {
        BusEventDetailVO vo = new BusEventDetailVO();
        BeanUtils.copyProperties(detail, vo);
        return vo;
    }
}
