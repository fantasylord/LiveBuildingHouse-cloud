<template>
  <div class="sms-record-list">
    <div class="page-header">
      <h2>短信记录</h2>
      <el-button type="primary" @click="handleSendSms">
        <el-icon><Plus /></el-icon>
        发送短信
      </el-button>
    </div>

    <el-card>
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="发送状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="待发送" :value="0" />
            <el-option label="已发送" :value="1" />
            <el-option label="发送失败" :value="2" />
          </el-select>
        </el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form>

      <el-table :data="smsRecordList" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="templateCode" label="模板编码" width="130" />
        <el-table-column prop="content" label="短信内容" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误信息" show-overflow-tooltip />
        <el-table-column prop="sendTime" label="发送时间" width="180">
          <template #default="scope">{{ formatDateTime(scope.row.sendTime) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button size="small" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next, jumper"
        @current-change="loadSmsRecords"
        style="margin-top: 20px; text-align: right;"
      />
    </el-card>

    <el-dialog title="发送短信" v-model="showSendDialog" width="480px">
      <el-form :model="sendForm" label-width="80px">
        <el-form-item label="手机号" required>
          <el-input v-model="sendForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="模板编码">
          <el-input v-model="sendForm.templateCode" placeholder="可选" />
        </el-form-item>
        <el-form-item label="短信内容" required>
          <el-input v-model="sendForm.content" type="textarea" rows="4" placeholder="请输入短信内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSendDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSendSubmit">确定发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { deleteSmsRecordApi, getSmsRecordListApi, sendSmsApi } from '@/api/message.js'

const searchForm = reactive({ phone: '', status: '' })
const smsRecordList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const showSendDialog = ref(false)

const sendForm = reactive({
  phone: '',
  templateCode: '',
  content: ''
})

const loadSmsRecords = async () => {
  loading.value = true
  try {
    const response = await getSmsRecordListApi(pageNum.value, pageSize.value, {
      phone: searchForm.phone || undefined,
      status: searchForm.status || undefined
    })
    const data = response.data.data
    smsRecordList.value = data?.records || []
    total.value = data?.total || 0
  } catch (error) {
    ElMessage.error('获取短信记录失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadSmsRecords)

const getStatusText = (status) => {
  return { 0: '待发送', 1: '已发送', 2: '发送失败' }[status] || '未知'
}

const getStatusType = (status) => {
  return { 0: 'warning', 1: 'success', 2: 'danger' }[status] || 'info'
}

const formatDateTime = (value) => value ? String(value).replace('T', ' ').substring(0, 19) : '-'

const handleSearch = () => {
  pageNum.value = 1
  loadSmsRecords()
}

const handleReset = () => {
  searchForm.phone = ''
  searchForm.status = ''
  handleSearch()
}

const handleSendSms = () => {
  sendForm.phone = ''
  sendForm.templateCode = ''
  sendForm.content = ''
  showSendDialog.value = true
}

const handleSendSubmit = async () => {
  if (!sendForm.phone || !sendForm.content) {
    ElMessage.warning('请填写手机号和短信内容')
    return
  }
  try {
    await sendSmsApi(sendForm.phone, sendForm.templateCode, sendForm.content)
    ElMessage.success('发送成功')
    showSendDialog.value = false
    loadSmsRecords()
  } catch (error) {
    ElMessage.error('发送失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除该短信记录吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteSmsRecordApi(row.id)
      ElMessage.success('删除成功')
      loadSmsRecords()
    })
    .catch(() => {})
}
</script>

<style scoped>
.sms-record-list {
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
</style>