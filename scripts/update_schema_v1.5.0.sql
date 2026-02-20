CREATE TABLE ee_bus_event_history (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    entity_id BIGINT(20) NOT NULL COMMENT 'Event ID',
    status VARCHAR(50) NOT NULL DEFAULT '' COMMENT 'Status',
    context TEXT COMMENT 'Context/Description',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    PRIMARY KEY (id),
    INDEX idx_entity_id (entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Event Status History';
