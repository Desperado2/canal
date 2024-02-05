package com.alibaba.otter.canal.k2s;


import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类
 * @author mujingjing
 * @date 2024/2/4
 **/
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class K2SApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(K2SApplication.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.run(args);
    }
}
