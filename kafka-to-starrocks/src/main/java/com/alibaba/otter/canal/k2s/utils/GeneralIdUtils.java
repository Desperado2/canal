package com.alibaba.otter.canal.k2s.utils;


import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

/**
 * id生成
 * @author mujingjing
 * @date 2024/2/17
 **/
public class GeneralIdUtils {



    public static String generateId() {
        // 使用nanoid生成一个长度为20的字符串
         return NanoIdUtils.randomNanoId();
    }
}
