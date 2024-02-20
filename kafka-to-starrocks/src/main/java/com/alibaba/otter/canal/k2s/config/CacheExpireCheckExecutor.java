package com.alibaba.otter.canal.k2s.config;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 缓存过期检测线程
 * @author mujingjing
 * @date 2023/10/29
 **/
@Component
public class CacheExpireCheckExecutor {

    @Bean("cacheExpireCheckExecutorService")
    public ScheduledExecutorService cacheExpireCheckExecutorService(){
        return Executors.newScheduledThreadPool(5,
                new ThreadFactoryBuilder().setNameFormat("cache-expire-check-pool-%d").build());
    }
}
