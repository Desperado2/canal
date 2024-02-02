package com.alibaba.otter.canal.admin.model;

import io.ebean.Finder;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

/**
 * 表结构映射
 * @author mujingjing
 * @date 2024/2/2
 **/
@Entity
@Table(name = "canal_table_structure_mapping")
public class TableStructureMapping extends Model {

    public static final TableStructureMappingFinder find = new TableStructureMappingFinder();

    public static class TableStructureMappingFinder extends Finder<Long, TableStructureMapping> {

        /**
         * Construct using the default EbeanServer.
         */
        public TableStructureMappingFinder(){
            super(TableStructureMapping.class);
        }

    }

    @Id
    private Long   id;
    private String envCode;
    private String srcDatabase;
    private String srcTable;
    private String dstDatabase;
    private String dstTable;
    private String content;
    private String contentMd5;

    @WhenModified
    private Date modifiedTime;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentMd5() {
        return contentMd5;
    }

    public void setContentMd5(String contentMd5) {
        this.contentMd5 = contentMd5;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}
