package com.alibaba.otter.canal.k2s.starrocks.support;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.k2s.config.ConsumerTaskConfig;
import com.alibaba.otter.canal.k2s.starrocks.config.MappingConfig;
import com.alibaba.otter.canal.k2s.starrocks.manager.StarRocksSinkBufferEntity;
import com.alibaba.otter.canal.k2s.starrocks.manager.StarRocksSinkManager;
import com.alibaba.otter.canal.k2s.starrocks.sink.StarRocksSinkOptions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StarrocksTemplate {
    private static final Logger logger  = LoggerFactory.getLogger(StarrocksTemplate.class);
    private ConsumerTaskConfig consumerTaskConfig;

    private StarRocksSinkManager starRocksSinkManager;

    public StarrocksTemplate(ConsumerTaskConfig consumerTaskConfig) {
        this.consumerTaskConfig = consumerTaskConfig;
    }

    public ConsumerTaskConfig getConsumerTaskConfig() {
        return consumerTaskConfig;
    }

    public void setConsumerTaskConfig(ConsumerTaskConfig consumerTaskConfig) {
        this.consumerTaskConfig = consumerTaskConfig;
    }

    public String delete(Map<String, Object> rowData) {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, Object> column : rowData.entrySet()) {
            if (column != null  && StringUtils.isNotEmpty(column.getKey())) {
                jsonObject.put(column.getKey(), column.getValue());
            }
        }
        jsonObject.put("__op", "1");
        return jsonObject.toJSONString();
    }

    public String upsert(Map<String, Object> rowData) {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, Object> column : rowData.entrySet()) {
            if (column != null  && StringUtils.isNotEmpty(column.getKey())) {
                jsonObject.put(column.getKey(), column.getValue());
            }
        }
        return jsonObject.toJSONString();
    }

    public void sink(String taskId, StarRocksBufferData bufferData) {
        if (bufferData == null || CollectionUtils.isEmpty(bufferData.getData())) {
            return;
        }

        MappingConfig srMapping = bufferData.getMappingConfig();
        String database = srMapping.getSrcDatabase();
        String table = srMapping.getSrcTable();
        String srDataBase = srMapping.getDstDatabase();
        String srTable = srMapping.getDstTable();
        MDC.put("taskId", taskId);
        logger.debug("taskId:{}, Sync table {}.{}", database, table, taskId);
        MDC.remove("taskId");
        List<String> columnList = srMapping.getContent().getColumns().stream()
                .map(MappingConfig.MappingData.ColumnMapping::getDstField).collect(Collectors.toList());
        List<String> dstPkList = srMapping.getContent().getDstPkList();
        StarRocksSinkOptions op = getStarRocksSinkOptions(taskId, srDataBase, srTable, columnList, dstPkList);

        StarRocksSinkBufferEntity bufferEntity = new StarRocksSinkBufferEntity(op.getDatabaseName(), op.getTableName(), null);
        bufferEntity.setBuffer((ArrayList<byte[]>) bufferData.getData());
        for (byte[] bts : bufferData.getData()) {
            bufferEntity.incBatchSize(bts.length);
        }

        try {
            if (bufferData.getData().size() != 0 && bufferEntity.getBatchSize() > 0) {
                MDC.put("taskId", taskId);
                logger.debug(String.format("StarRocks buffer Sinking triggered: db: [%s] table: [%s] rows[%d] label[%s].", database, table, bufferData.getData().size(), bufferEntity.getLabel()));
                MDC.remove("taskId");
                starRocksSinkManager.getStarrocksStreamLoadVisitor().doStreamLoad(taskId,bufferEntity);
            }
        }catch (Exception e) {
            MDC.put("taskId", taskId);
            logger.error("Sink table {}  data to StarRocks failed. The data is: {}  and  error message is: {}", database + "." + table, ConvertUtil.convertBytesToString(bufferEntity.getBuffer()), e.getMessage());
            MDC.remove("taskId");
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private StarRocksSinkOptions getStarRocksSinkOptions(String taskId, String srDataBase, String srTable,
                                                         List<String> columnList, List<String> dstPkList) {
        StarRocksSinkOptions op = StarRocksSinkOptions.builder()
                .withProperty("jdbc-url", this.consumerTaskConfig.getJdbcUrl()  + srDataBase  + "?useSSL=false&serverTimezone=GMT")
                .withProperty("load-url", this.consumerTaskConfig.getFeHost() + ":" + this.consumerTaskConfig.getFeHttpPort())
                .withProperty("username", this.consumerTaskConfig.getUserName())
                .withProperty("password",  this.consumerTaskConfig.getPassWord())
                .withProperty("table-name", srTable)
                .withProperty("database-name", srDataBase)
                .withProperty("sink.properties.format", "json")
                .withProperty("sink.properties.strip_outer_array", "true")
                .withProperty("sink.properties.max_filter_ratio", "0.2")
                .build();

        if (starRocksSinkManager == null) {
            starRocksSinkManager = new StarRocksSinkManager(taskId, op, columnList == null ? null :columnList.toArray(new String[0]), dstPkList);
        } else {
            starRocksSinkManager.getStarrocksStreamLoadVisitor().setSinkOptions(op);
        }
        return op;
    }
}
