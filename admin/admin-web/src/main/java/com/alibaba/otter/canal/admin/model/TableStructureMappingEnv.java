package com.alibaba.otter.canal.admin.model;

import io.ebean.Finder;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 * 表结构环境
 * @author mujingjing
 * @date 2024/2/2
 **/
@Entity
@Table(name = "canal_table_structure_mapping_env")
public class TableStructureMappingEnv extends Model {

    public static final TableStructureMappingEnvFinder find = new TableStructureMappingEnvFinder();

    public static class TableStructureMappingEnvFinder extends Finder<Long, TableStructureMappingEnv> {

        /**
         * Construct using the default EbeanServer.
         */
        public TableStructureMappingEnvFinder(){
            super(TableStructureMappingEnv.class);
        }

    }

    @Id
    private Long   id;
    private String envCode;
    private String envName;
    private String description;

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

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
