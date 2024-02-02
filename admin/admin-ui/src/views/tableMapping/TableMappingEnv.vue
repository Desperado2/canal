<template>
  <div class="app-container">
    <div class="filter-container">
      <el-button class="filter-item" type="primary" @click="handleCreate()">新建环境</el-button>
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
      <el-table-column label="环境名称" min-width="200" align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.envName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="环境描述" min-width="300" align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.description }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" prop="created_at" label="操作" min-width="150">
        <template slot-scope="scope">
          <el-dropdown trigger="click">
            <el-button type="primary" size="mini">
              操作<i class="el-icon-arrow-down el-icon--right" />
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="handleUpdate(scope.row)">修改</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="count>0" :total="count" :page.sync="listQuery.page" :limit.sync="listQuery.size" @pagination="fetchData()" />
    <el-dialog :visible.sync="dialogFormVisible" :title="textMap[dialogStatus]" width="600px">
      <el-form ref="dataForm" :rules="rules" :model="tableMappingEnv" label-position="left" label-width="120px" style="width: 400px; margin-left:30px;">
        <el-form-item label="环境编码" prop="envCode">
          <el-input :readonly="dialogStatus==='update'" v-model="tableMappingEnv.envCode" />
        </el-form-item>
        <el-form-item label="环境名称" prop="envName">
          <el-input v-model="tableMappingEnv.envName" />
        </el-form-item>
        <el-form-item label="环境描述" prop="zkHosts">
          <el-input type="textarea" v-model="tableMappingEnv.description" />
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
import { addTableMappingEnv, getTableMappingEnvPageList, updateTableMappingEnv } from '@/api/tableMappingEnv'
import Pagination from "@/components/Pagination";
import {updateNodeServer} from "@/api/nodeServer";

export default {
  components: { Pagination },
  data() {
    return {
      list: null,
      listLoading: true,
      dialogFormVisible: false,
      count: 0,
      listQuery: {
        page: 1,
        size: 10
      },
      tableMappingEnv: {
        id: null,
        envCode: null,
        envName: null,
        description: null
      },
      textMap: {
        create: '新建环境信息',
        update: '修改修改信息'
      },
      rules: {
        envCode: [{ required: true, message: '环境编码不能为空', trigger: 'change' }],
        envName: [{ required: true, message: '环境名称不能为空', trigger: 'change' }]
      },
      dialogStatus: 'create'
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    fetchData() {
      this.listLoading = true
      getTableMappingEnvPageList(this.listQuery).then(res => {
        this.list = res.data.items
        this.count = res.data.count
      }).finally(() => {
        this.listLoading = false
      })
    },
    resetModel() {
      this.tableMappingEnv = {
        id: null,
        envCode: null,
        envName: null,
        description: null
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
            addTableMappingEnv(this.tableMappingEnv).then(res => {
              this.operationRes(res)
            })
          }
          if (this.dialogStatus === 'update') {
            updateTableMappingEnv(this.tableMappingEnv).then(res => {
              this.operationRes(res)
            })
          }
        }
      })
    },
    handleUpdate(row) {
      this.resetModel()
      this.tableMappingEnv = Object.assign({}, row)
      this.dialogStatus = 'update'
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
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
    }
  }
}
</script>
