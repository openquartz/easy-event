package org.svnee.easyevent.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.svnee.easyevent.starter.annotation.EnableEasyEventAutoConfiguration;

/**
 * EasyEventDisruptorExampleStarter
 *
 * @author svnee
 **/
@SpringBootApplication
@EnableEasyEventAutoConfiguration
public class EasyEventDisruptorExampleStarter {

    public static void main(String[] args) {
        SpringApplication.run(EasyEventDisruptorExampleStarter.class);
    }

}
