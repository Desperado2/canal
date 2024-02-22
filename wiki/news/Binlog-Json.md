# 背景

业务中有多个数据库实例，多个实例中存在同名数据库+同名表+自增主键id的数据。需要将其同步到一张StarRocks表中，需要保证数据一致。


# 设计理念

在获取binlog之后，格式化之时加入instance中配置的canal.instance.master.address属性值，以数据库的连接信息来标识数据的不同。在StarRocks中以（instance、database、table、pk）联合来作为主键，解决多实例的数据同步问题。

## 变化点
1.  binlog在转换为FlatMessage写入kafka时，加入instance字段。
```
{
    "data": [
        {
            "id": "169727",
            "product_id": "fe70b5bcf99f4b55a365905f61fc438d",
            "product_code": "000199",
            "product_name": "红韵600"
  
        }
    ],
    "database": "test",
    "es": 1708582515000,
    "gtid": "",
    "id": 78,
    "instance": "rm-xxxxxxxx.mysql.rds.aliyuncs.com:3306",
    "isDdl": false,
    "mysqlType": {
        "id": "bigint(20)",
        "product_id": "varchar(32)",
        "product_code": "varchar(100)",
        "product_name": "varchar(100)",
        "create_time": "datetime",
        "update_time": "datetime"
    },
    "old": null,
    "pkNames": [
        "id"
    ],
    "sql": "",
    "sqlType": {
        "id": -5,
        "product_id": 12,
        "product_code": 12,
        "product_name": 12,
        "create_time": 93,
        "update_time": 93
    },
    "table": "t_test",
    "ts": 1708582515452,
    "type": "INSERT"
}
```

