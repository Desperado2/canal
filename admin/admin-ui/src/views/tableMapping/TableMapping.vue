<template>
  <div class="app-container">
    <div class="filter-container">
      <el-button class="filter-item" type="primary" @click="handleCreate()">新建映射</el-button>
    </div>
    <el-table
      v-loading="listLoading"
      :data="list"
      element-loading-text="Loading"
      border
      fit
      highlight-current-row
    >
      <el-table-column label="环境编码" min-width="200" align="center">
        <template slot-scope="scope">
          {{ scope.row.envCode }}
        </template>
      </el-table-column>
      <el-table-column label="原始库" min-width="200" align="center">
        <template slot-scope="scope">
          {{ scope.row.srcDatabase }}
        </template>
      </el-table-column>
      <el-table-column label="原始表" min-width="200" align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.srcTable }}</span>
        </template>
      </el-table-column>
      <el-table-column label="目标库" min-width="100" align="center">
        <template slot-scope="scope">
          {{ scope.row.dstDatabase }}
        </template>
      </el-table-column>
      <el-table-column label="目标表" min-width="100" align="center">
        <template slot-scope="scope">
          {{ scope.row.dstTable }}
        </template>
      </el-table-column>
      <el-table-column align="center" prop="created_at" label="操作" min-width="150">
        <template slot-scope="scope">
          <el-dropdown trigger="click">
            <el-button type="primary" size="mini">
              操作<i class="el-icon-arrow-down el-icon--right" />
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="handleConfig(scope.row)">配置</el-dropdown-item>
              <el-dropdown-item @click.native="handleUpdate(scope.row)">修改</el-dropdown-item>
              <el-dropdown-item @click.native="handleDelete(scope.row)">删除</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="count>0" :total="count" :page.sync="listQuery.page" :limit.sync="listQuery.size" @pagination="fetchData()" />
    <el-dialog :visible.sync="dialogFormVisible" :title="textMap[dialogStatus]" width="600px">
      <el-form ref="dataForm" :rules="rules" :model="nodeModel" label-position="left" label-width="120px" style="width: 400px; margin-left:30px;">
        <el-form-item label="所属环境" prop="clusterId">
          <el-select  v-model="nodeModel.envCode" placeholder="选择所属集群">
            <el-option v-for="item in tableMappingEnv" :key="item.envCode" :label="item.envName" :value="item.envCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="源库" prop="srcDatabase">
          <el-input v-model="nodeModel.srcDatabase" placeholder="支持表达式"  />
        </el-form-item>
        <el-form-item label="源表" prop="srcTable">
          <el-input v-model="nodeModel.srcTable"  placeholder="支持表达式"  />
        </el-form-item>
        <el-form-item label="目标库" prop="dstDatabase">
          <el-input v-model="nodeModel.dstDatabase"  />
        </el-form-item>
        <el-form-item label="目标表" prop="dstTable">
          <el-input v-model="nodeModel.dstTable"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="dataOperation()">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { addTableMapping, getTableMappingPageList, updateTableMapping, deleteTableMapping } from '@/api/tableMapping'
import { getTableMappingEnvList } from '@/api/tableMappingEnv'
import Pagination from '@/components/Pagination'

export default {
  components: { Pagination },
  data() {
    return {
      list: null,
      listLoading: true,
      tableMappingEnv: [],
      count: 0,
      listQuery: {
        page: 1,
        size: 20
      },
      dialogFormVisible: false,
      dialogInstances: false,
      textMap: {
        create: '新建映射信息',
        update: '修改映射信息'
      },
      nodeModel: {
        id: undefined,
        envCode: null,
        srcDatabase: null,
        srcTable: null,
        dstDatabase: null,
        dstTable: null,
        content: null
      },
      rules: {
        envCode: [{ required: true, message: '环境名称不能为空', trigger: 'change' }],
        srcDatabase: [{ required: true, message: '源库不能为空', trigger: 'change' }],
        srcTable: [{ required: true, message: '源表不能为空', trigger: 'change' }],
        dstDatabase: [{ required: true, message: '目标库不能为空', trigger: 'change' }],
        dstTable: [{ required: true, message: '目标表不能为空', trigger: 'change' }]
      },
      dialogStatus: 'create'
    }
  },
  created() {
    getTableMappingEnvList().then((res) => {
      this.tableMappingEnv = res.data
    })
    this.fetchData()
  },
  methods: {
    fetchData() {
      this.listLoading = true
      getTableMappingPageList(this.listQuery).then(res => {
        this.list = res.data.items
        this.count = res.data.count
      }).finally(() => {
        this.listLoading = false
      })
    },
    queryData() {
      this.listQuery.page = 1
      this.fetchData()
    },
    resetModel() {
      this.nodeModel = {
        id: undefined,
        envCode: null,
        srcDatabase: null,
        srcTable: null,
        dstDatabase: null,
        dstTable: null,
        content: null
      }
    },
    handleCreate() {
      this.resetModel()
      this.dialogStatus = 'create'
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },
    dataOperation() {
      this.$refs['dataForm'].validate((valid) => {
        if (valid) {
          if (this.dialogStatus === 'create') {
            addTableMapping(this.nodeModel).then(res => {
              this.operationRes(res)
            })
          }
          if (this.dialogStatus === 'update') {
            updateTableMapping(this.nodeModel).then(res => {
              this.operationRes(res)
            })
          }
        }
      })
    },
    operationRes(res) {
      if (res.data === 'success') {
        this.fetchData()
        this.dialogFormVisible = false
        this.$message({
          message: this.textMap[this.dialogStatus] + '成功',
          type: 'success'
        })
      } else {
        this.$message({
          message: this.textMap[this.dialogStatus] + '失败',
          type: 'error'
        })
      }
    },
    handleConfig(row) {
      this.$router.push('/tableMapping/tableMapping/config?mappingId=' + row.id)
    },
    handleUpdate(row) {
      this.resetModel()
      this.nodeModel = Object.assign({}, row)
      this.dialogStatus = 'update'
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },
    handleDelete(row) {
      this.$confirm('删除数据映射会导致数据丢失', '确定删除映射信息', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteTableMapping(row.id).then((res) => {
          if (res.data === 'success') {
            this.fetchData()
            this.$message({
              message: '删除信息成功',
              type: 'success'
            })
          } else {
            this.$message({
              message: '删除信息失败',
              type: 'error'
            })
          }
        })
      })
    },
  }
}
</script>
