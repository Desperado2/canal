package com.alibaba.otter.canal.k2s.kafka.helper;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * kafka配置信息 助手类
 * @author mujingjing
 * @date 2024/2/4
 **/
@Component
public class KafkaPropertiesHelper {

    private Properties kafkaProp;

    public synchronized Properties getKafkaProp(String groupId) {
        if(kafkaProp == null){
            initProperties(groupId);
        }
        return kafkaProp;
    }

    /**
     * 初始化properties
     */
    public void initProperties(String groupId){
        Properties properties = getGlobalConfig();
        // 获取kafka的配置信息
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka2");
        kafkaProp = properties;
    }

    /**
     * 设置公共参数
     * @return 参数
     */
    private Properties getGlobalConfig(){
        Properties properties = new Properties();
        // 不开启自动提交
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 100000);
        properties.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 110000);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        return properties;
    }
}
