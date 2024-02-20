package com.alibaba.otter.canal.k2s.cache;


import com.alibaba.otter.canal.k2s.monitor.ConsumerTaskConfigMonitor;
import com.alibaba.otter.canal.k2s.utils.LruHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class TaskRestartCache {

    private static final Logger log = LoggerFactory.getLogger(TaskRestartCache.class);


    private static final int MAX_CACHE_SIZE = 1024;

    final ScheduledExecutorService cacheExpireCheckExecutorService;

    /**
     * 过期检测周期
     */
    private static final long CHECK_TIME_SECOND = 60 * 1000L;

    private final LruHashMap<String,String> cacheMap = new LruHashMap<>(MAX_CACHE_SIZE);
    private final Map<String,Long> expiresTime = new ConcurrentHashMap<>();

    @Autowired
    @Lazy
    private ConsumerTaskConfigMonitor consumerTaskConfigMonitor;

    /**
     * 过期处理
     */
    private final Runnable expireCheckTask = new Runnable() {
        @Override
        public void run() {
            Set<String> keys = expiresTime.keySet();
            for (String key : keys){
                if (expiresTime.get(key) <= System.currentTimeMillis()){
                    synchronized (this){
                        consumerTaskConfigMonitor.mergeConfigByTaskId(cacheMap.get(key));
                        expiresTime.remove(key);
                        cacheMap.remove(key);
                        log.debug("Automatic cache expiration:{}",key);
                    }
                }
            }
        }
    };

    public TaskRestartCache(ScheduledExecutorService cacheExpireCheckExecutorService) {
        this.cacheExpireCheckExecutorService = cacheExpireCheckExecutorService;
    }


    @PostConstruct
    public void init(){
        cacheExpireCheckExecutorService.scheduleAtFixedRate(expireCheckTask, CHECK_TIME_SECOND, CHECK_TIME_SECOND,
                TimeUnit.MILLISECONDS);
    }

    public void set(String cacheKey, String value, Long cacheTime) {
        synchronized (this){
            this.cacheMap.put(cacheKey,value);
            this.expiresTime.put(cacheKey,System.currentTimeMillis()+cacheTime * 1000);
        }
    }

    public String get(String cacheKey) {
        return this.cacheMap.get(cacheKey);
    }


    public void clear() {
        synchronized (this){
            this.cacheMap.clear();
            this.expiresTime.clear();
        }
    }
}
