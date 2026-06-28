<template>
  <div class="template-list">
    <div class="page-header">
      <h2>消息模板配置</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>
        新增模板
      </el-button>
    </div>

    <el-card>
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="模板编码/名称" clearable />
        </el-form-item>
        <el-form-item label="消息类型">
          <el-select v-model="searchForm.msgType" placeholder="全部" clearable style="width: 140px">
            <el-option label="预约消息" :value="1" />
            <el-option label="直播消息" :value="2" />
            <el-option label="客户消息" :value="3" />
            <el-option label="系统消息" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form>

      <el-table :data="templateList" border v-loading="loading">
        <el-table-column prop="templateCode" label="模板编码" width="150" />
        <el-table-column prop="templateName" label="模板名称" min-width="150" />
        <el-table-column prop="msgType" label="类型" width="110">
          <template #default="{ row }">
            <el-tag>{{ getMessageTypeText(row.msgType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="titleTemplate" label="站内信标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="smsTemplate" label="短信模板" min-width="220" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="loadTemplates"
        />
      </div>
    </el-card>

    <el-dialog :title="form.id ? '编辑模板' : '新增模板'" v-model="showDialog" width="720px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="模板编码" required>
          <el-input v-model="form.templateCode" placeholder="如 LIVE_START" />
        </el-form-item>
        <el-form-item label="模板名称" required>
          <el-input v-model="form.templateName" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="消息类型">
          <el-select v-model="form.msgType" style="width: 100%">
            <el-option label="预约消息" :value="1" />
            <el-option label="直播消息" :value="2" />
            <el-option label="客户消息" :value="3" />
            <el-option label="系统消息" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="站内信标题" required>
          <el-input v-model="form.titleTemplate" placeholder="支持 {sessionName} 等变量" />
        </el-form-item>
        <el-form-item label="站内信内容" required>
          <el-input v-model="form.contentTemplate" type="textarea" rows="4" placeholder="请输入站内信内容模板" />
        </el-form-item>
        <el-form-item label="短信内容">
          <el-input v-model="form.smsTemplate" type="textarea" rows="3" placeholder="请输入短信内容模板" />
        </el-form-item>
        <el-form-item label="变量说明">
          <el-input v-model="form.variables" placeholder="sessionName,anchorName,startTime,playUrl,customerName" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-alert
          type="info"
          :closable="false"
          show-icon
          title="开播通知支持变量：{sessionName}、{anchorName}、{startTime}、{playUrl}、{customerName}"
        />
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  addMessageTemplateApi,
  changeMessageTemplateStatusApi,
  deleteMessageTemplateApi,
  getMessageTemplateListApi,
  updateMessageTemplateApi
} from '@/api/message.js'

const searchForm = reactive({ keyword: '', msgType: null, status: null })
const templateList = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const showDialog = ref(false)

const emptyForm = () => ({
  id: null,
  templateCode: '',
  templateName: '',
  msgType: 2,
  titleTemplate: '',
  contentTemplate: '',
  smsTemplate: '',
  variables: 'sessionName,anchorName,startTime,playUrl,customerName',
  status: 1,
  remark: ''
})

const form = reactive(emptyForm())

onMounted(() => {
  loadTemplates()
})

const loadTemplates = async () => {
  loading.value = true
  try {
    const response = await getMessageTemplateListApi(pageNum.value, pageSize.value, {
      keyword: searchForm.keyword || undefined,
      msgType: searchForm.msgType ?? undefined,
      status: searchForm.status ?? undefined
    })
    const data = response.data?.data || {}
    templateList.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
    ElMessage.error('获取模板列表失败')
  } finally {
    loading.value = false
  }
}

const openDialog = (row = null) => {
  Object.assign(form, emptyForm(), row || {})
  showDialog.value = true
}

const handleSave = async () => {
  if (!form.templateCode || !form.templateName || !form.titleTemplate || !form.contentTemplate) {
    ElMessage.warning('请填写模板编码、名称、标题和内容')
    return
  }
  try {
    if (form.id) {
      await updateMessageTemplateApi(form)
    } else {
      await addMessageTemplateApi(form)
    }
    ElMessage.success('保存成功')
    showDialog.value = false
    loadTemplates()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const toggleStatus = async (row) => {
  await changeMessageTemplateStatusApi(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态已更新')
  loadTemplates()
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除模板「${row.templateName}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteMessageTemplateApi(row.id)
      ElMessage.success('删除成功')
      loadTemplates()
    })
    .catch(() => {})
}

const handleSearch = () => {
  pageNum.value = 1
  loadTemplates()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.msgType = null
  searchForm.status = null
  handleSearch()
}

const getMessageTypeText = (type) => ({ 1: '预约消息', 2: '直播消息', 3: '客户消息', 4: '系统消息' }[type] || '未知')
</script>

<style scoped>
.template-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}

.search-form {
  margin-bottom: 20px;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
