package org.svnee.easyevent.starter.spring.boot.autoconfig.property;

import lombok.Data;
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

    public static class JdbcTableProperties {

        private String prefix = "ee";

        /**
         * 总的分片数。小于0不开启分表。大于0则为开启分表数表后缀为[0,totalSharding)范围内
         */
        private int totalSharding = -1;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public int getTotalSharding() {
            return totalSharding;
        }

        public void setTotalSharding(int totalSharding) {
            this.totalSharding = totalSharding;
        }
    }

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

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

}
