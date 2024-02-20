# 版本信息

本文所研究的MySQL版本：
-->   MySQL 8.0.13  :  [https://github.com/mysql/mysql-server/releases/tag/mysql-8.0.13](https://github.com/mysql/mysql-server/releases/tag/mysql-8.0.13)

# 协议变化

### 1. PARTIAL\_UPDATE\_ROWS\_EVENT 新的binlog对象

MySQL 8.0.3之后新增了支持json局部更新的binlog事件(主要是基于性能考虑，在after事件中只会记录json的变更项，而不是完整镜像)，__目前canal已经完全支持partial事件的完整解析__
参考文档：[https://dev.mysql.com/doc/refman/8.0/en/replication-options-binary-log.html#sysvar\_binlog\_row\_value\_options](https://dev.mysql.com/doc/refman/8.0/en/replication-options-binary-log.html#sysvar_binlog_row_value_options)
```plain
# 关闭patital_json的事件
set binlog_row_value_options=""

# 开启patital_json的事件
set binlog_row_value_options="PARTIAL_JSON"

# 测试内容
CREATE TABLE `test_json` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `c_json` json DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='10000000';

insert into test_json values(1,'{"k1": "v1", "k2": 10}');
update test_json set c_json = JSON_SET(c_json, '$.k1', 'v1') where id = 1;
--> 输出：JSON_REPLACE(@1, $.k1, "v2")

update test_json set c_json = JSON_SET(c_json, '$.k3', 'k3') where id = 1;
--> 输出：JSON_INSERT(@1, $.k3, "k3")

update test_json set c_json = JSON_REPLACE(c_json, '$.k3', CAST('[1,2]' AS JSON )) where id = 1;
--> 输出：JSON_REPLACE(@1, $.k3, [1, 2])

update test_json set c_json = JSON_REMOVE(c_json, '$.k4') where id = 1;
--> 输出：JSON_REMOVE(@1, $.k4)

update test_json set c_json = JSON_SET(c_json, '$.k4', CAST('[1,2]' AS JSON )) where id = 1;
--> 输出：JSON_INSERT(@1, $.k4, [1, 2])


insert into test_json values(2,'["a", {"b": [1, 2]}');
update test_json set c_json = JSON_SET(c_json, '$[1]', 'v2') where id = 2;
--> 输出：JSON_REPLACE(@1, $[1], "v1")

update test_json set c_json = JSON_SET(c_json, '$[2]', 'v2' , '$[3]', 'v2') where id = 2;
--> 输出：JSON_ARRAY_INSERT(@1, $[2], "v2", $[3], "v2")


```


### 2. QueryLogEvent 新增变量解析 ，需要修改QueryLogEvent.unpackVariables()方法，处理8.0新增的变量
```plain
    /**
     * The variable carries xid info of 2pc-aware (recoverable) DDL queries.
     */
    public static final int Q_DDL_LOGGED_WITH_XID             = 17;
    /**
     * This variable stores the default collation for the utf8mb4 character set.
     * Used to support cross-version replication.
     */
    public static final int Q_DEFAULT_COLLATION_FOR_UTF8MB4   = 18;

    /**
     * Replicate sql_require_primary_key.
     */
    public static final int Q_SQL_REQUIRE_PRIMARY_KEY         = 19;
```

### 3. binlog\_row\_metadata=FULL特性支持
MySQL8.0.1版本之后新增在binlog里记录更多的column metadata信息，比如列名、主键、编码、SET/ENUM/GEO类型等信息，默认是MINIMAL信息 (只记录基本的type/meta/unsigned等信息)

参考文档：[https://dev.mysql.com/doc/refman/8.0/en/replication-options-binary-log.html#sysvar\_binlog\_row\_metadata](https://dev.mysql.com/doc/refman/8.0/en/replication-options-binary-log.html#sysvar_binlog_row_metadata)
```plain
# 开启metadata全记录
set @@global.binlog_row_metadata='FULL';
```

考虑binlog记录的信息，相比于canal之前定义的表元数据有缺失，比如uk/mysql type信息，即使开启binlog\_row\_metadata=FULL模式，更多是和TableMetaTSDB得到的元数据进行强校验.


![image.png | left | 747x254](https://cdn.nlark.com/lark/0/2018/png/5565/1541401197160-2d0e6b5e-4b65-490e-8849-003d6d527186.png "")

