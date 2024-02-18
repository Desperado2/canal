<template>
  <div class="app-container">
    <el-collapse v-model="activeNames" @change="handleChange">
      <el-collapse-item  name="1" class="line-text">
        <template slot="title">
          <div class="line_header">表的正则配置</div>
        </template>
        <div style="font: 16px bold; color: red">表的正则配置说明如下：</div>
        <div>mysql 数据解析关注的表，Perl正则表达式.</div>
        <div>多个正则之间以逗号(,)分隔，转义符需要双斜杠(\\).</div>
        <div>常见例子：</div>
        <el-table
          :data="tableRegexInfo"
          stripe
          show-header="false"
          style="width: 100%">
          <el-table-column
            prop="name"
            width="200px"
            label="表达式">
          </el-table-column>
          <el-table-column
            prop="desc"
            label="说明">
          </el-table-column>
        </el-table>

        <div></div>
        <div>详细参考文档:<a href="https://github.com/alibaba/canal/wiki/AdminGuide" target="_blank" style="color: #1482f0">Github文档</a> </div>
      </el-collapse-item>
      <el-collapse-item title="" name="2">
        <template slot="title">
          <div class="line_header">KAFKA消息动态分区路由</div>
        </template>
        <div style="font: 16px bold; color: red">KAFKA消息动态分区路由说明如下：</div>
        <div>支持配置格式：schema.table:pk1^pk2，多个配置之间使用逗号分隔</div>
        <div>常见例子：</div>
        <el-table
          :data="partitionHashInfo"
          stripe
          show-header="false"
          style="width: 100%">
          <el-table-column
            prop="name"
            width="200px"
            label="表达式">
          </el-table-column>
          <el-table-column
            prop="desc"
            label="说明">
          </el-table-column>
        </el-table>
        <div></div>
        <div>详细参考文档:<a href="https://github.com/alibaba/canal/wiki/Canal-Kafka-RocketMQ-QuickStart" target="_blank" style="color: #1482f0">Github文档</a> </div>
      </el-collapse-item>
      <el-collapse-item title="" name="3">
        <template slot="title">
          <div class="line_header">数据消费实例配置</div>
        </template>
        <div style="font: 16px bold; color: red">数据消费实例的配置说明如下：</div>
        <el-table
          :data="consumerInfo"
          stripe
          show-header="false"
          style="width: 100%">
          <el-table-column
            prop="name"
            width="200px"
            label="字段名称">
          </el-table-column>
          <el-table-column
            prop="desc"
            label="说明">
          </el-table-column>
        </el-table>
      </el-collapse-item>
      <el-collapse-item title="" name="4">
        <template slot="title">
          <div class="line_header">表的Mapping配置</div>
        </template>
        <div style="font: 16px bold; color: red">表的Mapping配置说明如下：</div>
        <div>1.原始库：表示原始mysql数据库的名称，支持Perl表达式,可参数 表的正则配置。</div>
        <div>2.原始表：表示原始mysql数据表的名称，支持Perl表达式,可参数 表的正则配置。</div>
        <div>3.目标库：表示需要写入的starrocks数据库。</div>
        <div>4.目标表：表示需要写入的starrocks数据表。</div>
        <div></div>
        <div style="font: 16px bold; color: red">其配置文件说明如下</div>
        <div>1.columns: 原表与目标表直接的字段级映射。</div>
        <div>&nbsp;&nbsp;&nbsp;A: srcField: 原表字段名称，除了支持原表的字段之外，还支持以下值：</div>
        <div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  ${DATABASE_NAME}: 表示当前的原表的表名。</div>
        <div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  ${TABLE_NAME}: 表示当前的原表的表名。</div>
        <div>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  ${SYNC_TIME}: 表示该条ddl的同步时间。</div>
        <div>&nbsp;&nbsp;&nbsp;B: dstField: 目标表的字段名称</div>
        <div>2.dstField: 目标表的主键字段，值为数组。</div>
        <div>3.deleteStrategy: 针对删除数据的策略，支持的值为：delete、update</div>
        <div>4.deleteUpdateField: 如何将删除策略配置为update，则需要指定更新的目标表字段名称。</div>
        <div>5.deleteUpdateValue: 如何将删除策略配置为update，则需要指定更新的目标表字段值。</div>
        <div>6.needType: 所需要的数据变更类型，值为数组，支持的值为：insert、update、delete。</div>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script>

export default {
  data() {
    return {
      activeNames: ['1'],
      consumerInfo:[
        {
        name: 'kafkaBootstrap',
        desc: 'kafka的地址，如:192.168.20.1:9092,192.168.20.2:9092'
      },{
        name: 'topics',
        desc: '需要消费的topic列表，如:["topic1", "topic2"]'
      },{
        name: 'mappingEnv',
        desc: 'Table Mapping的环境'
      },{
        name: 'jdbcUrl',
        desc: 'starrocks的jdbc连接地址，不需要写数据库，如:jdbc:mysql://127.0.0.1:9030/'
      },{
        name: 'feHost',
        desc: 'fe的host地址，用于streamLoad，如:127.0.0.1'
      },{
        name: 'feHttpPort',
        desc: 'fe的端口，用于streamLoad，如:8050'
      },{
        name: 'userName',
        desc: 'starrocks进行jdbc操作的用户名'
      },{
        name: 'passWord',
        desc: 'starrocks进行jdbc操作的密码'
      },{
        name: 'consumerPolicy',
        desc: '消费策略，有如下四种消费策略，\n ' +
          ' 1.E-C:如果消费过，则继续从消费点开始消费，如果没有消费过，则从头开始消费。\n' +
          ' 2:E-B:无论是否消费过，都从头开始消费。\n' +
          ' 3:L-C:如果消费过，则继续从消费点开始消费，如果没有消费过，则从最新数据开始消费。\n' +
          ' 4:L-B:无论是否消费过，都从最新数据开始消费。\n'+
          ' 如不是该四种情况，默认为：E-C'

      }
      ],
      tableRegexInfo:[
        {
          name: '.*   or  .*\\\\..*',
          desc: '所有表'
        },
        {
          name: 'canal\\\\..*',
          desc: 'canal schema下所有表'
        },
        {
          name: 'canal\\\\.canal.*',
          desc: 'canal下的以canal打头的表'
        },
        {
          name: 'canal\\\\.test1',
          desc: 'canal schema下的一张名为test1的表'
        },
        {
          name: 'canal\\\\..*,mysql.test1,mysql.test2 (逗号分隔)',
          desc: '多个规则组合使用'
        }
      ],
      partitionHashInfo: [
        {
          name: 'test\\\\.test:pk1^pk2',
          desc:'指定匹配的单表，对应的hash字段为pk1 + pk2'
        },
        {
          name: '.*\\\\..*:id',
          desc:'正则匹配，指定所有正则匹配的表对应的hash字段为id'
        },
        {
          name: '.*\\\\..*:$pk$',
          desc:'正则匹配，指定所有正则匹配的表对应的hash字段为表主键(自动查找)'
        },
        {
          name: '',
          desc:'匹配规则啥都不写，则默认发到0这个partition上'
        },
        {
          name: '.*\\\\..*',
          desc:'不指定pk信息的正则匹配，将所有正则匹配的表,对应的hash字段为表名'
        },
        {
          name: 'test\\\\.test:id,.\\\\..* ',
          desc:'针对test的表按照id散列,其余的表按照table散列'
        },
      ]
    }
  },
  methods: {
    handleChange(val) {
      console.log(val);
    }
  }
}
</script>
<style scoped>
.line_header{
  color: #20a0ff !important;
  font: 20px bold!important;
}
</style>
