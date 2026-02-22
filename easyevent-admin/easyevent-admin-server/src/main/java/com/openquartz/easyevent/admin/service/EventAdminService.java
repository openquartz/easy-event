package com.openquartz.easyevent.admin.service;

import com.openquartz.easyevent.admin.dao.BusEventDao;
import com.openquartz.easyevent.admin.model.BusEventDetail;
import com.openquartz.easyevent.admin.model.BusEventEntity;

import com.openquartz.easyevent.admin.model.PageResult;
import com.openquartz.easyevent.admin.model.query.EventQuery;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventAdminService {

    private final BusEventDao busEventDao;

    public PageResult<BusEventEntity> pageEvents(EventQuery query) {
        long total = busEventDao.count(query);
        if (total == 0) {
            return new PageResult<>(0, null, query.getPage(), query.getSize());
        }
        List<BusEventEntity> list = busEventDao.selectPage(query);
        return new PageResult<>(total, list, query.getPage(), query.getSize());
    }

    @Transactional(rollbackFor = Exception.class)
    public void retryEvents(List<Long> eventIds) {
        busEventDao.updateForRetry(eventIds);
        log.info("Reset events for retry: {}", eventIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateEvent(BusEventDetail eventDetail) {
        BusEventEntity entity = new BusEventEntity();
        BeanUtils.copyProperties(eventDetail, entity);
        busEventDao.update(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteEvent(Long eventId) {
        busEventDao.delete(eventId);
    }

    public BusEventDetail getEventDetail(Long eventId) {
        BusEventEntity event = busEventDao.findById(eventId);
        if (event == null) {
            return null;
        }

        BusEventDetail detail = new BusEventDetail();
        BeanUtils.copyProperties(event, detail);

        // Derive fields that are not in BusEventEntity
        detail.setTitle(event.getEventKey() != null ? event.getEventKey() : event.getClassName());
        detail.setMaxRetries(5); // Default value as it's not stored
        
        if (event.getStartExecutionTime() != null) {
            detail.setStartedTime(event.getStartExecutionTime());
        }

        // Estimated complete time is not currently tracked/predicted
        detail.setEstimatedCompleteTime(null);
        
        return detail;
    }
}
