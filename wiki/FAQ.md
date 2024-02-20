# Frequently Asked Quesionts

Q1: Why INSERT/UPDATE/DELETE is parsed as a Query or DDL?

A1: Generally, it's because the binlog is a Query event. For example:

   	1. Binlog format is configured as statement mode or mixed mode . You could check with `show variables like 'binlog_format' ` . For the statement or mixed mode, DML will exist as a SQL.
   	2. For MySQL5.6+ binlog row mode, there is a bool variable for DML to control the record of raw SQL: binlog-rows-query-log-events=true (check by `show variables`). Corresponding event for binlog is RowsQueryLogEvent, also keeping row records. Via properties settings in ps.canal, users could set filters: `canal.instance.filter.query.dml=true`

-------------------

Q2: I set filters for the table but it doesn't work.

A2: 

1. Please step to [AdminGuide](https://github.com/alibaba/canal/wiki/AdminGuide) for the details of canal.instance.filter.regex.

   ```
   MySQL parses the table and Perl supports regex.
   Use comma(,) for multiple regex, double backslash(\\) for escape characters.
   Examples:
   1. All the tables: .* or .*\\..*
   2. All the tables in canal scheme: canal\\..*
   3. Tables starting with canal in canal scheme: canal\\.canal.*
   4. Access a table in canal scheme: canal.test1
   5. A combination of multiple rules: canal\\..*, mysql.test1, mysql.test2 (with comma as delimeter)
   ```

2. Check with the format of binlog. Filters will be valid only for data in row mode. (Mixed/statement doesn't parse SQL and thus cannot exactly extract tableName for filtering.)

3. Check whether CanalConnectors have called subscribe(filter) method. If so, filters should be consistent with the canal.instance.filter.regex in instance.properties, otherwise filters of subscribe will overwrite the settings in the instance. Please NOTE that, if the filter for subscribe is .\*/..\*, then you have consumed all the data updated.

4. For canal version 1.1.3+, it will log the most recent filter condition. You can see if the filter is as expected, if not, please check with Step 3

   ```
   c.a.o.canal.parse.inbound.mysql.dbsync.LogEventConvert - --> init table filter : ^.*\..*$
   c.a.o.canal.parse.inbound.mysql.dbsync.LogEventConvert - --> init table black filter :
   ```

5. See the [Issues](https://github.com/alibaba/canal/issues?utf8=%E2%9C%93&q=subscribe). Some of the problems have already been solved, such as double backslash problems and expression incorrectness.

---------------

Q3: Does canal support binlog subscribe of aliyun rds?

A3: Yes. See Issue [#727](https://github.com/alibaba/canal/issues/727).

-----------------

Q4: How is the overall performance of canal?

A4: See Issue [#726](<https://github.com/alibaba/canal/issues/726) and [Performance Documents](https://github.com/alibaba/canal/wiki/Performance).

----------------

Q5: “batchId:73 is not the firstly:72" or "clientId:1001 batchId:50560 is not exist , please check”

A5:

1. First problem is with the client ack: the first two exceptions are caused by improper ack sequence to batchId. 
2. The second problem is that batchId of ack got cleaned by the server. There are only two reasons for server clean:
   - client has set up a rollback
   - server has launched an instance restart (e.g. scan=true causes automatic restart when file changed)

--------

Q6: "Received error packet: errno = 1236, sqlstate = HY000 errmsg = Could not find first log file name in binary log index file"

A6: This is a dump exception. We'd suggest google errno/errmsg, since it is by MySQL standards to prompt corresponding binlog position does not exist. It is likely to be cleaned by MySQL server. Solution: reset canal subscribe position.
