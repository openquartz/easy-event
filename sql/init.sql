CREATE TABLE ee_bus_event_entity
(
    id                        BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'eventId',
    app_id                    VARCHAR(50)  NOT NULL DEFAULT '' COMMENT 'appId',
    source_id                 BIGINT (20) NOT NULL DEFAULT 0 COMMENT 'sourceId',
    class_name                VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'Event-Class',
    error_count               TINYINT (3) NOT NULL DEFAULT 0 COMMENT '执行错误次数',
    processing_state          VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '执行状态',
    successful_subscriber     VARCHAR(512) NOT NULL DEFAULT '' COMMENT '执行成功的订阅者',
    trace_id                  VARCHAR(50)  NOT NULL DEFAULT '' COMMENT 'traceId',
    event_data                TEXT         NOT NULL COMMENT 'EventData',
    event_key                 VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'EventKey',
    creating_owner            VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '创建者机器',
    processing_owner          VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '生产者机器',
    processing_available_date TIMESTAMP             DEFAULT NULL COMMENT '执行有效时间',
    processing_failed_reason  VARCHAR(128) NOT NULL DEFAULT '' COMMENT '已经执行失败的原因',
    created_time              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX                     idx_event_key (event_key),
    INDEX                     idx_app_state_owner_time(app_id, processing_state, processing_owner, created_time)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'ee_bus_event_entity';
