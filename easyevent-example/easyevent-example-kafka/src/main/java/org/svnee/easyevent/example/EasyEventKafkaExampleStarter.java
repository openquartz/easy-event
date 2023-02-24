package org.svnee.easyevent.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.svnee.easyevent.starter.annotation.EnableEasyEventAutoConfiguration;

/**
 * @author svnee
 **/
@SpringBootApplication
@EnableEasyEventAutoConfiguration
public class EasyEventKafkaExampleStarter {

    public static void main(String[] args) {
        SpringApplication.run(EasyEventKafkaExampleStarter.class);
    }
}
