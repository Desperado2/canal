package com.alibaba.otter.canal.k2s.kafka.thread;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.k2s.kafka.consumer.BinlogConsumer;
import com.alibaba.otter.canal.k2s.starrocks.config.MappingConfig;
import com.alibaba.otter.canal.k2s.starrocks.service.StarrocksSyncService;
import com.alibaba.otter.canal.k2s.starrocks.support.Dml;
import com.alibaba.otter.canal.k2s.starrocks.support.StarRocksBufferData;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.utils.CopyOnWriteMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * kafka数据处理
 * @author mujingjing
 * @date 2024/2/22
 **/
public class KafkaMessageHandler implements Runnable{

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaMessageHandler.class);

    private final Map<String, StarRocksBufferData> buffer = new CopyOnWriteMap<>();
    private final int commitBatch;
    private final long commitTimeout;
    private final ScheduledExecutorService scheduler;
    private final Consumer<String> dataConsumer;
    private final StarrocksSyncService starrocksSyncService;
    private final Map<String, Map<String, MappingConfig>> mappingConfig;
    private final String taskId;

    public KafkaMessageHandler(int commitBatch, long commitTimeout,
                               Consumer<String> dataConsumer,
                               StarrocksSyncService starrocksSyncService, List<MappingConfig> mappingConfigList,
                               String taskId) {
        this.commitBatch = commitBatch;
        this.commitTimeout = commitTimeout;
        this.dataConsumer = dataConsumer;
        this.starrocksSyncService = starrocksSyncService;
        this.mappingConfig = transform(mappingConfigList);
        this.taskId = taskId;
        this.scheduler = Executors.newScheduledThreadPool(10);
        scheduleCommitTask();
        MDC.put("taskId", taskId);
        LOGGER.debug("初始化kafka数据消费缓存对象，commitBatch={}条， commitTimeout={}毫秒", commitBatch, commitTimeout);
        MDC.remove("task");
    }

    public void addRecord(ConsumerRecord<String, String> record) {
        scheduler.execute(() -> {
            synchronized (buffer) {
                List<Dml> dmlList = new ArrayList<>();
                String value = record.value();
                dmlList.add(JSONObject.toJavaObject(JSONObject.parseObject(value), Dml.class));
                // 转换
                Map<String, StarRocksBufferData> bufferDataMap = starrocksSyncService.transform(taskId, mappingConfig, dmlList);
                for (Map.Entry<String, StarRocksBufferData> dataEntry : bufferDataMap.entrySet()) {
                    if(buffer.containsKey(dataEntry.getKey())){
                        buffer.get(dataEntry.getKey()).getData().addAll(dataEntry.getValue().getData());
                    }else{
                        buffer.put(dataEntry.getKey(), dataEntry.getValue());
                    }
                }
                int dataCount = buffer.values().stream().map(it -> it.getData().size()).mapToInt(it -> it).sum();
                MDC.put("taskId", taskId);
                LOGGER.debug("kafka数据缓存数量:{}", dataCount);
                MDC.remove("task");
                if (dataCount >= commitBatch) {
                    MDC.put("taskId", taskId);
                    LOGGER.debug("kafka数据缓存数量:{}>{},提交数据", dataCount, commitBatch);
                    MDC.remove("task");
                    run(); // 达到提交条件，立即提交
                }
            }
        });
    }

    @Override
    public void run() {
        synchronized (buffer) {
            if (!buffer.isEmpty()) {
                starrocksSyncService.sync(taskId, new HashMap<>(buffer));
                dataConsumer.accept(taskId);
                buffer.clear();
                MDC.put("taskId", taskId);
                LOGGER.debug("成功提交数据，触发条件：数据量达标");
                MDC.remove("task");
            }
        }
    }

    private void scheduleCommitTask() {
        scheduler.scheduleAtFixedRate(() -> {
           try {
               synchronized (buffer) {
                   if (!buffer.isEmpty()) {
                       // 提交数据写kafka
                       starrocksSyncService.sync(taskId, new HashMap<>(buffer));
                       buffer.clear();
                   }
                   dataConsumer.accept(taskId);
                   // 提交偏移量
                   MDC.put("taskId", taskId);
                   LOGGER.debug("成功提交数据，触发条件：超时");
                   MDC.remove("task");
               }
           }catch (Exception e){
               LOGGER.error("定时任务执行异常", e);
           }
        }, 0, commitTimeout, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    private Map<String, Map<String, MappingConfig>> transform(List<MappingConfig> mappingConfigList){
        Map<String, Map<String, MappingConfig>> mapMap = new HashMap<>();
        for (MappingConfig mappingConfig : mappingConfigList) {
            String srcDatabase = mappingConfig.getSrcDatabase();
            String srcTable = mappingConfig.getSrcTable();
            if(mapMap.containsKey(srcDatabase)){
                mapMap.get(srcDatabase).put(srcTable, mappingConfig);
            }else{
                Map<String, MappingConfig> tableMap = new HashMap<>();
                tableMap.put(srcTable, mappingConfig);
                mapMap.put(srcDatabase, tableMap);
            }
        }
        return mapMap;
    }
}
