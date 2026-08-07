package com.openquartz.easyevent.starter.spring.boot.autoconfig;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

public class JdbcStorageAutoConfigurationTest {

    @Test
    public void jdbcStorageJdbcTemplate_shouldBeConditionalOnMissingBeanByName() throws Exception {
        Method method = JdbcStorageAutoConfiguration.class.getMethod(
            "jdbcStorageJdbcTemplate",
            com.openquartz.easyevent.starter.spring.boot.autoconfig.property.JdbcStorageProperties.class,
            org.springframework.core.env.Environment.class);

        ConditionalOnMissingBean conditional = method.getAnnotation(ConditionalOnMissingBean.class);
        assertNotNull(conditional);
        assertArrayEquals(new String[]{"jdbcStorageJdbcTemplate"}, conditional.name());
        assertEquals(0, conditional.value().length);

        Bean bean = method.getAnnotation(Bean.class);
        assertNotNull(bean);
        assertArrayEquals(new String[]{"jdbcStorageJdbcTemplate"}, bean.name());
    }
}
