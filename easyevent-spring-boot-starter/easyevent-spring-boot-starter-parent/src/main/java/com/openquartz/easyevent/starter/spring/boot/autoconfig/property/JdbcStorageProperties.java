package com.openquartz.easyevent.starter.spring.boot.autoconfig.property;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JdbcStorage properties
 *
 * @author svnee
 **/
@Data
@ConfigurationProperties(prefix = JdbcStorageProperties.PREFIX)
public class JdbcStorageProperties {

    public static final String PREFIX = "easyevent.storage.jdbc";

    /**
     * 表名前缀
     */
    private JdbcTableProperties table = new JdbcTableProperties();

    /**
     * datasource
     */
    private JdbcDatasourceProperties datasource = new JdbcDatasourceProperties();

    @Data
    public static class JdbcTableProperties {

        private String prefix = "ee";

        /**
         * 总的分片数。小于0不开启分表。大于0则为开启分表数表后缀为[0,totalSharding)范围内
         */
        private int totalSharding = -1;

        @Override
        public String toString() {
            return "JdbcTableProperties{" +
                "prefix='" + prefix + '\'' +
                ", totalSharding=" + totalSharding +
                '}';
        }
    }

    @Data
    public static class JdbcDatasourceProperties {

        /**
         * type
         */
        private String type = "org.apache.tomcat.jdbc.pool.DataSource";

        /**
         * jdbcUrl
         */
        private String url;

        /**
         * driver-class-name
         */
        private String driverClassName;

        /**
         * username
         */
        private String username;

        /**
         * password
         */
        private String password;

        @Override
        public String toString() {
            return "JdbcDatasourceProperties{" +
                "type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", driverClassName='" + driverClassName + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
        }
    }

}
