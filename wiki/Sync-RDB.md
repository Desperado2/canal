# 背景

canal 1.1.1版本之后, 内置增加客户端数据同步功能, Client适配器整体介绍: [[ClientAdapter]]

# RDB适配器

RDB adapter 用于适配mysql到任意关系型数据库(需支持jdbc)的数据同步及导入
测试支持的数据库列表:
1. MySQL
2. Oracle
3. Postgress
4. SQLServer
5. ...

## 1 修改启动器配置: application.yml, 这里以oracle目标库为例
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
      - name: rdb                                               # 指定为rdb类型同步
        key: oracle1                                            # 指定adapter的唯一key, 与表映射配置中outerAdapterKey对应
        properties:
          jdbc.driverClassName: oracle.jdbc.OracleDriver        # jdbc驱动名, 部分jdbc的jar包需要自行放致lib目录下
          jdbc.url: jdbc:oracle:thin:@localhost:49161:XE        # jdbc url
          jdbc.username: mytest                                 # jdbc username
          jdbc.password: m121212                                # jdbc password
          threads: 5                                            # 并行执行的线程数, 默认为1
```
注意点:
1. 其中 outAdapter 的配置: name统一为rdb, key为对应的数据源的唯一标识需和下面的表映射文件中的outerAdapterKey对应, properties为目标库jdb的相关参数
2. adapter将会自动加载 conf/rdb 下的所有.yml结尾的表映射配置文件

## 2 RDB表映射文件

修改 conf/rdb/mytest_user.yml文件:
```
dataSourceKey: defaultDS        # 源数据源的key, 对应上面配置的srcDataSources中的值
destination: example            # cannal的instance或者MQ的topic
groupId:                        # 对应MQ模式下的groupId, 只会同步对应groupId的数据
outerAdapterKey: oracle1        # adapter key, 对应上面配置outAdapters中的key
concurrent: true                # 是否按主键hash并行同步, 并行同步的表必须保证主键不会更改及主键不能为其他同步表的外键!!
dbMapping:
  database: mytest              # 源数据源的database/shcema
  table: user                   # 源数据源表名
  targetTable: mytest.tb_user   # 目标数据源的库名.表名
  targetPk:                     # 主键映射
    id: id                      # 如果是复合主键可以换行映射多个
#  mapAll: true                 # 是否整表映射, 要求源表和目标表字段名一模一样 (如果targetColumns也配置了映射,则以targetColumns配置为准)
  targetColumns:                # 字段映射, 格式: 目标表字段: 源表字段, 如果字段名一样源表字段名可不填
    id:
    name:
    role_id:
    c_time:
    test1: 
```
导入的类型以目标表的元类型为准, 将自动进行类型转换

## 3 Mysql 库间镜像schema DDL DML同步

修改 application.yml:
```
canalAdapters:
- instance: example # canal instance Name or mq topic name
groups:
- groupId: g1
    outerAdapters:
    - name: rdb
    key: mysql1
    properties:
        jdbc.driverClassName: com.mysql.jdbc.Driver
        jdbc.url: jdbc:mysql://192.168.0.36/mytest?useUnicode=true
        jdbc.username: root
        jdbc.password: 121212
```

修改 conf/rdb/mytest_user.yml文件:
```
dataSourceKey: defaultDS
destination: example
outerAdapterKey: mysql1
concurrent: true
dbMapping:
  mirrorDb: true
  database: mytest
```
其中dbMapping.database的值代表源库和目标库的schema名称，即两库的schema要一模一样

## 4 RDB启动

* 将目标库的jdbc jar包放入lib文件夹, 这里放入ojdbc6.jar (如果是其他数据库则放入对应的驱动)
* 启动canal-adapter启动器
```
bin/startup.sh
```
* 验证
修改mysql mytest.user表的数据, 将会自动同步到Oracle的MYTEST.TB_USER表下面, 并会打出DML的log