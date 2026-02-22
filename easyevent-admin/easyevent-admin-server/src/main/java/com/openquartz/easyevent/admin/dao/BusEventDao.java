package com.openquartz.easyevent.admin.dao;

import com.openquartz.easyevent.admin.model.BusEventEntity;
import com.openquartz.easyevent.admin.model.query.EventQuery;
import java.util.List;
import java.util.Map;



public interface BusEventDao {
    
    long count(EventQuery query);
    
    List<BusEventEntity> selectPage(EventQuery query);
    
    void updateForRetry(List<Long> ids);
    
    List<Map<String, Object>> countByState();
    
    List<Map<String, Object>> countTrendLast24Hours();
    
    List<Map<String, Object>> subscriberLatencyRanking();

    BusEventEntity findById(Long id);

    void update(BusEventEntity entity);

    void delete(Long id);


}