/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.canal.k2s.starrocks.manager;

import com.alibaba.otter.canal.k2s.starrocks.connection.StarRocksJdbcConnectionOptions;
import com.alibaba.otter.canal.k2s.starrocks.connection.StarRocksJdbcConnectionProvider;
import com.alibaba.otter.canal.k2s.starrocks.sink.StarRocksSinkOptions;
import org.apache.flink.table.api.TableColumn;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.api.constraints.UniqueConstraint;
import org.apache.flink.table.types.logical.LogicalTypeRoot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StarRocksSinkManager implements Serializable {

    private static final long serialVersionUID = 1L;

    private final StarRocksJdbcConnectionProvider jdbcConnProvider;
    private final StarRocksQueryVisitor starrocksQueryVisitor;
    private StarRocksStreamLoadVisitor starrocksStreamLoadVisitor;
    private final StarRocksSinkOptions sinkOptions;

    public StarRocksSinkManager(StarRocksSinkOptions sinkOptions, String[] columnList, List<String> dstPkList) {
        this.sinkOptions = sinkOptions;
        StarRocksJdbcConnectionOptions jdbcOptions = new StarRocksJdbcConnectionOptions(sinkOptions.getJdbcUrl(),
                sinkOptions.getUsername(), sinkOptions.getPassword());
        this.jdbcConnProvider = new StarRocksJdbcConnectionProvider(jdbcOptions);
        this.starrocksQueryVisitor = new StarRocksQueryVisitor(jdbcConnProvider, sinkOptions.getDatabaseName(), sinkOptions.getTableName());
        init(columnList,dstPkList);
    }


    protected void init(String[] columnList, List<String> dstPkList) {
        validateTableStructure(columnList, dstPkList);
        String version = starrocksQueryVisitor.getStarRocksVersion();
        this.starrocksStreamLoadVisitor = new StarRocksStreamLoadVisitor(
                sinkOptions,
                columnList,
                version.length() > 0 && !version.trim().startsWith("1.")
        );
    }

    private void validateTableStructure(String[] columnList, List<String> dstPkList) {

        List<Map<String, Object>> rows = starrocksQueryVisitor.getTableColumnsMetaData();
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("Couldn't get the sink table's column info.");
        }
        // validate primary keys
        List<String> primayKeys = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String keysType = row.get("COLUMN_KEY").toString();
            if (!"PRI".equals(keysType)) {
                continue;
            }
            primayKeys.add(row.get("COLUMN_NAME").toString().toLowerCase());
        }
        if (!primayKeys.isEmpty()) {
            if (dstPkList == null || dstPkList.isEmpty()) {
                throw new IllegalArgumentException("Primary keys not defined in the sink `TableSchema`.");
            }
            if (dstPkList.size() != primayKeys.size() ||
                    !dstPkList.stream().allMatch(col -> primayKeys.contains(col.toLowerCase()))) {
                throw new IllegalArgumentException("Primary keys of the flink `TableSchema` do not match with the ones from starrocks table.");
            }
            sinkOptions.enableUpsertDelete();
        }
    }

    public StarRocksStreamLoadVisitor getStarrocksStreamLoadVisitor() {
        return this.starrocksStreamLoadVisitor;
    }

}
