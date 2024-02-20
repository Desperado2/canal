<div class="blog_content">
    <div class="iteye-blog-content-contain">
<p style="font-size: 14px;">  </p>
<h1>背景</h1>
<p style="font-size: 14px;">   早期，阿里巴巴B2B公司因为存在杭州和美国双机房部署，存在跨机房同步的业务需求。不过早期的数据库同步业务，主要是基于trigger的方式获取增量变更，不过从2010年开始，阿里系公司开始逐步的尝试基于数据库的日志解析，获取增量变更进行同步，由此衍生出了增量订阅&amp;消费的业务，从此开启了一段新纪元。ps. 目前内部使用的同步，已经支持mysql5.x和oracle部分版本的日志解析</p>
<p style="font-size: 14px;"> </p>
<p style="font-size: 14px;">基于日志增量订阅&amp;消费支持的业务：</p>
<ol style="font-size: 14px;">
<li>数据库镜像</li>
<li>数据库实时备份</li>
<li>多级索引 (卖家和买家各自分库索引)</li>
<li>search build</li>
<li>业务cache刷新</li>
<li>价格变化等重要业务消息</li>
</ol>
<h1>项目介绍</h1>
<p style="font-size: 14px;">   名称：canal [kə'næl]</p>
<p style="font-size: 14px;">   译意： 水道/管道/沟渠 </p>
<p style="font-size: 14px;">   语言： 纯java开发</p>
<p style="font-size: 14px;">   定位： 基于数据库增量日志解析，提供增量数据订阅&amp;消费，目前主要支持了mysql</p>
<p style="font-size: 14px;"> </p>
<h2>工作原理</h2>
<h3 style="font-size: 14px;">mysql主备复制实现</h3>
<p><img src="http://dl.iteye.com/upload/attachment/0080/3086/468c1a14-e7ad-3290-9d3d-44ac501a7227.jpg" alt=""><br> 从上层来看，复制分成三步：</p>
<ol>
<li>master将改变记录到二进制日志(binary log)中（这些记录叫做二进制日志事件，binary log events，可以通过show binlog events进行查看）；</li>
<li>slave将master的binary log events拷贝到它的中继日志(relay log)；</li>
<li>slave重做中继日志中的事件，将改变反映它自己的数据。</li>
</ol>
<h3>canal的工作原理：</h3>
<p><img width="590" src="http://dl.iteye.com/upload/attachment/0080/3107/c87b67ba-394c-3086-9577-9db05be04c95.jpg" alt="" height="273"></p>
<p>原理相对比较简单：</p>
<ol>
<li>canal模拟mysql slave的交互协议，伪装自己为mysql slave，向mysql master发送dump协议</li>
<li>mysql master收到dump请求，开始推送binary log给slave(也就是canal)</li>
<li>canal解析binary log对象(原始为byte流)</li>
</ol>
<h1>架构</h1>
<p><img width="548" src="http://dl.iteye.com/upload/attachment/0080/3126/49550085-0cd2-32fa-86a6-f676db5b597b.jpg" alt="" height="238" style="line-height: 1.5;"></p>
<p style="color: #333333; background-image: none; margin-top: 10px; margin-bottom: 10px; font-family: Arial, Helvetica, FreeSans, sans-serif;">说明：</p>
<ul style="line-height: 1.5; color: #333333; font-family: Arial, Helvetica, FreeSans, sans-serif;">
<li>server代表一个canal运行实例，对应于一个jvm</li>
<li>instance对应于一个数据队列  （1个server对应1..n个instance)</li>
</ul>
<p>instance模块：</p>
<ul style="line-height: 1.5; color: #333333; font-family: Arial, Helvetica, FreeSans, sans-serif;">
<li>eventParser (数据源接入，模拟slave协议和master进行交互，协议解析)</li>
<li>eventSink (Parser和Store链接器，进行数据过滤，加工，分发的工作)</li>
<li>eventStore (数据存储)</li>
<li>metaManager (增量订阅&amp;消费信息管理器)</li>
</ul>
<h2>知识科普</h2>
<p style="font-size: 14px; font-weight: normal; line-height: 21px;">mysql的Binlay Log介绍</p>
<ul style="font-size: 14px; font-weight: normal; line-height: 21px;">
<li><a href="http://dev.mysql.com/doc/refman/5.5/en/binary-log.html">http://dev.mysql.com/doc/refman/5.5/en/binary-log.html</a></li>
<li><a href="http://www.taobaodba.com/html/474_mysqls-binary-log_details.html" style="line-height: 1.5;">http://www.taobaodba.com/html/474_mysqls-binary-log_details.html</a></li>
</ul>
<p>简单点说：</p>
<ul>
<li>mysql的binlog是多文件存储，定位一个LogEvent需要通过binlog filename +  binlog position，进行定位</li>
<li>mysql的binlog数据格式，按照生成的方式，主要分为：statement-based、row-based、mixed。<br><pre name="code" class="java">mysql&gt; show variables like 'binlog_format';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| binlog_format | ROW   |
+---------------+-------+
1 row in set (0.00 sec)</pre>
</li>
</ul>
<p>目前canal只能支持row模式的增量订阅(statement只有sql，没有数据，所以无法获取原始的变更日志)</p>
<p> </p>
<p> </p>
<h2>EventParser设计</h2>
<p>大致过程：</p>
<p><img width="707" src="http://dl.iteye.com/upload/attachment/0080/3143/7951c169-f7df-3cb3-aebb-d924f57311cb.jpg" alt="" height="406"></p>
<p>整个parser过程大致可分为几步：</p>
<ol>
<li>Connection获取上一次解析成功的位置  (如果第一次启动，则获取初始指定的位置或者是当前数据库的binlog位点)</li>
<li>Connection建立链接，发送BINLOG_DUMP指令<br> // 0. write command number<br> // 1. write 4 bytes bin-log position to start at<br> // 2. write 2 bytes bin-log flags<br> // 3. write 4 bytes server id of the slave<br> // 4. write bin-log file name</li>
<li>Mysql开始推送Binaly Log</li>
<li>接收到的Binaly Log的通过Binlog parser进行协议解析，补充一些特定信息<br>// 补充字段名字，字段类型，主键信息，unsigned类型处理</li>
<li>传递给EventSink模块进行数据存储，是一个阻塞操作，直到存储成功</li>
<li>存储成功后，定时记录Binaly Log位置</li>
</ol>
<p>mysql的Binlay Log网络协议：</p>
<p><img width="667" src="http://dl.iteye.com/upload/attachment/0080/3173/638a83ae-3b5c-3f83-9722-2c1912e7cac6.png" alt="" height="352"></p>
<p> </p>
<p>说明：</p>
<ul>
<li>图中的协议4byte header，主要是描述整个binlog网络包的length</li>
<li>binlog event structure，详细信息请参考： <a href="http://forge.mysql.com/wiki/MySQL_Internals_Binary_Log">http://forge.mysql.com/wiki/MySQL_Internals_Binary_Log</a>
</li>
</ul>
<h2>EventSink设计</h2>
<p><img width="647" src="http://dl.iteye.com/upload/attachment/0080/3214/0a2fd671-d6e9-3ed7-b110-6a3b738a3cb0.jpg" alt="" height="240"></p>
<p>说明：</p>
<ul>
<li>数据过滤：支持通配符的过滤模式，表名，字段内容等</li>
<li>数据路由/分发：解决1:n (1个parser对应多个store的模式)</li>
<li>数据归并：解决n:1 (多个parser对应1个store)</li>
<li>数据加工：在进入store之前进行额外的处理，比如join</li>
</ul>
<h3>数据1:n业务</h3>
<p>  为了合理的利用数据库资源， 一般常见的业务都是按照schema进行隔离，然后在mysql上层或者dao这一层面上，进行一个数据源路由，屏蔽数据库物理位置对开发的影响，阿里系主要是通过cobar/tddl来解决数据源路由问题。</p>
<p>  所以，一般一个数据库实例上，会部署多个schema，每个schema会有由1个或者多个业务方关注</p>
<p> </p>
<h3>数据n:1业务</h3>
<p>  同样，当一个业务的数据规模达到一定的量级后，必然会涉及到水平拆分和垂直拆分的问题，针对这些拆分的数据需要处理时，就需要链接多个store进行处理，消费的位点就会变成多份，而且数据消费的进度无法得到尽可能有序的保证。</p>
<p>  所以，在一定业务场景下，需要将拆分后的增量数据进行归并处理，比如按照时间戳/全局id进行排序归并.</p>
<p> </p>
<h2>EventStore设计</h2>
<ul style="color: #333333; font-family: Arial, Helvetica, FreeSans, sans-serif;">
<li style="line-height: 1.5; margin-bottom: 0px; margin-left: 0px;">1.  目前仅实现了Memory内存模式，后续计划增加本地file存储，mixed混合模式</li>
<li style="line-height: 1.5; margin-bottom: 0px; margin-left: 0px;">2.  借鉴了Disruptor的RingBuffer的实现思路</li>
</ul>
<p>RingBuffer设计：</p>
<p><img width="331" src="http://dl.iteye.com/upload/attachment/0080/3237/063e8480-15c8-3e66-bbd3-9c44def09c8f.jpg" alt="" height="303"></p>
<p>定义了3个cursor</p>
<ul>
<li>Put :  Sink模块进行数据存储的最后一次写入位置</li>
<li>Get :  数据订阅获取的最后一次提取位置</li>
<li>Ack :  数据消费成功的最后一次消费位置</li>
</ul>
<p>借鉴Disruptor的RingBuffer的实现，将RingBuffer拉直来看：<br><img width="620" src="http://dl.iteye.com/upload/attachment/0080/3239/4f58912d-7c8e-37de-b7b8-10dd21aa6c22.jpg" alt="" height="165"></p>
<p>实现说明：</p>
<ul>
<li>Put/Get/Ack cursor用于递增，采用long型存储</li>
<li>buffer的get操作，通过取余或者与操作。(与操作： cusor &amp; (size - 1) , size需要为2的指数，效率比较高)</li>
</ul>
<h2>Instance设计</h2>
<p><br><img width="651" src="http://dl.iteye.com/upload/attachment/0080/3247/5de1c9af-7798-3d42-bc43-5c54d82c4dbf.jpg" alt="" height="415"></p>
<p> </p>
<p>instance代表了一个实际运行的数据队列，包括了EventPaser,EventSink,EventStore等组件。</p>
<p>抽象了CanalInstanceGenerator，主要是考虑配置的管理方式：</p>
<ul>
<li>manager方式： 和你自己的内部web console/manager系统进行对接。(目前主要是公司内部使用)</li>
<li>spring方式：基于spring xml + properties进行定义，构建spring配置. </li>
</ul>
<h2>Server设计</h2>
<p><br><img width="498" src="http://dl.iteye.com/upload/attachment/0080/3257/f4df38ba-59e2-398e-b5eb-1bbfbecc0676.jpg" alt="" height="254"></p>
<p>server代表了一个canal的运行实例，为了方便组件化使用，特意抽象了Embeded(嵌入式) / Netty(网络访问)的两种实现</p>
<ul>
<li>Embeded :  对latency和可用性都有比较高的要求，自己又能hold住分布式的相关技术(比如failover)</li>
<li>Netty :  基于netty封装了一层网络协议，由canal server保证其可用性，采用的pull模型，当然latency会稍微打点折扣，不过这个也视情况而定。(阿里系的notify和metaq，典型的push/pull模型，目前也逐步的在向pull模型靠拢，push在数据量大的时候会有一些问题) </li>
</ul>
<h2>增量订阅/消费设计</h2>
<p><img width="427" src="http://dl.iteye.com/upload/attachment/0080/3297/9d7ed13e-6a86-386d-92f4-852238c475bf.jpg" alt="" height="524"></p>
<p>具体的协议格式，可参见：<a href="https://github.com/alibabatech/canal/blob/master/protocol/src/main/java/com/alibaba/otter/canal/protocol/CanalProtocol.proto">CanalProtocol.proto</a></p>
<p>get/ack/rollback协议介绍：</p>
<ul>
<li>Message getWithoutAck(int batchSize)，允许指定batchSize，一次可以获取多条，每次返回的对象为Message，包含的内容为：<br>a. batch id 唯一标识<br>b. entries 具体的数据对象，对应的数据对象格式：<a href="https://github.com/alibabatech/canal/blob/master/protocol/src/main/java/com/alibaba/otter/canal/protocol/EntryProtocol.proto">EntryProtocol.proto</a>
</li>
<li>void rollback(long batchId)，顾命思议，回滚上次的get请求，重新获取数据。基于get获取的batchId进行提交，避免误操作</li>
<li>void ack(long batchId)，顾命思议，确认已经消费成功，通知server删除数据。基于get获取的batchId进行提交，避免误操作</li>
</ul>
<p>canal的get/ack/rollback协议和常规的jms协议有所不同，允许get/ack异步处理，比如可以连续调用get多次，后续异步按顺序提交ack/rollback，项目中称之为流式api. </p>
<p>流式api设计的好处：</p>
<ul>
<li>get/ack异步化，减少因ack带来的网络延迟和操作成本 (99%的状态都是处于正常状态，异常的rollback属于个别情况，没必要为个别的case牺牲整个性能)</li>
<li>get获取数据后，业务消费存在瓶颈或者需要多进程/多线程消费时，可以不停的轮询get数据，不停的往后发送任务，提高并行化.  (作者在实际业务中的一个case：业务数据消费需要跨中美网络，所以一次操作基本在200ms以上，为了减少延迟，所以需要实施并行化)</li>
</ul>
<p>流式api设计：</p>
<p><img width="441" src="http://dl.iteye.com/upload/attachment/0080/3300/7f7986e5-d8c6-3a17-8796-71166e6bc2dc.jpg" alt="" height="277"></p>
<ul>
<li>每次get操作都会在meta中产生一个mark，mark标记会递增，保证运行过程中mark的唯一性</li>
<li>每次的get操作，都会在上一次的mark操作记录的cursor继续往后取，如果mark不存在，则在last ack cursor继续往后取</li>
<li>进行ack时，需要按照mark的顺序进行数序ack，不能跳跃ack. ack会删除当前的mark标记，并将对应的mark位置更新为last ack cusor</li>
<li>一旦出现异常情况，客户端可发起rollback情况，重新置位：删除所有的mark, 清理get请求位置，下次请求会从last ack cursor继续往后取</li>
</ul>
<h3>数据对象格式：<a href="https://github.com/alibabatech/canal/blob/master/protocol/src/main/java/com/alibaba/otter/canal/protocol/EntryProtocol.proto" style="font-size: 14px; line-height: 1.5; color: #bc2a4d; text-decoration: underline;">EntryProtocol.proto</a>
</h3>
<pre name="code" class="java">Entry
	Header
		logfileName [binlog文件名]
		logfileOffset [binlog position]
		executeTime [发生的变更]
		schemaName 
		tableName
		eventType [insert/update/delete类型]
	entryType 	[事务头BEGIN/事务尾END/数据ROWDATA]
	storeValue 	[byte数据,可展开，对应的类型为RowChange]
	
RowChange
	isDdl		[是否是ddl变更操作，比如create table/drop table]
	sql		[具体的ddl sql]
	rowDatas	[具体insert/update/delete的变更数据，可为多条，1个binlog event事件可对应多条变更，比如批处理]
		beforeColumns [Column类型的数组]
		afterColumns [Column类型的数组]
		
Column 
	index		
	sqlType		[jdbc type]
	name		[column name]
	isKey		[是否为主键]
	updated		[是否发生过变更]
	isNull		[值是否为null]
	value		[具体的内容，注意为文本]</pre>
<p>说明：</p>
<ul>
<li>可以提供数据库变更前和变更后的字段内容，针对binlog中没有的name,isKey等信息进行补全</li>
<li>可以提供ddl的变更语句</li>
</ul>
<h2>HA机制设计</h2>
<p>canal的ha分为两部分，canal server和canal client分别有对应的ha实现</p>
<ul>
<li>canal server:  为了减少对mysql dump的请求，不同server上的instance要求同一时间只能有一个处于running，其他的处于standby状态. </li>
<li>canal client: 为了保证有序性，一份instance同一时间只能由一个canal client进行get/ack/rollback操作，否则客户端接收无法保证有序。</li>
</ul>
<p>整个HA机制的控制主要是依赖了zookeeper的几个特性，watcher和EPHEMERAL节点(和session生命周期绑定)，可以看下我之前zookeeper的相关文章。</p>
<p> </p>
<p>Canal Server: </p>
<p><img width="624" src="http://dl.iteye.com/upload/attachment/0080/3303/d3202c26-e954-35c0-a319-57604102c57d.jpg" alt="" height="341"><br>大致步骤：</p>
<ol>
<li><span style="line-height: 21px;">canal server要启动某个canal instance时都先向zookeeper进行一次尝试启动判断  (实现：创建EPHEMERAL节点，谁创建成功就允许谁启动)</span></li>
<li><span style="line-height: 21px;">创建zookeeper节点成功后，对应的canal server就启动对应的canal instance，没有创建成功的canal instance就会处于standby状态</span></li>
<li><span style="line-height: 21px;">一旦zookeeper发现canal server A创建的节点消失后，立即通知其他的canal server再次进行步骤1的操作，重新选出一个canal server启动instance.</span></li>
<li><span style="line-height: 21px;">canal client每次进行connect时，会首先向zookeeper询问当前是谁启动了canal instance，然后和其建立链接，一旦链接不可用，会重新尝试connect.</span></li>
</ol>
<p><span style="line-height: 21px;">Canal Client的方式和canal server方式类似，也是利用zokeeper的抢占</span><span style="line-height: 21px;">EPHEMERAL节点的方式进行控制. </span></p>
<p> </p>
