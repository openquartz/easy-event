ALTER TABLE ee_bus_event_entity ADD COLUMN start_execution_time TIMESTAMP DEFAULT NULL COMMENT '开始执行时间';
ALTER TABLE ee_bus_event_entity ADD COLUMN execution_success_time TIMESTAMP DEFAULT NULL COMMENT '执行成功时间';
