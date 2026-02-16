package com.jk.finice.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);

        System.out.println("""
            
            ╔════════════════════════════════════════════════════════════╗
            ║                 EUREKA SERVER STARTED                      ║
            ╠════════════════════════════════════════════════════════════╣
            ║  Dashboard:        http://localhost:8761                   ║
            ║  Service Registry: http://localhost:8761/eureka/apps       ║
            ║  Actuator Health:  http://localhost:8761/actuator/health   ║
            ╠════════════════════════════════════════════════════════════╣
            ║  Waiting for services to register...                       ║
            ║                                                            ║
            ╚════════════════════════════════════════════════════════════╝
            
            """);
    }

}