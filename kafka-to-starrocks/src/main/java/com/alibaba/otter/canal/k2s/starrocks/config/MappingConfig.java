package com.alibaba.otter.canal.k2s.starrocks.config;


import java.util.List;

/**
 * StarRocks表映射配置
 */
public class MappingConfig  {

    private Long   id;
    private String envCode;
    private String srcDatabase;
    private String srcTable;
    private String dstDatabase;
    private String dstTable;
    private MappingData mappingData;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEnvCode() {
        return envCode;
    }

    public void setEnvCode(String envCode) {
        this.envCode = envCode;
    }

    public String getSrcDatabase() {
        return srcDatabase;
    }

    public void setSrcDatabase(String srcDatabase) {
        this.srcDatabase = srcDatabase;
    }

    public String getSrcTable() {
        return srcTable;
    }

    public void setSrcTable(String srcTable) {
        this.srcTable = srcTable;
    }

    public String getDstDatabase() {
        return dstDatabase;
    }

    public void setDstDatabase(String dstDatabase) {
        this.dstDatabase = dstDatabase;
    }

    public String getDstTable() {
        return dstTable;
    }

    public void setDstTable(String dstTable) {
        this.dstTable = dstTable;
    }

    public MappingData getMappingData() {
        return mappingData;
    }

    public void setMappingData(MappingData mappingData) {
        this.mappingData = mappingData;
    }

    public static class MappingData{
        private List<ColumnMapping> columnMappingList;
        private List<String> dstPkList;
        private String deleteStrategy;
        private String deleteUpdateField;
        private String deleteUpdateValue;
        private List<String> needType;

        public List<ColumnMapping> getColumnMappingList() {
            return columnMappingList;
        }

        public void setColumnMappingList(List<ColumnMapping> columnMappingList) {
            this.columnMappingList = columnMappingList;
        }

        public List<String> getDstPkList() {
            return dstPkList;
        }

        public void setDstPkList(List<String> dstPkList) {
            this.dstPkList = dstPkList;
        }

        public String getDeleteStrategy() {
            return deleteStrategy;
        }

        public void setDeleteStrategy(String deleteStrategy) {
            this.deleteStrategy = deleteStrategy;
        }

        public String getDeleteUpdateField() {
            return deleteUpdateField;
        }

        public void setDeleteUpdateField(String deleteUpdateField) {
            this.deleteUpdateField = deleteUpdateField;
        }

        public String getDeleteUpdateValue() {
            return deleteUpdateValue;
        }

        public void setDeleteUpdateValue(String deleteUpdateValue) {
            this.deleteUpdateValue = deleteUpdateValue;
        }

        public List<String> getNeedType() {
            return needType;
        }

        public void setNeedType(List<String> needType) {
            this.needType = needType;
        }

        public static class ColumnMapping{
            private String srcField;
            private String dstField;

            public String getSrcField() {
                return srcField;
            }

            public void setSrcField(String srcField) {
                this.srcField = srcField;
            }

            public String getDstField() {
                return dstField;
            }

            public void setDstField(String dstField) {
                this.dstField = dstField;
            }
        }
    }
}
