<h1>协议变化</h1>
<p>1.   binlog checksum</p>
<p>    mysql5.6之后，支持在binlog对象中增加checksum信息，比如CRC32协议.   <span style="color: #ff0000;">其原理主要是在原先binlog的末尾新增了4个byte，写入一个crc32的校验值</span>. </p>
<p>    对应参数说明： <a style="font-size: 12px; line-height: 1.5;" href="http://dev.mysql.com/doc/refman/5.6/en/replication-options-binary-log.html#sysvar_binlog_checksum">http://dev.mysql.com/doc/refman/5.6/en/replication-options-binary-log.html#sysvar_binlog_checksum</a></p>
<p>    注意：</p>
<ul>
<li>mysql5.6.6之后默认就会开启checksum. </li>
<li>如果canal要开启checksum协议支持，需要设置session参数，目前canal只解析checksum，并没有对checksum进行校验<br><pre name="code" class="java">set @master_binlog_checksum= '@@global.binlog_checksum'  </pre>
</li>
</ul>
<p>2.  INSERT/UPDATE/DELETE协议变化 </p>
<pre name="code" class="java">public static final int    WRITE_ROWS_EVENT_V1      = 23;
public static final int    UPDATE_ROWS_EVENT_V1     = 24;
public static final int    DELETE_ROWS_EVENT_V1     = 25;

/** Version 2 of the Row events */
public static final int    WRITE_ROWS_EVENT         = 30;
public static final int    UPDATE_ROWS_EVENT        = 31;
public static final int    DELETE_ROWS_EVENT        = 32;</pre>
<p>   新增了version 2的协议，主要的变化，就是增加了self check extra的信息，和checksum一样保证数据的完整性. </p>
<p>   对应参数说明： <a style="font-size: 12px; line-height: 1.5;" href="http://dev.mysql.com/doc/refman/5.6/en/replication-options-binary-log.html#sysvar_log_bin_use_v1_row_events">http://dev.mysql.com/doc/refman/5.6/en/replication-options-binary-log.html#sysvar_log_bin_use_v1_row_events</a></p>
<p>   默认值为0，也就是会开启version 2协议，mysql5.5之前默认是version 1协议</p>
<p> </p>
<p>3.  RowsQueryLogEvent事件新增</p>
<p>    对应事件说明： <a style="font-size: 12px; line-height: 1.5;" href="http://dev.mysql.com/worklog/task/?id=5404">http://dev.mysql.com/worklog/task/?id=5404</a>  ，(主要用途：就是在RBR模式下，也可以输出原始执行insert/update/delete的sql信息)</p>
<p>    对应参数说明： <a style="font-size: 12px; line-height: 1.5;" href="http://dev.mysql.com/doc/refman/5.6/en/replication-options-binary-log.html#option_mysqld_binlog-rows-query-log-events">http://dev.mysql.com/doc/refman/5.6/en/replication-options-binary-log.html#option_mysqld_binlog-rows-query-log-events</a></p>
<p>    默认值为false，代表不开启。 如果设置为true，对应的一个事务中的LogEvent事件就会变为： (RowsQuery会出现在tableMap协议之前)</p>
<pre name="code" class="java">Query :  Begin 
RowsQuery:   insert/update/delete sql
TableMap : 
Rows :  Write/Update/DELETE
Query/XId </pre>
<p> </p>
<p>4.  其他协议变化</p>
<ul>
<li>HEARTBEAT_LOG_EVENT      = 27   ##主要用途：在mysql idle期间，发送一些heartbeat事件，对应事件的内容是上一次最后发送的LogEvent信息</li>
<li>IGNORABLE_LOG_EVENT = 28  ## 可忽略的logEvent事件概念，这是mysql为了后续协议扩展引入的，在低版本mysql发现无法识别LogEvent时，可根据LOG_EVENT_IGNORABLE_F标志来判断是否可以直接丢弃. </li>
<li>GTID_LOG_EVENT           = 33 </li>
<li>ANONYMOUS_GTID_LOG_EVENT = 34</li>
<li>PREVIOUS_GTIDS_LOG_EVENT = 35</li>
</ul>
<p>   目前gtid协议只是解析，并没有使用GTID发起COM_BINLOG_DUMP，后续会考虑支持. </p>
<p>5.  新增type :  TIME2/DATETIME2/TIMESTAMP2</p>
<pre name="code" class="java">    public static final int    MYSQL_TYPE_TIMESTAMP2    = 17;
    public static final int    MYSQL_TYPE_DATETIME2     = 18;
    public static final int    MYSQL_TYPE_TIME2         = 19;</pre>
<p> 新增了3种mysql type类型，和5.5之前的有不同的存储格式，最可恶的是居然是采用了Big-Endian，和之前的所有事件解析litten-Endian形成一个对比，不知道mysql那帮人怎么想的</p>
<p> </p>
<h1>测试</h1>
<p>1.  mysql版本: 5.6.10</p>
<p>2.  mysql server配置 : </p>
<pre name="code" class="java">server-id=1
binlog-checksum=CRC32
#binlog-checksum=NONE
master-verify-checksum=1
slave-sql-verify-checksum=1
log-bin=mysql-bin
binlog-format=ROW
binlog-rows-query-log-events=true
log-bin-use-v1-row-events=1
binlog_cache_size=2M
max_binlog_size=512M
sync_binlog=0
character-set-server = utf8
#default-character-set = utf8
collation-server = utf8_unicode_ci
[mysql]
default-storage-engine=INNODB
default-character-set=utf8</pre>
<p> 3. 测试注意(需要设置<span style="font-family: monospace; font-size: 1em; line-height: 1.5;">master_binlog_checksum变量，和mysql server保持一致</span><span style="font-size: 12px; line-height: 1.5;">)</span></p>
<pre name="code" class="java">Connection connection = DriverManager.getConnection("jdbc:mysql://10.20.144.34:3306", "root", "root");
Statement statement = connection.createStatement();
statement.execute("SET @master_binlog_checksum='@@global.binlog_checksum'");</pre>
<p> </p>
<p> </p>
</div>
    

  </div>
</div>