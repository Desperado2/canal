<h1>ClientSample</h1>

<h2>直接使用canal.example工程</h2>
a.  首先启动Canal Server，可参见[[QuickStart]] <br/>
b.  <br/>
<ol>
<li>可以在eclipse里，直接打开com.alibaba.otter.canal.example.SimpleCanalClientTest，直接运行</li>
<li>在工程的example目录下运行命令行：
<pre class="java" name="code">mvn exec:java -Dexec.mainClass="com.alibaba.otter.canal.example.SimpleCanalClientTest"</li></pre>
<li>下载example包: https://github.com/alibaba/canal/releases，解压缩后，直接运行sh startup.sh脚本</li>
</ol>
c. 触发数据变更
d. 在控制台或者logs中查看，可以看到如下信息 ： 
<pre class="java" name="code">
================&gt; binlog[mysql-bin.002579:508882822] , name[retl,xdual] , eventType : UPDATE , executeTime : 1368607728000 , delay : 4270ms
-------&gt; before
ID : 1    update=false
X : 2013-05-15 11:43:42    update=false
-------&gt; after
ID : 1    update=false
X : 2013-05-15 16:48:48    update=true</pre>
<p> </p>

----

<h2>从头创建工程</h2>
<p>依赖配置：</p>
<pre name="code" class="java">&lt;dependency&gt;
    &lt;groupId&gt;com.alibaba.otter&lt;/groupId&gt;
    &lt;artifactId&gt;canal.client&lt;/artifactId&gt;
    &lt;version&gt;1.1.0&lt;/version&gt;
&lt;/dependency&gt;</pre>
<p> </p>
<p>1. 创建mvn标准工程：</p>
<pre name="code" class="java">mvn archetype:create -DgroupId=com.alibaba.otter -DartifactId=canal.sample</pre>
maven3.0.5以上版本舍弃了create，使用generate生成项目
<pre name="code" class="java">mvn archetype:generate -DgroupId=com.alibaba.otter -DartifactId=canal.sample</pre>
<p> </p>
<p>2.  修改pom.xml，添加依赖</p>
<p> </p>
<p>3.  ClientSample代码</p>
<pre name="code" class="SimpleCanalClientExample">package com.alibaba.otter.canal.sample;

import java.net.InetSocketAddress;
import java.util.List;

import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;

public class SimpleCanalClientExample {

    public static void main(String args[]) {
        // 创建链接
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(AddressUtils.getHostIp(),
                                                                                            11111), "example", "", "");
        int batchSize = 1000;
        int emptyCount = 0;
        try {
            connector.connect();
            connector.subscribe(".*\\..*");
            connector.rollback();
            int totalEmptyCount = 120;
            while (emptyCount < totalEmptyCount) {
                Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == -1 || size == 0) {
                    emptyCount++;
                    System.out.println("empty count : " + emptyCount);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    emptyCount = 0;
                    // System.out.printf("message[batchId=%s,size=%s] \n", batchId, size);
                    printEntry(message.getEntries());
                }

                connector.ack(batchId); // 提交确认
                // connector.rollback(batchId); // 处理失败, 回滚数据
            }

            System.out.println("empty too many times, exit");
        } finally {
            connector.disconnect();
        }
    }

    private static void printEntry(List<Entry> entrys) {
        for (Entry entry : entrys) {
            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
                continue;
            }

            RowChange rowChage = null;
            try {
                rowChage = RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
                                           e);
            }

            EventType eventType = rowChage.getEventType();
            System.out.println(String.format("================&gt; binlog[%s:%s] , name[%s,%s] , eventType : %s",
                                             entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                                             entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                                             eventType));

            for (RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == EventType.DELETE) {
                    printColumn(rowData.getBeforeColumnsList());
                } else if (eventType == EventType.INSERT) {
                    printColumn(rowData.getAfterColumnsList());
                } else {
                    System.out.println("-------&gt; before");
                    printColumn(rowData.getBeforeColumnsList());
                    System.out.println("-------&gt; after");
                    printColumn(rowData.getAfterColumnsList());
                }
            }
        }
    }

    private static void printColumn(List<Column> columns) {
        for (Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }
}</pre>
<p> </p>
<p>4. 运行Client</p>
<p>首先启动Canal Server，可参见[[QuickStart]]
<p>启动Canal Client后，可以从控制台从看到类似消息：</p>
<pre name="code" class="java">empty count : 1
empty count : 2
empty count : 3
empty count : 4</pre>
<p> 此时代表当前数据库无变更数据</p>
<p> </p>
<p>5.  触发数据库变更</p>
<pre name="code" class="java">mysql&gt; use test;
Database changed
mysql&gt; CREATE TABLE `xdual` (
    -&gt;   `ID` int(11) NOT NULL AUTO_INCREMENT,
    -&gt;   `X` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -&gt;   PRIMARY KEY (`ID`)
    -&gt; ) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 ;
Query OK, 0 rows affected (0.06 sec)

mysql&gt; insert into xdual(id,x) values(null,now());Query OK, 1 row affected (0.06 sec)</pre>
<p> </p>
<p>可以从控制台中看到：</p>
<pre name="code" class="java">empty count : 1
empty count : 2
empty count : 3
empty count : 4
================&gt; binlog[mysql-bin.001946:313661577] , name[test,xdual] , eventType : INSERT
ID : 4    update=true
X : 2013-02-05 23:29:46    update=true</pre>
<p> </p>
<h2>最后：</h2>
<p> 如果需要更详细的exmpale例子，请下载canal当前最新源码包，里面有个example工程，谢谢.</p>
<ul>
<li>Simple客户端例子：<a href="https://github.com/alibaba/canal/blob/master/example/src/main/java/com/alibaba/otter/canal/example/SimpleCanalClientTest.java">SimpleCanalClientTest</a></li>
<li>Cluster客户端例子：<a href="https://github.com/alibaba/canal/blob/master/example/src/main/java/com/alibaba/otter/canal/example/ClusterCanalClientTest.java">ClusterCanalClientTest</a></li>
</ul>
</div>