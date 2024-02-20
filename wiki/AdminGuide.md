---------
<h1>环境要求</h1>
<h3>1. 操作系统</h3>
<p style="font-size: 14px;">&nbsp; &nbsp; a. &nbsp;纯java开发，windows/linux均可支持</p>
<p style="font-size: 14px;">&nbsp; &nbsp; b. &nbsp;jdk建议使用1.6.25以上的版本，稳定可靠，目前阿里巴巴使用基本为此版本.&nbsp;</p>
<p style="font-size: 14px;">&nbsp;</p>
<h3>2. mysql要求</h3>
<p style="font-size: 14px;">&nbsp; &nbsp;a.&nbsp;当前的canal开源版本支持5.7及以下的版本(阿里内部mysql 5.7.13, 5.6.10, mysql 5.5.18和5.1.40/48)，ps. mysql4.x版本没有经过严格测试，理论上是可以兼容</p>
<p style="font-size: 14px;">&nbsp; &nbsp;b. canal的原理是基于mysql binlog技术，所以这里一定需要开启mysql的binlog写入功能，并且配置binlog模式为row.</p>
<pre name="code" class="java">[mysqld]  
log-bin=mysql-bin #添加这一行就ok  
binlog-format=ROW #选择row模式  
server_id=1 #配置mysql replaction需要定义，不能和canal的slaveId重复  </pre>

&nbsp; &nbsp;&nbsp;数据库重启后, 简单测试 `my.cnf` 配置是否生效:  

```
mysql> show variables like 'binlog_format';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| binlog_format | ROW   |
+---------------+-------+
```
```
mysql> show variables like 'log_bin';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| log_bin       | ON    |
+---------------+-------+
```

&nbsp; &nbsp;&nbsp;如果 my.cnf 设置不起作用,请参考:  
  &nbsp; &nbsp;&nbsp;https://stackoverflow.com/questions/38288646/changes-to-my-cnf-dont-take-effect-ubuntu-16-04-mysql-5-6  
  &nbsp; &nbsp;&nbsp;https://stackoverflow.com/questions/52736162/set-binlog-for-mysql-5-6-ubuntu16-4

<div class="iteye-blog-content-contain" style="font-size: 14px;">&nbsp; &nbsp;c. &nbsp;canal的原理是模拟自己为mysql slave，所以这里一定需要做为mysql slave的相关权限&nbsp;<br />
<pre name="code" class="java">CREATE USER canal IDENTIFIED BY 'canal';    
GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'canal'@'%';  
-- GRANT ALL PRIVILEGES ON *.* TO 'canal'@'%' ;  
FLUSH PRIVILEGES; </pre>
&nbsp; &nbsp; &nbsp;针对已有的账户可通过grants查询权限：<br />
<pre name="code" class="java">show grants for 'canal' </pre>
</div>
<h3 style="font-size: 14px;">&nbsp;</h3>
<h1>部署</h1>
<h3>1. 获取发布包</h3>
<p style="font-size: 14px;">方法1： (直接下载)</p>
<p style="font-size: 14px;">访问：<a style="font-size: 12px; line-height: 1.5;" href="https://github.com/alibaba/canal/tree/gh-pages/download">https://github.com/alibaba/canal/tree/gh-pages/download</a>&nbsp;，会列出所有历史的发布版本包</p>
<p style="font-size: 14px;">下载方式，比如以1.0.4版本为例子：&nbsp;</p>
<pre name="code" class="java">wget https://raw.github.com/alibaba/canal/gh-pages/download/canal.deployer-1.0.4.tar.gz</pre>
<p style="font-size: 14px;">方法2: &nbsp;(自己编译)</p>
<pre name="code" class="java">git clone git@github.com:alibaba/canal.git
git co canal-$version #切换到对应的版本上
mvn clean install -Denv=release</pre>
<p style="font-size: 14px;">执行完成后，会在canal工程根目录下生成一个target目录，里面会包含一个 canal.deployer-$verion.tar.gz</p>
<p style="font-size: 14px;">&nbsp;</p>
<h3>2. 目录结构</h3>
<p style="font-size: 14px;">解压缩发布包后，可得如下目录结构：</p>
<pre name="code" class="java">drwxr-xr-x 2 jianghang jianghang  136 2013-03-19 15:03 bin
drwxr-xr-x 4 jianghang jianghang  160 2013-03-19 15:03 conf
drwxr-xr-x 2 jianghang jianghang 1352 2013-03-19 15:03 lib
drwxr-xr-x 2 jianghang jianghang   48 2013-03-19 15:03 logs</pre>
<p style="font-size: 14px;">&nbsp;</p>
<h3>3. 启动/停止</h3>
<p style="font-size: 14px;">&nbsp; &nbsp;linux启动 : &nbsp;&nbsp;</p>
<pre name="code" class="java">sh startup.sh </pre>
<p style="font-size: 14px;">&nbsp; &nbsp;linux带debug方式启动：(默认使用suspend=y，阻塞等待你remote debug链接成功)</p>
<pre name="code" class="java">sh startup.sh debug 9099</pre>
<p style="font-size: 14px;">&nbsp; &nbsp;linux停止：</p>
<pre name="code" class="java">sh stop.sh</pre>
<p style="font-size: 14px;">&nbsp;&nbsp; &nbsp; &nbsp;&nbsp;</p>
<p style="font-size: 14px;">&nbsp; 几点注意：&nbsp;</p>
<ol style="font-size: 14px;">
<li>linux启动完成后，会在bin目录下生成canal.pid，stop.sh会读取canal.pid进行进程关闭</li>
<li>startup.sh默认读取系统环境变量中的which java获得JAVA执行路径，需要设置PATH=$JAVA_HOME/bin环境变量</li>
</ol>
<p style="font-size: 14px;">------------- &nbsp;&nbsp;</p>
<p style="font-size: 14px;">&nbsp; &nbsp; windows启动：(windows支持相对比较弱)</p>
<pre name="code" class="java">startup.bat</pre>
<p style="font-size: 14px;">&nbsp; &nbsp; windows停止：直接关闭终端即可</p>
<p style="font-size: 14px;">&nbsp;</p>
<h1>配置</h1>
<p style="font-size: 14px;">介绍配置之前，先了解下canal的配置加载方式：</p>
<p style="font-size: 14px;"><img src="http://dl.iteye.com/upload/attachment/0081/8759/4faa7c2e-ed95-3638-a3e2-8b481c8e4060.jpg" alt="" width="460" height="321" /></p>
<p style="font-size: 14px;">&nbsp;</p>
<p style="font-size: 14px;">canal配置方式有两种：</p>
<ol>
<li>ManagerCanalInstanceGenerator： 基于manager管理的配置方式，目前alibaba内部配置使用这种方式。大家可以实现CanalConfigClient，连接各自的管理系统，即可完成接入。</li>
<li>SpringCanalInstanceGenerator：基于本地spring xml的配置方式，目前开源版本已经自带该功能所有代码，建议使用</li>
</ol>
<h3>Spring配置</h3>
<p>spring配置的原理是将整个配置抽象为两部分：</p>
<ul>
<li>xxxx-instance.xml &nbsp; (canal组件的配置定义，可以在多个instance配置中共享)</li>
<li>xxxx.properties &nbsp;&nbsp;(每个instance通道都有各自一份定义，因为每个mysql的ip，帐号，密码等信息不会相同)</li>
</ul>
<p>通过spring的PropertyPlaceholderConfigurer通过机制将其融合，生成一份instance实例对象，每个instance对应的组件都是相互独立的，互不影响</p>
<p>&nbsp;</p>
<h4>properties配置文件</h4>
<p>properties配置分为两部分：</p>
<ul>
<li>canal.properties &nbsp;(系统根配置文件)</li>
<li>instance.properties &nbsp;(instance级别的配置文件，每个instance一份)</li>
</ul>
<p><strong>canal.properties介绍：</strong></p>
<p>&nbsp;</p>
<p>canal配置主要分为两部分定义：</p>
<p>1. &nbsp; instance列表定义 (列出当前server上有多少个instance，每个instance的加载方式是spring/manager等) &nbsp; &nbsp; &nbsp; &nbsp;</p>
<table class="bbcode" style="table-layout: fixed; min-width: 400px; max-width: 650px;">
<tbody>
<tr>
<td>参数名字</td>
<td>参数说明</td>
<td>默认值</td>
</tr>
<tr>
<td>canal.destinations</td>
<td>当前server上部署的instance列表</td>
<td>无</td>
</tr>
<tr>
<td>canal.conf.dir</td>
<td>conf/目录所在的路径</td>
<td>../conf</td>
</tr>
<tr>
<td>canal.auto.scan</td>
<td>开启instance自动扫描<br />如果配置为true，canal.conf.dir目录下的instance配置变化会自动触发：<br />a. instance目录新增： 触发instance配置载入，lazy为true时则自动启动<br />b. instance目录删除：卸载对应instance配置，如已启动则进行关闭<br />c. instance.properties文件变化：reload instance配置，如已启动自动进行重启操作</td>
<td>true</td>
</tr>
<tr>
<td>canal.auto.scan.interval</td>
<td>instance自动扫描的间隔时间，单位秒</td>
<td>5</td>
</tr>
<tr>
<td>canal.instance.global.mode</td>
<td>全局配置加载方式</td>
<td>spring</td>
</tr>
<tr>
<td>canal.instance.global.lazy</td>
<td>全局lazy模式</td>
<td>false</td>
</tr>
<tr>
<td>canal.instance.global.manager.address</td>
<td>全局的manager配置方式的链接信息</td>
<td>无</td>
</tr>
<tr>
<td>canal.instance.global.spring.xml</td>
<td>全局的spring配置方式的组件文件</td>
<td>classpath:spring/memory-instance.xml&nbsp;<br />&nbsp;(spring目录相对于canal.conf.dir)</td>
</tr>
<tr>
<td>canal.instance.example.mode<br />canal.instance.example.lazy<br />canal.instance.example.spring.xml<br />.....</td>
<td>instance级别的配置定义，如有配置，会自动覆盖全局配置定义模式<br />命名规则：canal.instance.{name}.xxx</td>
<td>无</td>
</tr>
<tr>
<td>canal.instance.tsdb.spring.xml</td>
<td>v1.0.25版本新增,全局的tsdb配置方式的组件文件</td>
<td>classpath:spring/tsdb/h2-tsdb.xml (spring目录相对于canal.conf.dir)</td>
</tr>
</tbody>
</table>
<p>&nbsp;</p>
<p>&nbsp;</p>
<p>2. &nbsp;common参数定义，比如可以将instance.properties的公用参数，抽取放置到这里，这样每个instance启动的时候就可以共享. &nbsp;【instance.properties配置定义优先级高于canal.properties】</p>
<table class="bbcode" style="table-layout: fixed; min-width: 400px; max-width: 650px;">
<tbody>
<tr>
<td>参数名字</td>
<td>参数说明</td>
<td>默认值</td>
</tr>
<tr>
<td>canal.id</td>
<td>每个canal server实例的唯一标识，暂无实际意义</td>
<td>1</td>
</tr>
<tr>
<td>canal.ip</td>
<td>canal server绑定的本地IP信息，如果不配置，默认选择一个本机IP进行启动服务</td>
<td>无</td>
</tr>
<tr>
<td>canal.register.ip</td>
<td>canal server注册到外部zookeeper、admin的ip信息 (针对docker的外部可见ip)</td>
<td>无</td>
</tr>
<tr>
<td>canal.port</td>
<td>canal server提供socket服务的端口</td>
<td>11111</td>
</tr>
<tr>
<td>canal.zkServers</td>
<td>canal server链接zookeeper集群的链接信息<br />例子：10.20.144.22:2181,10.20.144.51:2181</td>
<td>无</td>
</tr>
<tr>
<td>canal.zookeeper.flush.period</td>
<td>canal持久化数据到zookeeper上的更新频率，单位毫秒</td>
<td>1000</td>
</tr>
<tr>
<td>canal.instance.memory.batch.mode</td>
<td>canal内存store中数据缓存模式<br />1. ITEMSIZE : 根据buffer.size进行限制，只限制记录的数量<br />2. MEMSIZE : 根据buffer.size &nbsp;*&nbsp;buffer.memunit的大小，限制缓存记录的大小</td>
<td>MEMSIZE</td>
</tr>
<tr>
<td>canal.instance.memory.buffer.size</td>
<td>canal内存store中可缓存buffer记录数，需要为2的指数</td>
<td>16384</td>
</tr>
<tr>
<td>canal.instance.memory.buffer.memunit</td>
<td>内存记录的单位大小，默认1KB，和buffer.size组合决定最终的内存使用大小</td>
<td>1024</td>
</tr>
<tr>
<td>canal.instance.transactionn.size</td>
<td>最大事务完整解析的长度支持<br />超过该长度后，一个事务可能会被拆分成多次提交到canal store中，无法保证事务的完整可见性</td>
<td>1024</td>
</tr>
<tr>
<td>canal.instance.fallbackIntervalInSeconds</td>
<td>canal发生mysql切换时，在新的mysql库上查找binlog时需要往前查找的时间，单位秒<br />说明：mysql主备库可能存在解析延迟或者时钟不统一，需要回退一段时间，保证数据不丢</td>
<td>60</td>
</tr>
<tr>
<td>canal.instance.detecting.enable</td>
<td>是否开启心跳检查</td>
<td>false</td>
</tr>
<tr>
<td>canal.instance.detecting.sql</td>
<td>心跳检查sql</td>
<td>insert into retl.xdual values(1,now()) on duplicate key update x=now()</td>
</tr>
<tr>
<td>canal.instance.detecting.interval.time</td>
<td>心跳检查频率，单位秒</td>
<td>3</td>
</tr>
<tr>
<td>canal.instance.detecting.retry.threshold</td>
<td>心跳检查失败重试次数</td>
<td>3</td>
</tr>
<tr>
<td>canal.instance.detecting.heartbeatHaEnable</td>
<td>心跳检查失败后，是否开启自动mysql自动切换<br />说明：比如心跳检查失败超过阀值后，如果该配置为true，canal就会自动链到mysql备库获取binlog数据</td>
<td>false</td>
</tr>
<tr>
<td>canal.instance.network.receiveBufferSize</td>
<td>网络链接参数，SocketOptions.SO_RCVBUF</td>
<td>16384</td>
</tr>
<tr>
<td>canal.instance.network.sendBufferSize</td>
<td>网络链接参数，SocketOptions.SO_SNDBUF</td>
<td>16384</td>
</tr>
<tr>
<td>canal.instance.network.soTimeout</td>
<td>网络链接参数，SocketOptions.SO_TIMEOUT</td>
<td>30</td>
</tr>
<tr>
<td>canal.instance.filter.druid.ddl</td>
<td>是否使用druid处理所有的ddl解析来获取库和表名</td>
<td>
<p class="p1"><span style="color: #000000; font-family: Helvetica, Tahoma, Arial, sans-serif; font-size: 14px;">true</span></p>
</td>
</tr>
<tr>
<td>canal.instance.filter.query.dcl</td>
<td>是否忽略dcl语句</td>
<td>false</td>
</tr>
<tr>
<td>canal.instance.filter.query.dml</td>
<td>是否忽略dml语句<br />(mysql5.6之后，在row模式下每条DML语句也会记录SQL到binlog中,可参考<a href="https://dev.mysql.com/doc/refman/5.6/en/replication-options-binary-log.html#sysvar_binlog_rows_query_log_events">MySQL文档</a>)</td>
<td>false</td>
</tr>
<tr>
<td>canal.instance.filter.query.ddl</td>
<td>是否忽略ddl语句</td>
<td>false</td>
</tr>
<tr>
<td>canal.instance.filter.table.error</td>
<td>
<p>是否忽略binlog表结构获取失败的异常</p>
<p>(主要解决回溯binlog时,对应表已被删除或者表结构和binlog不一致的情况)</p>
</td>
<td>false</td>
</tr>
<tr>
<td>canal.instance.filter.rows</td>
<td>
<p>是否dml的数据变更事件</p>
<p>(主要针对用户只订阅ddl/dcl的操作)</p>
</td>
<td>false</td>
</tr>
<tr>
<td>canal.instance.filter.transaction.entry</td>
<td>是否忽略事务头和尾,比如针对写入kakfa的消息时，不需要写入TransactionBegin/Transactionend事件</td>
<td>false</td>
</tr>
<tr>
<td>canal.instance.binlog.format</td>
<td>支持的binlog format格式列表<br />(otter会有支持format格式限制)</td>
<td>ROW,STATEMENT,MIXED</td>
</tr>
<tr>
<td>canal.instance.binlog.image</td>
<td>支持的binlog image格式列表<br />(otter会有支持format格式限制)</td>
<td>FULL,MINIMAL,NOBLOB</td>
</tr>
<tr>
<td>canal.instance.get.ddl.isolation</td>
<td>
<p>ddl语句是否单独一个batch返回</p>
<p>(比如下游dml/ddl如果做batch内无序并发处理,会导致结构不一致)</p>
</td>
<td>false</td>
</tr>
<tr>
<td>canal.instance.parser.parallel</td>
<td>
<p>是否开启binlog并行解析模式</p>
<p>(串行解析资源占用少,但性能有瓶颈, 并行解析可以提升近2.5倍+)</p>
</td>
<td>true</td>
</tr>
<tr>
<td>canal.instance.parser.parallelBufferSize</td>
<td>binlog并行解析的异步ringbuffer队列<br />(必须为2的指数)</td>
<td>
<p class="p1"><span style="color: #000000; font-family: Helvetica, Tahoma, Arial, sans-serif; font-size: 14px;">256</span></p>
</td>
</tr>
<tr>
<td>canal.instance.tsdb.enable</td>
<td>是否开启tablemeta的tsdb能力</td>
<td>true</td>
</tr>
<tr>
<td>canal.instance.tsdb.dir</td>
<td>主要针对h2-tsdb.xml时对应h2文件的存放目录,默认为conf/xx/h2.mv.db</td>
<td>${canal.file.data.dir:../<span class="s1">conf</span>}/${canal.instance.destination:}</td>
</tr>
<tr>
<td>canal.instance.tsdb.url</td>
<td>
<p>jdbc url的配置</p>
<p>(h2的地址为默认值，如果是mysql需要自行定义)</p>
</td>
<td>jdbc:h2:${canal.instance.tsdb.dir}/h2;CACHE_SIZE=1000;MODE=MYSQL;</td>
</tr>
<tr>
<td>canal.instance.tsdb.dbUsername</td>
<td>
<p>jdbc url的配置</p>
<p>(h2的地址为默认值，如果是mysql需要自行定义)</p>
</td>
<td>canal</td>
</tr>
<tr>
<td>canal.instance.tsdb.dbPassword</td>
<td>
<p>jdbc url的配置</p>
<p>(h2的地址为默认值，如果是mysql需要自行定义)</p>
</td>
<td>canal</td>
</tr>
<tr>
<td>canal.instance.rds.accesskey</td>
<td>
<p>aliyun账号的ak信息</p>
<p>(如果不需要在本地binlog超过18小时被清理后自动下载oss上的binlog，可以忽略该值</p>
</td>
<td>无</td>
</tr>
<tr>
<td>canal.instance.rds.secretkey</td>
<td>
<p>aliyun账号的sk信息</p>
<p>(如果不需要在本地binlog超过18小时被清理后自动下载oss上的binlog，可以忽略该值)</p>
</td>
<td>无</td>
</tr>
<tr>
<td>canal.admin.manager</td>
<td>
canal链接canal-admin的地址 (v1.1.4新增)
</td>
<td>无</td>
</tr>
<tr>
<td>canal.admin.port</td>
<td>
<p>admin管理指令链接端口 (v1.1.4新增)</p>
</td>
<td>11110</td>
</tr>
<tr>
<td>canal.admin.user</td>
<td>
<p>admin管理指令链接的ACL配置 (v1.1.4新增)</p>
</td>
<td>admin</td>
</tr>
<tr>
<td>canal.admin.passwd</td>
<td>
<p>admin管理指令链接的ACL配置 (v1.1.4新增)</p>
</td>
<td>密码默认值为admin的密文</td>
</tr>
<tr>
<td>canal.user</td>
<td>
<p>canal数据端口订阅的ACL配置 (v1.1.4新增)</p>
<p>如果为空，代表不开启</p>
</td>
<td>无</td>
</tr>
<tr>
<td>canal.passwd</td>
<td>
<p>canal数据端口订阅的ACL配置 (v1.1.4新增)</p>
<p>如果为空，代表不开启</p>
</td>
<td>无</td>
</tr>
</tbody>
</table>
<p>&nbsp;</p>
<h4>instance.properties介绍：</h4>
<p>a. 在canal.properties定义了canal.destinations后，需要在canal.conf.dir对应的目录下建立同名的文件</p>
<p>比如：</p>
<p>&nbsp;</p>
<pre name="code" class="java">canal.destinations = example1,example2</pre>
&nbsp;这时需要创建example1和example2两个目录，每个目录里各自有一份instance.properties.
<p>&nbsp;</p>
<p>&nbsp;ps. canal自带了一份instance.properties demo，可直接复制conf/example目录进行配置修改</p>
<p>&nbsp;</p>
<pre name="code" class="java">cp -R example example1/
cp -R example example2/</pre>
&nbsp;
<p>&nbsp;</p>
<p>b. 如果canal.properties未定义instance列表，但开启了canal.auto.scan时</p>
<ul>
<li>server第一次启动时，会自动扫描conf目录下，将文件名做为instance name，启动对应的instance</li>
<li>server运行过程中，会根据canal.auto.scan.interval定义的频率，进行扫描<br />1. 发现目录有新增，启动新的instance<br />2. 发现目录有删除，关闭老的instance<br />3. 发现对应目录的instance.properties有变化，重启instance</li>
</ul>
<p>一个标准的conf目录结果：</p>
<p>&nbsp;</p>
<pre name="code" class="java">jianghang@jianghang-laptop:~/work/canal/deployer/target/canal$ ls -l conf/
总用量 8
-rwxrwxrwx 1 jianghang jianghang 1677 2013-03-19 15:03 canal.properties  ##系统配置
drwxr-xr-x 2 jianghang jianghang   88 2013-03-19 15:03 example  ## instance配置
-rwxrwxrwx 1 jianghang jianghang 1840 2013-03-19 15:03 logback.xml ## 日志文件
drwxr-xr-x 2 jianghang jianghang  168 2013-03-19 17:04 spring  ## spring instance目录
</pre>
&nbsp;
<p>&nbsp;</p>
<p>instance.properties参数列表：</p>
<table class="bbcode" style="table-layout: fixed; min-width: 400px; max-width: 650px;">
<tbody>
<tr>
<td>参数名字</td>
<td>参数说明</td>
<td>默认值</td>
</tr>
<tr>
<td>canal.instance.mysql.slaveId</td>
<td>mysql集群配置中的serverId概念，需要保证和当前mysql集群中id唯一 <br />(v1.1.x版本之后canal会自动生成，不需要手工指定)</td>
<td>无</td>
</tr>
<tr>
<td>canal.instance.master.address</td>
<td>mysql主库链接地址</td>
<td>127.0.0.1:3306</td>
</tr>
<tr>
<td>canal.instance.master.journal.name</td>
<td>mysql主库链接时起始的binlog文件</td>
<td>无</td>
</tr>
<tr>
<td>canal.instance.master.position</td>
<td>mysql主库链接时起始的binlog偏移量</td>
<td>无</td>
</tr>
<tr>
<td>canal.instance.master.timestamp</td>
<td>mysql主库链接时起始的binlog的时间戳</td>
<td>无</td>
</tr>
<tr>
<td>canal.instance.gtidon</td>
<td>是否启用mysql gtid的订阅模式</td>
<td>false</td>
</tr>
<tr>
<td>canal.instance.master.gtid</td>
<td>mysql主库链接时对应的gtid位点</td>
<td>无</td>
</tr>
<tr>
<td>canal.instance.dbUsername</td>
<td>mysql数据库帐号</td>
<td>canal</td>
</tr>
<tr>
<td>canal.instance.dbPassword</td>
<td>mysql数据库密码</td>
<td>canal</td>
</tr>
<tr>
<td>canal.instance.defaultDatabaseName</td>
<td>mysql链接时默认schema</td>
<td>&nbsp;</td>
</tr>
<tr>
<td>canal.instance.connectionCharset</td>
<td>mysql 数据解析编码</td>
<td>UTF-8</td>
</tr>
<tr>
<td>canal.instance.filter.regex</td>
<td>
<p>mysql 数据解析关注的表，Perl正则表达式.</p>
<p>多个正则之间以逗号(,)分隔，转义符需要双斜杠(\\)</p>
<p><br />常见例子：</p>
<p>1. &nbsp;所有表：.* &nbsp; or &nbsp;.*\\..*<br />2. &nbsp;canal schema下所有表： canal\\..*<br />3. &nbsp;canal下的以canal打头的表：canal\\.canal.*<br />4. &nbsp;canal schema下的一张表：canal\\.test1</p>
<p>5. &nbsp;多个规则组合使用：canal\\..*,mysql.test1,mysql.test2 (逗号分隔)</p>
</td>
<td>.*\\..*</td>
</tr>
<tr>
<td>canal.instance.filter.black.regex</td>
<td>
<p>mysql 数据解析表的黑名单，表达式规则见白名单的规则</p>
</td>
<td>无</td>
</tr>
<tr>
<td>canal.instance.rds.instanceId</td>
<td>
<div style="box-sizing: border-box; min-height: 24px; line-height: 24px; margin-top: 0px; margin-bottom: 0px; white-space: pre-wrap; color: #262626; font-family: 'Chinese Quote', -apple-system, system-ui, 'Segoe UI', Roboto, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif; letter-spacing: 0.7px;" data-type="p">aliyun rds对应的实例id信息</div>
<div style="box-sizing: border-box; min-height: 24px; line-height: 24px; margin-top: 0px; margin-bottom: 0px; white-space: pre-wrap; color: #262626; font-family: 'Chinese Quote', -apple-system, system-ui, 'Segoe UI', Roboto, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif; letter-spacing: 0.7px;" data-type="p">(如果不需要在本地binlog超过18小时被清理后自动下载oss上的binlog，可以忽略该值)</div>
</td>
<td>无</td>
</tr>
</tbody>
</table>
<p>&nbsp;</p>
<p>几点说明：</p>
<p>1. &nbsp;mysql链接时的起始位置</p>
<ul>
<li>canal.instance.master.journal.name + &nbsp;canal.instance.master.position : &nbsp;精确指定一个binlog位点，进行启动</li>
<li>canal.instance.master.timestamp : &nbsp;指定一个时间戳，canal会自动遍历mysql binlog，找到对应时间戳的binlog位点后，进行启动</li>
<li>不指定任何信息：默认从当前数据库的位点，进行启动。(show master status)</li>
</ul>
<p>2. mysql解析关注表定义</p>
<ul>
<li>标准的Perl正则，注意转义时需要双斜杠：\\</li>
</ul>
<p>3. mysql链接的编码</p>
<ul>
<li>目前canal版本仅支持一个数据库只有一种编码，如果一个库存在多个编码，需要通过filter.regex配置，将其拆分为多个canal instance，为每个instance指定不同的编码</li>
</ul>
<p>&nbsp;</p>
<h4>instance.xml配置文件</h4>
<p>目前默认支持的instance.xml有以下几种：</p>
<ol>
<li>spring/memory-instance.xml</li>
<li>spring/default-instance.xml</li>
<li>spring/group-instance.xml</li>
</ol>
<p>在介绍instance配置之前，先了解一下canal如何维护一份增量订阅&amp;消费的关系信息：</p>
<ul>
<li>解析位点 (parse模块会记录，上一次解析binlog到了什么位置，对应组件为：CanalLogPositionManager)</li>
<li>消费位点&nbsp;(canal server在接收了客户端的ack后，就会记录客户端提交的最后位点，对应的组件为：CanalMetaManager)</li>
</ul>
<p>对应的两个位点组件，目前都有几种实现：</p>
<ul>
<li>memory &nbsp;(memory-instance.xml中使用)</li>
<li>zookeeper</li>
<li>mixed &nbsp;</li>
<li>period &nbsp;&nbsp;(default-instance.xml中使用，集合了zookeeper+memory模式，先写内存，定时刷新数据到zookeeper上)</li>
</ul>
<p>-------------------</p>
<p>memory-instance.xml介绍：</p>
<p>&nbsp; &nbsp;所有的组件(parser , sink , store)都选择了内存版模式，记录位点的都选择了memory模式，重启后又会回到初始位点进行解析&nbsp;</p>
<p>&nbsp; &nbsp;特点：速度最快，依赖最少(不需要zookeeper)</p>
<p>&nbsp; &nbsp;场景：一般应用在quickstart，或者是出现问题后，进行数据分析的场景，不应该将其应用于生产环境</p>
<p>&nbsp;</p>
<p>default-instance.xml介绍：</p>
<p>&nbsp; &nbsp;store选择了内存模式，其余的parser/sink依赖的位点管理选择了持久化模式，目前持久化的方式主要是写入zookeeper，保证数据集群共享.&nbsp;</p>
<p>&nbsp; &nbsp;特点：支持HA</p>
<p>&nbsp; &nbsp;场景：生产环境，集群化部署.&nbsp;</p>
<p>&nbsp;</p>
<p>group-instance.xml介绍：</p>
<p>&nbsp; &nbsp; 主要针对需要进行多库合并时，可以将多个物理instance合并为一个逻辑instance，提供客户端访问。</p>
<p>&nbsp; &nbsp; 场景：分库业务。 比如产品数据拆分了4个库，每个库会有一个instance，如果不用group，业务上要消费数据时，需要启动4个客户端，分别链接4个instance实例。使用group后，可以在canal server上合并为一个逻辑instance，只需要启动1个客户端，链接这个逻辑instance即可.&nbsp;</p>
<p>&nbsp;</p>
<h4>instance.xml设计初衷：</h4>
<p>&nbsp; 允许进行自定义扩展，比如实现了基于数据库的位点管理后，可以自定义一份自己的instance.xml，整个canal设计中最大的灵活性在于此</p>
<p>&nbsp;</p>
<h3><strong>HA模式配置</strong></h3>
<p>1. &nbsp;机器准备</p>
<p>&nbsp; &nbsp; &nbsp;a. &nbsp;运行canal的机器： 10.20.144.22 , 10.20.144.51.</p>
<p>&nbsp; &nbsp; &nbsp;b. &nbsp;zookeeper地址为10.20.144.51:2181</p>
<p>&nbsp; &nbsp; &nbsp;c. &nbsp;mysql地址：10.20.144.15:3306</p>
<p>2. &nbsp;按照部署和配置，在单台机器上各自完成配置，演示时instance name为example</p>
<p>&nbsp; &nbsp;a. 修改canal.properties，加上zookeeper配置</p>
<p>&nbsp;</p>
<pre name="code" class="java">canal.zkServers=10.20.144.51:2181
canal.instance.global.spring.xml = classpath:spring/default-instance.xml</pre>
<p>&nbsp; &nbsp;b. 创建example目录，并修改instance.properties</p>
<pre name="code" class="java">canal.instance.mysql.slaveId = 1234 ##另外一台机器改成1235，保证slaveId不重复即可
canal.instance.master.address = 10.20.144.15:3306</pre>
<p>&nbsp; &nbsp;</p>
<p>&nbsp; &nbsp; 注意：&nbsp;两台机器上的instance目录的名字需要保证完全一致，HA模式是依赖于instance name进行管理，同时必须都选择default-instance.xml配置</p>
<p>3. &nbsp;启动两台机器的canal</p>
<pre name="code" class="java">-------
ssh 10.20.144.51
sh bin/startup.sh
--------
ssh 10.20.144.22
sh bin/startup.sh</pre>
<p>&nbsp;启动后，你可以查看logs/example/example.log，只会看到一台机器上出现了启动成功的日志。</p>
<p>&nbsp; &nbsp;</p>
<p>&nbsp; 比如我这里启动成功的是10.20.144.51</p>
<pre name="code" class="java">2013-03-19 18:18:20.590 [main] INFO  c.a.o.c.i.spring.support.PropertyPlaceholderConfigurer - Loading properties file from class path resource [canal.properties]
2013-03-19 18:18:20.596 [main] INFO  c.a.o.c.i.spring.support.PropertyPlaceholderConfigurer - Loading properties file from class path resource [example/instance.properties]
2013-03-19 18:18:20.831 [main] INFO  c.a.otter.canal.instance.spring.CanalInstanceWithSpring - start CannalInstance for 1-example 
2013-03-19 18:18:20.845 [main] INFO  c.a.otter.canal.instance.spring.CanalInstanceWithSpring - start successful...</pre>
<p>&nbsp; &nbsp;&nbsp;</p>
<p>&nbsp; &nbsp;查看一下zookeeper中的节点信息，也可以知道当前工作的节点为10.20.144.51:11111</p>
<pre name="code" class="java">[zk: localhost:2181(CONNECTED) 15] get /otter/canal/destinations/example/running  
{"active":true,"address":"10.20.144.51:11111","cid":1}</pre>
<p>&nbsp;</p>
<p>4. &nbsp;客户端链接, 消费数据</p>
<p>&nbsp; &nbsp; a. &nbsp;可以直接指定zookeeper地址和instance name，canal client会自动从zookeeper中的running节点，获取当前服务的工作节点，然后与其建立链接：</p>
<pre name="code" class="java">CanalConnector connector = CanalConnectors.newClusterConnector("10.20.144.51:2181", "example", "", "");</pre>
<p>&nbsp; &nbsp; b. 链接成功后，canal server会记录当前正在工作的canal client信息，比如客户端ip，链接的端口信息等 (聪明的你，应该也可以发现，canal client也可以支持HA功能)</p>
<pre name="code" class="java">[zk: localhost:2181(CONNECTED) 17] get /otter/canal/destinations/example/1001/running
{"active":true,"address":"10.12.48.171:50544","clientId":1001}</pre>
<p>&nbsp; &nbsp; c. 数据消费成功后，canal server会在zookeeper中记录下当前最后一次消费成功的binlog位点. &nbsp;(下次你重启client时，会从这最后一个位点继续进行消费)</p>
<pre name="code" class="java">[zk: localhost:2181(CONNECTED) 16] get /otter/canal/destinations/example/1001/cursor
{"@type":"com.alibaba.otter.canal.protocol.position.LogPosition","identity":{"slaveId":-1,"sourceAddress":{"address":"10.20.144.15","port":3306}},"postion":{"included":false,"journalName":"mysql-bin.002253","position":2574756,"timestamp":1363688722000}}
</pre>
<p>&nbsp;</p>
<p>5. &nbsp;重启一下canal server</p>
<p>&nbsp; &nbsp; 停止正在工作的10.20.144.51的canal server</p>
<pre name="code" class="java">ssh 10.20.144.51 
sh bin/stop.sh</pre>
<p>&nbsp; &nbsp;这时10.20.144.22会立马启动example instance，提供新的数据服务</p>
<pre name="code" class="java">[zk: localhost:2181(CONNECTED) 19] get /otter/canal/destinations/example/running
{"active":true,"address":"10.20.144.22:11111","cid":1}
</pre>
<p>&nbsp; &nbsp;与此同时，客户端也会随着canal server的切换，通过获取zookeeper中的最新地址，与新的canal server建立链接，继续消费数据，整个过程自动完成</p>
<p>&nbsp;</p>
<h4><strong>触发HA自动切换场景 (server/client HA模式都有效)</strong></h4>
<p>1. 正常场景</p>
<p>&nbsp; &nbsp; a. &nbsp;正常关闭canal server(会释放instance的所有资源，包括删除running节点)</p>
<p>&nbsp; &nbsp; b. &nbsp;平滑切换(gracefully)</p>
<p>&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;操作：更新对应instance的running节点内容，将"active"设置为false，对应的running节点收到消息后，会主动释放running节点，让出控制权但自己jvm不退出，gracefully.&nbsp;</p>
<pre name="code" class="java">{"active":false,"address":"10.20.144.22:11111","cid":1}</pre>
<p>2. &nbsp;异常场景</p>
<p>&nbsp; &nbsp;a. &nbsp;canal server对应的jvm异常crash，running节点的释放会在对应的zookeeper session失效后，释放running节点(EPHEMERAL节点)</p>
<p>&nbsp; &nbsp; &nbsp; &nbsp;ps. session过期时间默认为zookeeper配置文件中定义的tickTime的20倍，如果不改动zookeeper配置，那默认就是40秒</p>
<p>&nbsp; &nbsp;b. &nbsp;canal server所在的网络出现闪断，导致zookeeper认为session失效，释放了running节点，此时canal server对应的jvm并未退出，(一种假死状态，非常特殊的情况)</p>
<p>&nbsp; &nbsp; &nbsp; ps. 为了保护假死状态的canal server，避免因瞬间runing失效导致instance重新分布，所以做了一个策略：canal server在收到running节点释放后，延迟一段时间抢占running，原本running节点的拥有者可以不需要等待延迟，优先取得running节点，可以保证假死状态下尽可能不无谓的释放资源。 目前延迟时间的默认值为5秒，即running节点针对假死状态的保护期为5秒.&nbsp;</p>
<p>&nbsp;</p>
<h3><strong>mysql多节点解析配置(parse解析自动切换)</strong></h3>
<p>1. &nbsp;mysql机器准备</p>
<p>&nbsp; &nbsp; &nbsp;准备两台mysql机器，配置为M-M模式，比如ip为：10.20.144.25:3306，10.20.144.29:3306</p>
<pre name="code" class="java">[mysqld] 
xxxxx ##其他正常master/slave配置
log_slave_updates=true ##这个配置一定要打开</pre>
<p>2. &nbsp;canal instance配置</p>
<pre name="code" class="java"># position info
canal.instance.master.address = 10.20.144.25:3306
canal.instance.master.journal.name = 
canal.instance.master.position = 
canal.instance.master.timestamp = 

canal.instance.standby.address = 10.20.144.29:3306
canal.instance.standby.journal.name =
canal.instance.standby.position = 
canal.instance.standby.timestamp =

##detecing config
canal.instance.detecting.enable = true ## 需要开启心跳检查
canal.instance.detecting.sql = insert into retl.xdual values(1,now()) on duplicate key update x=now() ##心跳检查sql，也可以选择类似select 1的query语句
canal.instance.detecting.interval.time = 3 ##心跳检查频率
canal.instance.detecting.retry.threshold = 3  ## 心跳检查失败次数阀值，超过该阀值后会触发mysql链接切换，比如切换到standby机器上继续消费binlog
canal.instance.detecting.heartbeatHaEnable = true ## 心跳检查超过失败次数阀值后，是否开启master/standby的切换. </pre>
<p>&nbsp;</p>
<p>注意：</p>
<p>&nbsp; &nbsp; a. &nbsp;填写master/standby的地址和各自的起始binlog位置，目前配置只支持一个standby配置. &nbsp;</p>
<p>&nbsp; &nbsp; b. &nbsp;发生master/standby的切换的条件：(heartbeatHaEnable = true) &amp;&amp; (失败次数&gt;=retry.threshold).&nbsp;</p>
<p>&nbsp; &nbsp; c. 多引入一个heartbeatHaEnable的考虑：开启心跳sql有时候是为client检测canal server是否正常工作，如果定时收到了心跳语句，那说明整个canal server工作正常</p>
<p>3. &nbsp;启动 &amp; 测试</p>
<p>&nbsp; &nbsp; 比如关闭一台机器的mysql , /etc/init.d/mysql stop 。在经历大概 &nbsp;interval.time * retry.threshold时间后，就会切换到standby机器上</p>
</div>
  </div>