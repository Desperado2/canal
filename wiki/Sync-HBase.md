# 背景
canal 1.1.1版本之后, 内置增加客户端数据同步功能, Client适配器整体介绍: [[ClientAdapter]]

# HBase适配器
## 1. 修改启动器配置: application.yml
```
canal.conf:
  canalServerHost: 127.0.0.1:11111
  batchSize: 500
  syncBatchSize: 1000
  retries: 0
  timeout:
  mode: tcp # kafka rocketMQ
  srcDataSources:
    defaultDS:
      url: jdbc:mysql://127.0.0.1:3306/mytest?useUnicode=true
      username: root
      password: 121212
  canalAdapters:
  - instance: example # canal instance Name or mq topic name
    groups:
    - groupId: g1
      outerAdapters:
      - name: hbase
        properties:
          hbase.zookeeper.quorum: 127.0.0.1
          hbase.zookeeper.property.clientPort: 2181
          zookeeper.znode.parent: /hbase
```
注意：adapter将会自动加载 conf/hbase 下的所有.yml结尾的配置文件

## 2. HBase表映射文件

修改 conf/hbase/mytest_person.yml文件:
```
dataSourceKey: defaultDS            # 对应application.yml中的datasourceConfigs下的配置
destination: example                # 对应tcp模式下的canal instance或者MQ模式下的topic
groupId:                            # 对应MQ模式下的groupId, 只会同步对应groupId的数据
hbaseMapping:                       # mysql--HBase的单表映射配置
  mode: STRING                      # HBase中的存储类型, 默认统一存为String, 可选: #PHOENIX  #NATIVE   #STRING 
                                    # NATIVE: 以java类型为主, PHOENIX: 将类型转换为Phoenix对应的类型
  destination: example              # 对应 canal destination/MQ topic 名称
  database: mytest                  # 数据库名/schema名
  table: person                     # 表名
  hbaseTable: MYTEST.PERSON         # HBase表名
  family: CF                        # 默认统一Column Family名称
  uppercaseQualifier: true          # 字段名转大写, 默认为true
  commitBatch: 3000                 # 批量提交的大小, ETL中用到
  #rowKey: id,type                  # 复合字段rowKey不能和columns中的rowKey并存
                                    # 复合rowKey会以 '|' 分隔
  columns:                          # 字段映射, 如果不配置将自动映射所有字段, 
                                    # 并取第一个字段为rowKey, HBase字段名以mysql字段名为主
    id: ROWKE                       
    name: CF:NAME
    email: EMAIL                    # 如果column family为默认CF, 则可以省略
    type:                           # 如果HBase字段和mysql字段名一致, 则可以省略
    c_time: 
    birthday: 
```

注意: 如果涉及到类型转换,可以如下形式:
```
...
  columns:                         
    id: ROWKE$STRING                      
    ...                   
    type: TYPE$BYTE                          
    ...
```

类型转换涉及到Java类型和Phoenix类型两种, 分别定义如下:
```
#Java 类型转换, 对应配置 mode: NATIVE
$DEFAULT
$STRING
$INTEGER
$LONG
$SHORT
$BOOLEAN
$FLOAT
$DOUBLE
$BIGDECIMAL
$DATE
$BYTE
$BYTES
```
```
#Phoenix 类型转换, 对应配置 mode: PHOENIX
$DEFAULT                  对应PHOENIX里的VARCHAR
$UNSIGNED_INT             对应PHOENIX里的UNSIGNED_INT           4字节
$UNSIGNED_LONG            对应PHOENIX里的UNSIGNED_LONG          8字节
$UNSIGNED_TINYINT         对应PHOENIX里的UNSIGNED_TINYINT       1字节
$UNSIGNED_SMALLINT        对应PHOENIX里的UNSIGNED_SMALLINT      2字节
$UNSIGNED_FLOAT           对应PHOENIX里的UNSIGNED_FLOAT         4字节
$UNSIGNED_DOUBLE          对应PHOENIX里的UNSIGNED_DOUBLE        8字节
$INTEGER                  对应PHOENIX里的INTEGER                4字节
$BIGINT                   对应PHOENIX里的BIGINT                 8字节
$TINYINT                  对应PHOENIX里的TINYINT                1字节
$SMALLINT                 对应PHOENIX里的SMALLINT               2字节
$FLOAT                    对应PHOENIX里的FLOAT                  4字节
$DOUBLE                   对应PHOENIX里的DOUBLE                 8字节
$BOOLEAN                  对应PHOENIX里的BOOLEAN                1字节
$TIME                     对应PHOENIX里的TIME                   8字节
$DATE                     对应PHOENIX里的DATE                   8字节
$TIMESTAMP                对应PHOENIX里的TIMESTAMP              12字节
$UNSIGNED_TIME            对应PHOENIX里的UNSIGNED_TIME          8字节
$UNSIGNED_DATE            对应PHOENIX里的UNSIGNED_DATE          8字节
$UNSIGNED_TIMESTAMP       对应PHOENIX里的UNSIGNED_TIMESTAMP     12字节
$VARCHAR                  对应PHOENIX里的VARCHAR                动态长度
$VARBINARY                对应PHOENIX里的VARBINARY              动态长度
$DECIMAL                  对应PHOENIX里的DECIMAL                动态长度
```
如果不配置将以java对象原生类型默认映射转换

## 3. 启动HBase数据同步
### 3.1 创建HBase表

在HBase shell中运行:
```
create 'MYTEST.PERSON', {NAME=>'CF'}
```
### 3.2 启动canal-adapter启动器

```
bin/startup.sh
```

### 3.4 验证
修改mysql mytest.person表的数据, 将会自动同步到HBase的MYTEST.PERSON表下面, 并会打出DML的log