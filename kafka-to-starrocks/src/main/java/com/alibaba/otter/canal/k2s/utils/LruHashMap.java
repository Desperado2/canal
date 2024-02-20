package com.alibaba.otter.canal.k2s.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mujingjing
 */
public class LruHashMap<String,Object> extends LinkedHashMap<String,Object> {

    private Integer maxCacheSize = null;

    private LruHashMap(){

    }

    public LruHashMap(Integer maxCacheSize){
        this.maxCacheSize = maxCacheSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > maxCacheSize;
    }

}
