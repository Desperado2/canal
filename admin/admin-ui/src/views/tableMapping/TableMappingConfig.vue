<template>
  <div>
    <el-form ref="form" :model="form">
      <div style="padding-left: 10px;padding-top: 20px;">
        <el-form-item>
          <el-button type="primary" @click="onSubmit">保存</el-button>
          <el-button type="warning" @click="onCancel">重置</el-button>
          <el-button type="success" @click="onLoadTemplate">载入模板</el-button>
          <el-button type="success" @click="showSupportingPaper">配置说明</el-button>
          <el-button type="info" @click="onBack">返回</el-button>
        </el-form-item>
      </div>
      <editor v-model="form.content" lang="json" theme="chrome" width="100%" :height="800" @init="editorInit" />
    </el-form>
    <el-dialog :visible.sync="isShowSupportingPaper" title="配置说明" width="600px">
      针对配置说明如下：<br>

      1.columns: 原表与目标表直接的字段级映射。<br>
      &nbsp;&nbsp; A: srcField: 原表字段名称，除了支持原表的字段之外，还支持以下值：<br>
      <br>
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${DATABASE_NAME}: 表示当前的原表的表名。<br>
      <br>
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${TABLE_NAME}: 表示当前的原表的表名。<br>
      <br>
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${SYNC_TIME}: 表示该条ddl的同步时间。<br>
      <br>
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;func.函数名称: 目标数据库支持的函数。<br>
      <br>
      &nbsp;&nbsp; B: dstField: 目标表的字段名称<br>
      <br>
      <br>

      2.dstField: 目标表的主键字段，值为数组。
      <br>
      <br>

      3.deleteStrategy: 针对删除数据的策略，支持的值为：delete、update
      <br>
      <br>

      4.deleteUpdateField: 如何将删除策略配置为update，则需要指定更新的目标表字段名称。
      <br>
      <br>

      5.deleteUpdateValue: 如何将删除策略配置为update，则需要指定更新的目标表字段值。
      <br>
      <br>

      6.needType: 所需要的数据变更类型，值为数组，支持的值为：insert、update、delete。
      <br>
      <br>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="isShowSupportingPaper = !isShowSupportingPaper">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getTableMapping, updateTableMappingContent,getTableMappingTemplate } from '@/api/tableMapping'
export default {
  components: {
    editor: require('vue2-ace-editor')
  },
  data() {
    return {
      form: {
        id: null,
        content: ''
      },
      isShowSupportingPaper: false
    }
  },
  created() {
    this.loadCanalConfig()
  },
  methods: {
    editorInit() {
      require('brace/ext/language_tools')
      require('brace/mode/html')
      require('brace/mode/yaml')
      require('brace/mode/properties')
      require('brace/mode/javascript')
      require('brace/mode/json')
      require('brace/mode/less')
      require('brace/theme/chrome')
      require('brace/snippets/javascript')
    },
    loadCanalConfig() {
      let mappingId = 0
      if (this.$route.query.mappingId) {
        mappingId = this.$route.query.mappingId
      }
      getTableMapping(mappingId).then(response => {
        const data = response.data
        this.form.id = data.id
        this.form.content = data.content
      })
    },
    onSubmit() {
      if (this.form.content === null || this.form.content === '') {
        this.$message({
          message: '配置内容不能为空',
          type: 'error'
        })
        return
      }
      this.$confirm(
        '修改映射配置可能会导致数据消费变更，是否继续？',
        '确定修改',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        updateTableMappingContent(this.form).then(response => {
          if (response.data === 'success') {
            this.$message({
              message: '保存成功',
              type: 'success'
            })
            this.loadCanalConfig()
          } else {
            this.$message({
              message: '保存失败',
              type: 'error'
            })
          }
        })
      })
    },
    onCancel() {
      this.loadCanalConfig()
    },
    onBack() {
      history.go(-1)
    },
    onLoadTemplate() {
      getTableMappingTemplate().then(res => {
        this.form.content = res.data
      })
    },
    showSupportingPaper(){
      this.isShowSupportingPaper = !this.isShowSupportingPaper
    }
  }
}
</script>

<style scoped>
.line{
  text-align: center;
}
</style>

