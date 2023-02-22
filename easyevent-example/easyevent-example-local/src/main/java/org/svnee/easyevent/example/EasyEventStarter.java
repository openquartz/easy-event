package org.svnee.easyevent.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.svnee.easyevent.starter.annotation.EnableEasyEventAutoConfiguration;

/**
 * EasyEventStarter
 *
 * @author svnee
 **/
@SpringBootApplication
@EnableEasyEventAutoConfiguration
public class EasyEventStarter {

    public static void main(String[] args) {
        SpringApplication.run(EasyEventStarter.class);
    }

}
