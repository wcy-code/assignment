package com.wcy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer /* @EnableEurekaServer注解表示这是一个Eureka服务*/
public class EServer {
    public static void main(String[] args) {
        SpringApplication.run(EServer.class,args);
    }
}
