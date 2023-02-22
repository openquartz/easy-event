/**
 * Springboot AutoConfig order
 * 1、{@link org.svnee.easyevent.starter.spring.boot.autoconfig.EasyEventCommonAutoConfiguration}
 * 2、{@link org.svnee.easyevent.starter.spring.boot.autoconfig.EasyEventStorageAutoConfiguration}
 * 3、{@link org.svnee.easyevent.starter.spring.boot.autoconfig.JdbcStorageAutoConfiguration}
 * 4、{@link org.svnee.easyevent.starter.spring.boot.autoconfig.EasyEventCreatorAutoConfiguration}
 * 5、{@link org.svnee.easyevent.starter.spring.boot.autoconfig.EasyEventTransferAutoConfiguration}
 * 6、{@link org.svnee.easyevent.starter.spring.boot.autoconfig.DisruptorTransferAutoConfiguration}
 * 7、{@link org.svnee.easyevent.starter.spring.boot.autoconfig.RocketMqTransferAutoConfiguration}
 * 8、{@link org.svnee.easyevent.starter.spring.boot.autoconfig.EasyEventAfterAutoConfiguration}
 */
package org.svnee.easyevent.starter.spring.boot.autoconfig;