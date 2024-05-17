package com.dlut.crazychat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CrazyChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrazyChatApplication.class, args);  //test
    }

}
