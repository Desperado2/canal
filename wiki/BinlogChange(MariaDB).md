 <div id="blog_content" class="blog_content">
    <div class="iteye-blog-content-contain">
<p style="font-size: 14px;">本文主要是介绍一下canal支持mariadb协议上的变化. </p>
<p style="font-size: 14px;"> </p>
<h1>协议变化</h1>
<h2 style="font-size: 14px;">mariadb5.5</h2>
<p style="font-size: 14px;">mariadb5.5主要是基于mysql5.5的原型，类型定义基本没啥变化，大体上都保持兼容</p>
<p style="font-size: 14px;">主要的变化：</p>
<p style="font-size: 14px;">1. QueryLogEvent增加了status变量.    </p>
<ul>
<li>Q_HRNOW  用于记录毫秒的精度，枚举值下标为128</li>
</ul>
<p>协议解析的时候，需要处理Q_HRNOW，需要跳过3字节的数据.  </p>
<p>ps.  mysql5.6后，新增了Q_MICROSECONDS来支持mariaDb中Q_HRNOW的毫秒精度的功能.  </p>
<p> </p>
<p>2. binlog事件的变化</p>
<pre name="code" class="java">  /* New MySQL/Sun events are to be added right above this comment */
  MYSQL_EVENTS_END,

  MARIA_EVENTS_BEGIN= 160,
  /* New Maria event numbers start from here */
  ANNOTATE_ROWS_EVENT= 160,

  /* Add new MariaDB events here - right above this comment!  */

  ENUM_END_EVENT /* end marker */</pre>
<p>新增了mariadb的binlog区间为160开始，<span style="font-family: monospace; font-size: 1em; line-height: 1.5;">ANNOTATE_ROWS_EVENT类型为mysql5.6中的</span><span style="font-family: monospace;">RowsQueryLogEvent，用于记录RBR模式下insert/update/delete中执行的sql.</span></p>
<p style="font-size: 14px;"> </p>
<h2 style="font-size: 14px;">mariadb10</h2>
<p>mariadb10主要是基于mysql5.6的原型，类型定义基本没啥变化，大体上都保持兼容(沿用了mysql5.6中TIMESTAMP2等新的时间类型和新的log_event类型)</p>
<p>主要的变化：</p>
<p>1. QueryLogEvent增加了status变量.    </p>
<ul>
<li>Q_HRNOW  用于记录毫秒的精度，枚举值下标为128</li>
</ul>
<p>协议解析的时候，需要处理Q_HRNOW，需要跳过3字节的数据.  </p>
<p>ps.  mysql5.6后，新增了Q_MICROSECONDS来支持mariaDb中Q_HRNOW的毫秒精度的功能.  </p>
<p> </p>
<p>2. binlog事件的变化</p>
<pre name="code" class="java">MARIA_EVENTS_BEGIN= 160,
  /* New Maria event numbers start from here */
  ANNOTATE_ROWS_EVENT= 160,
  /*
    Binlog checkpoint event. Used for XA crash recovery on the master, not used
    in replication.
    A binlog checkpoint event specifies a binlog file such that XA crash
    recovery can start from that file - and it is guaranteed to find all XIDs
    that are prepared in storage engines but not yet committed.
  */
  BINLOG_CHECKPOINT_EVENT= 161,
  /*
    Gtid event. For global transaction ID, used to start a new event group,
    instead of the old BEGIN query event, and also to mark stand-alone
    events.
  */
  GTID_EVENT= 162,
  /*
    Gtid list event. Logged at the start of every binlog, to record the
    current replication state. This consists of the last GTID seen for
    each replication domain.
  */
  GTID_LIST_EVENT= 163,

  /* Add new MariaDB events here - right above this comment!  */

  ENUM_END_EVENT /* end marker */</pre>
<p> 新增了mariadb自己的gtid处理</p>
<p> </p>
<h1>使用注意</h1>
<p>1.  AnnotateRowsEvent使用</p>
<ul>
<li> mariadb需要在my.cnf中设置binlog_annotate_row_events = true，开启记录annotate事件</li>
<li>canal在发送COM_BINLOG_DUMP指令中需要设置binlog_flags |= BINLOG_SEND_ANNOTATE_ROWS_EVENT，不然mariadb默认不会发送AnnotateRowsEvent，而是以空的QueryLogEvent来代替.</li>
</ul>
<p>2.  新增的binlog类型使用</p>
<ul>
<li>canal需要设置当前session变量<br><pre name="code" class="java">SET @mariadb_slave_capability='" + LogEvent.MARIA_SLAVE_CAPABILITY_MINE + "'"</pre>
</li>
</ul>
</div>
  </div>