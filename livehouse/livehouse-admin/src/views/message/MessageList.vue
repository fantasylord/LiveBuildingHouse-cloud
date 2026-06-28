<template>
  <div class="message-list">
    <div class="page-header">
      <h2>消息管理</h2>
      <div class="header-actions">
        <el-button type="success" @click="openSendDialog">
          <el-icon><Promotion /></el-icon>
          发送站内信
        </el-button>
        <el-button type="primary" @click="openDraftDialog">
          <el-icon><Plus /></el-icon>
          新增草稿
        </el-button>
      </div>
    </div>

    <el-card>
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="消息类型">
          <el-select v-model="searchForm.messageType" placeholder="全部类型" clearable style="width: 150px">
            <el-option label="预约消息" :value="1" />
            <el-option label="直播消息" :value="2" />
            <el-option label="客户消息" :value="3" />
            <el-option label="系统消息" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="发送状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 130px">
            <el-option label="待发送" :value="0" />
            <el-option label="已发送" :value="1" />
            <el-option label="发送失败" :value="2" />
          </el-select>
        </el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form>

      <el-table :data="messageList" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="msgType" label="类型" width="110">
          <template #default="{ row }">
            <el-tag>{{ getMessageTypeText(row.msgType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
        <el-table-column prop="channel" label="渠道" width="100">
          <template #default="{ row }">
            <el-tag type="info">{{ getChannelText(row.channel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="receiverCount" label="收件数" width="90" />
        <el-table-column prop="sendStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.sendStatus)">{{ getStatusText(row.sendStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sendTime" label="发送时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.sendTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" link type="success" @click="openSendDialog(row)">复发</el-button>
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
          @current-change="loadMessages"
        />
      </div>
    </el-card>

    <el-dialog :title="isEdit ? '编辑草稿' : '新增草稿'" v-model="showDraftDialog" width="560px">
      <el-form :model="draftForm" label-width="90px">
        <el-form-item label="消息类型" required>
          <el-select v-model="draftForm.msgType" style="width: 100%">
            <el-option label="预约消息" :value="1" />
            <el-option label="直播消息" :value="2" />
            <el-option label="客户消息" :value="3" />
            <el-option label="系统消息" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="draftForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="draftForm.content" type="textarea" rows="4" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="draftForm.linkUrl" placeholder="可选" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="draftForm.remark" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDraftDialog = false">取消</el-button>
        <el-button type="primary" @click="saveDraft">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog title="发送站内信" v-model="showSendDialog" width="680px">
      <el-form :model="sendForm" label-width="100px">
        <el-form-item label="接收端" required>
          <el-radio-group v-model="sendForm.receiverType" @change="resetTargets">
            <el-radio-button :label="1">客户端</el-radio-button>
            <el-radio-button :label="2">管理端</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发送范围" required>
          <el-radio-group v-model="sendForm.targetType">
            <el-radio label="selected">选择接收人</el-radio>
            <el-radio label="all">全部发送</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="sendForm.targetType === 'selected' && sendForm.receiverType === 2" label="员工" required>
          <el-select v-model="sendForm.receiverIds" multiple filterable placeholder="请选择员工" style="width: 100%">
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="`${user.realName || user.username}（${user.phone || user.username}）`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="sendForm.targetType === 'selected' && sendForm.receiverType === 1" label="客户" required>
          <el-select v-model="sendForm.customerIds" multiple filterable placeholder="请选择客户" style="width: 100%">
            <el-option
              v-for="customer in customerList"
              :key="customer.id"
              :label="`${customer.customerName}（${customer.phone}）`"
              :value="customer.id"
            />
          </el-select>
        </el-form-item>
        <el-alert
          v-if="sendForm.receiverType === 1"
          class="send-tip"
          type="info"
          :closable="false"
          show-icon
          title="客户端消息会按客户手机号匹配已注册 H5 用户，未注册客户会被跳过。"
        />
        <el-form-item label="消息类型" required>
          <el-select v-model="sendForm.message.msgType" style="width: 100%">
            <el-option label="预约消息" :value="1" />
            <el-option label="直播消息" :value="2" />
            <el-option label="客户消息" :value="3" />
            <el-option label="系统消息" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="sendForm.message.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="sendForm.message.content" type="textarea" rows="4" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="sendForm.message.linkUrl" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSendDialog = false">取消</el-button>
        <el-button type="primary" @click="submitSend">立即发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Promotion } from '@element-plus/icons-vue'
import {
  addMessageApi,
  deleteMessageApi,
  getMessageListApi,
  sendUnifiedMessageApi,
  updateMessageApi
} from '@/api/message.js'
import { getUserListApi } from '@/api/user.js'
import { getCustomerListApi } from '@/api/customer.js'

const searchForm = reactive({ messageType: '', status: '' })
const messageList = ref([])
const userList = ref([])
const customerList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const showDraftDialog = ref(false)
const showSendDialog = ref(false)
const isEdit = ref(false)

const emptyMessage = () => ({
  id: null,
  msgType: 4,
  title: '',
  content: '',
  channel: 'system',
  linkUrl: '',
  sendStatus: 0,
  remark: ''
})

const draftForm = reactive(emptyMessage())
const sendForm = reactive({
  receiverType: 1,
  targetType: 'selected',
  receiverIds: [],
  customerIds: [],
  message: emptyMessage()
})

const loadMessages = async () => {
  loading.value = true
  try {
    const response = await getMessageListApi(pageNum.value, pageSize.value, {
      messageType: searchForm.messageType || undefined,
      status: searchForm.status || undefined
    })
    const data = response.data.data
    messageList.value = data?.records || []
    total.value = data?.total || 0
  } catch (error) {
    ElMessage.error('获取消息列表失败')
  } finally {
    loading.value = false
  }
}

const loadTargets = async () => {
  try {
    const [users, customers] = await Promise.all([
      getUserListApi(),
      getCustomerListApi(1, 200)
    ])
    userList.value = users.data?.data || []
    customerList.value = customers.data?.data?.records || []
  } catch (error) {
    ElMessage.warning('接收人列表加载失败')
  }
}

onMounted(() => {
  loadMessages()
  loadTargets()
})

const getMessageTypeText = (type) => ({ 1: '预约消息', 2: '直播消息', 3: '客户消息', 4: '系统消息' }[type] || '未知')
const getChannelText = (channel) => ({ system: '站内信', sms: '短信', push: '推送' }[channel] || channel || '站内信')
const getStatusText = (status) => ({ 0: '待发送', 1: '已发送', 2: '发送失败' }[status] || '未知')
const getStatusType = (status) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[status] || 'info')
const formatDateTime = (value) => value ? String(value).replace('T', ' ').substring(0, 19) : '-'

const handleSearch = () => {
  pageNum.value = 1
  loadMessages()
}

const handleReset = () => {
  searchForm.messageType = ''
  searchForm.status = ''
  handleSearch()
}

const resetDraft = (source = null) => {
  Object.assign(draftForm, emptyMessage(), source || {})
}

const openDraftDialog = () => {
  isEdit.value = false
  resetDraft()
  showDraftDialog.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  resetDraft(row)
  showDraftDialog.value = true
}

const saveDraft = async () => {
  if (!draftForm.title || !draftForm.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  try {
    if (isEdit.value) {
      await updateMessageApi(draftForm)
    } else {
      await addMessageApi(draftForm)
    }
    ElMessage.success('保存成功')
    showDraftDialog.value = false
    loadMessages()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const resetSend = (source = null) => {
  sendForm.receiverType = 1
  sendForm.targetType = 'selected'
  sendForm.receiverIds = []
  sendForm.customerIds = []
  Object.assign(sendForm.message, emptyMessage(), source || {})
  sendForm.message.id = null
}

const resetTargets = () => {
  sendForm.receiverIds = []
  sendForm.customerIds = []
}

const openSendDialog = (row = null) => {
  resetSend(row)
  showSendDialog.value = true
}

const submitSend = async () => {
  if (!sendForm.message.title || !sendForm.message.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  if (sendForm.targetType === 'selected') {
    const emptyAdmin = sendForm.receiverType === 2 && sendForm.receiverIds.length === 0
    const emptyH5 = sendForm.receiverType === 1 && sendForm.customerIds.length === 0
    if (emptyAdmin || emptyH5) {
      ElMessage.warning('请选择接收人')
      return
    }
  }

  try {
    const payload = {
      receiverType: sendForm.receiverType,
      targetType: sendForm.targetType,
      receiverIds: sendForm.receiverType === 2 ? sendForm.receiverIds : [],
      customerIds: sendForm.receiverType === 1 ? sendForm.customerIds : [],
      message: sendForm.message
    }
    const response = await sendUnifiedMessageApi(payload)
    const result = response.data.data || {}
    const skipped = result.skippedCount || 0
    const summary = `发送成功 ${result.successCount || 0} 人${skipped ? `，跳过 ${skipped} 人` : ''}`
    if (skipped && result.skippedCustomers?.length) {
      ElMessage.warning(`${summary}：${result.skippedCustomers.join('、')}`)
    } else {
      ElMessage.success(summary)
    }
    showSendDialog.value = false
    loadMessages()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '发送失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除消息 "${row.title}" 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteMessageApi(row.id)
      ElMessage.success('删除成功')
      loadMessages()
    })
    .catch(() => {})
}
</script>

<style scoped>
.message-list {
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

.header-actions {
  display: flex;
  gap: 10px;
}

.search-form {
  margin-bottom: 20px;
}

.send-tip {
  margin-bottom: 18px;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
