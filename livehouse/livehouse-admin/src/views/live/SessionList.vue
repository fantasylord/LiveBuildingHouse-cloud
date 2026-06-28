<template>
  <div class="session-list">
    <div class="page-header">
      <h2>直播场次管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAddClick">新增场次</el-button>
    </div>

    <el-card>
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="场次名称">
          <el-input v-model="searchForm.sessionName" clearable placeholder="请输入场次名称" />
        </el-form-item>
        <el-form-item label="直播平台">
          <el-select v-model="searchForm.platformId" clearable placeholder="请选择平台" style="width: 160px">
            <el-option v-for="item in platformOptions" :key="item.id" :label="item.platformName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.liveType" clearable placeholder="请选择类型" style="width: 140px">
            <el-option label="公开直播" :value="1" />
            <el-option label="VIP私密" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" clearable placeholder="请选择状态" style="width: 140px">
            <el-option label="未开始" :value="0" />
            <el-option label="直播中" :value="1" />
            <el-option label="已结束" :value="2" />
            <el-option label="已关闭" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="sessionList" border>
        <el-table-column prop="sessionName" label="场次名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="平台" width="140">
          <template #default="{ row }">{{ getPlatformName(row.platformId) }}</template>
        </el-table-column>
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="row.liveType === 1 ? 'success' : 'warning'">
              {{ row.liveType === 1 ? '公开' : 'VIP私密' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="anchorName" label="主播" width="120" />
        <el-table-column label="计划开始" width="170">
          <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="计划结束" width="170">
          <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="430" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button size="small" link type="primary" @click="handlePushInfo(row)">推流</el-button>
            <el-button size="small" link type="primary" @click="handleSyncStatus(row)">同步</el-button>
            <el-button size="small" link type="primary" @click="handleReplay(row)">回放</el-button>
            <el-button v-if="row.status === 0" size="small" link type="success" @click="handleStart(row)">开始直播</el-button>
            <el-button v-if="row.status === 1" size="small" link type="primary" @click="openNotifyDialog(row)">通知</el-button>
            <el-button v-if="row.status === 1" size="small" link type="warning" @click="handleStop(row)">结束直播</el-button>
            <el-button size="small" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @current-change="loadSessionList"
        @size-change="handleSizeChange"
      />
    </el-card>

    <el-dialog :title="isEdit ? '编辑直播场次' : '新增直播场次'" v-model="showAddModal" width="680px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="场次名称" required>
          <el-input v-model="form.sessionName" placeholder="请输入场次名称" />
        </el-form-item>
        <el-form-item label="直播封面">
          <el-input v-model="form.coverImage" placeholder="请输入封面图片地址" />
        </el-form-item>
        <el-form-item label="直播平台" required>
          <el-select v-model="form.platformId" placeholder="请选择直播平台" style="width: 100%">
            <el-option v-for="item in platformOptions" :key="item.id" :label="item.platformName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="直播类型" required>
          <el-select v-model="form.liveType" style="width: 100%">
            <el-option label="公开直播" :value="1" />
            <el-option label="VIP私密直播" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="主播名称">
          <el-input v-model="form.anchorName" placeholder="请输入主播名称" />
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.liveType === 2" label="观看密码">
          <el-input v-model="form.password" placeholder="请输入观看密码" />
        </el-form-item>
        <el-form-item label="直播简介">
          <el-input v-model="form.introduction" type="textarea" rows="3" placeholder="请输入直播简介" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog title="直播详情" v-model="showDetailModal" width="760px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="场次名称">{{ detailData.sessionName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="场次编码">{{ detailData.sessionCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="平台">{{ getPlatformName(detailData.platformId) }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detailData.liveType === 1 ? '公开直播' : 'VIP私密直播' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusText(detailData.status) }}</el-descriptions-item>
        <el-descriptions-item label="主播">{{ detailData.anchorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="计划开始">{{ formatDateTime(detailData.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="计划结束">{{ formatDateTime(detailData.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="实际开始">{{ formatDateTime(detailData.actualStartTime) }}</el-descriptions-item>
        <el-descriptions-item label="实际结束">{{ formatDateTime(detailData.actualEndTime) }}</el-descriptions-item>
        <el-descriptions-item label="观看人数">{{ detailData.totalViewer || 0 }}</el-descriptions-item>
        <el-descriptions-item label="最高在线">{{ detailData.maxViewer || 0 }}</el-descriptions-item>
        <el-descriptions-item label="预约人数">{{ detailData.reserveCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="留资人数">{{ detailData.leaveCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="简介" :span="2">{{ detailData.introduction || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="showDetailModal = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog title="推流信息" v-model="showPushModal" width="760px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="场次名称">{{ pushInfo.sessionName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusText(pushInfo.status) }}</el-descriptions-item>
        <el-descriptions-item label="StreamId">{{ pushInfo.streamId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="推流地址">{{ pushInfo.pushUrl || '-' }}</el-descriptions-item>
        <el-descriptions-item label="播放地址">{{ pushInfo.playUrl || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div class="metric-row">
        <el-statistic title="累计观看" :value="pushInfo.totalViewer || 0" />
        <el-statistic title="最高在线" :value="pushInfo.maxViewer || 0" />
        <el-statistic title="预约人数" :value="pushInfo.reserveCount || 0" />
        <el-statistic title="留资人数" :value="pushInfo.leaveCount || 0" />
      </div>
      <template #footer>
        <el-button @click="showPushModal = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog title="回放管理" v-model="showReplayModal" width="640px">
      <el-form :model="replayForm" label-width="100px">
        <el-form-item label="回放状态">
          <el-select v-model="replayForm.replayStatus" style="width: 100%">
            <el-option label="未录制" :value="0" />
            <el-option label="录制中" :value="1" />
            <el-option label="已完成" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="回放地址">
          <el-input v-model="replayForm.replayUrl" type="textarea" rows="3" placeholder="请输入或粘贴回放地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReplayModal = false">取消</el-button>
        <el-button type="danger" :disabled="!replayForm.id" @click="handleDeleteReplay">删除回放</el-button>
        <el-button type="primary" @click="handleSaveReplay">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog title="开播通知" v-model="showNotifyModal" width="680px">
      <el-alert
        title="请选择接收客户和通知方式。站内信只发送给已注册H5用户，短信会写入短信发送记录。"
        type="info"
        show-icon
        :closable="false"
        class="notify-tip"
      />
      <el-form :model="notifyForm" label-width="110px">
        <el-form-item label="直播场次">
          <el-input :model-value="notifySession.sessionName || '-'" disabled />
        </el-form-item>
        <el-form-item label="消息模板" required>
          <el-select v-model="notifyForm.templateCode" placeholder="请选择消息模板" style="width: 100%">
            <el-option
              v-for="item in templateOptions"
              :key="item.templateCode"
              :label="`${item.templateName}（${item.templateCode}）`"
              :value="item.templateCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="接收客户" required>
          <el-select
            v-model="notifyForm.customerIds"
            multiple
            filterable
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择客户"
            style="width: 100%"
          >
            <el-option
              v-for="item in customerOptions"
              :key="item.id"
              :label="`${item.customerName || item.name || '未命名客户'} ${item.phone || ''}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="通知方式" required>
          <el-checkbox v-model="notifyForm.sendSystemMessage">站内信</el-checkbox>
          <el-checkbox v-model="notifyForm.sendSms">短信</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showNotifyModal = false">取消</el-button>
        <el-button type="primary" :loading="notifyLoading" @click="submitNotify">发送通知</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getSessionListApi,
  addSessionApi,
  updateSessionApi,
  deleteSessionApi,
  startLiveApi,
  notifyLiveStartApi,
  stopLiveApi,
  getPlatformOptionsApi,
  syncSessionStatusApi,
  getSessionPushInfoApi,
  updateReplayApi,
  deleteReplayApi
} from '@/api/live.js'
import { getCustomerListApi } from '@/api/customer.js'
import { getMessageTemplateListApi } from '@/api/message.js'

const searchForm = reactive({
  sessionName: '',
  liveType: null,
  status: null,
  platformId: null
})

const sessionList = ref([])
const platformOptions = ref([])
const customerOptions = ref([])
const templateOptions = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const notifyLoading = ref(false)
const showAddModal = ref(false)
const showDetailModal = ref(false)
const showPushModal = ref(false)
const showReplayModal = ref(false)
const showNotifyModal = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  sessionName: '',
  coverImage: '',
  liveType: 1,
  platformId: null,
  anchorName: '',
  startTime: '',
  endTime: '',
  password: '',
  introduction: '',
  remark: ''
})

const detailData = reactive({})
const pushInfo = reactive({})

const replayForm = reactive({
  id: null,
  replayUrl: '',
  replayStatus: 0
})

const notifySession = reactive({
  id: null,
  sessionName: ''
})

const notifyForm = reactive({
  customerIds: [],
  templateCode: 'LIVE_START',
  sendSystemMessage: true,
  sendSms: false
})

onMounted(() => {
  loadSessionList()
  loadPlatformOptions()
  loadNotifyOptions()
})

const loadPlatformOptions = async () => {
  try {
    const response = await getPlatformOptionsApi(1)
    if (response.data?.code === 200) {
      platformOptions.value = response.data.data || []
    }
  } catch (error) {
    console.error('加载直播平台失败:', error)
  }
}

const loadNotifyOptions = async () => {
  try {
    const [customerRes, templateRes] = await Promise.all([
      getCustomerListApi(1, 500),
      getMessageTemplateListApi(1, 100, { msgType: 2, status: 1 })
    ])
    customerOptions.value = customerRes.data?.data?.records || []
    templateOptions.value = templateRes.data?.data?.records || []
    if (!templateOptions.value.some(item => item.templateCode === notifyForm.templateCode) && templateOptions.value.length > 0) {
      notifyForm.templateCode = templateOptions.value[0].templateCode
    }
  } catch (error) {
    console.error('加载开播通知选项失败:', error)
  }
}

const loadSessionList = async () => {
  loading.value = true
  try {
    const response = await getSessionListApi(pageNum.value, pageSize.value, {
      sessionName: searchForm.sessionName || undefined,
      liveType: searchForm.liveType || undefined,
      status: searchForm.status || undefined,
      platformId: searchForm.platformId || undefined
    })
    if (response.data?.code === 200) {
      const pageData = response.data.data || {}
      sessionList.value = pageData.records || []
      total.value = pageData.total || 0
    }
  } catch (error) {
    ElMessage.error('获取直播场次失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const formatDateTime = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ').substring(0, 19)
}

const getPlatformName = (platformId) => {
  const platform = platformOptions.value.find(item => item.id === platformId)
  return platform?.platformName || '-'
}

const getStatusTagType = (status) => {
  return ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' })[status] || 'info'
}

const getStatusText = (status) => {
  return ({ 0: '未开始', 1: '直播中', 2: '已结束', 3: '已关闭' })[status] || '未知'
}

const handleSearch = () => {
  pageNum.value = 1
  loadSessionList()
}

const handleReset = () => {
  searchForm.sessionName = ''
  searchForm.liveType = null
  searchForm.status = null
  searchForm.platformId = null
  handleSearch()
}

const handleSizeChange = () => {
  pageNum.value = 1
  loadSessionList()
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    sessionName: '',
    coverImage: '',
    liveType: 1,
    platformId: null,
    anchorName: '',
    startTime: '',
    endTime: '',
    password: '',
    introduction: '',
    remark: ''
  })
}

const handleAddClick = () => {
  isEdit.value = false
  resetForm()
  showAddModal.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  resetForm()
  Object.assign(form, {
    id: row.id,
    sessionName: row.sessionName || '',
    coverImage: row.coverImage || '',
    platformId: row.platformId || null,
    liveType: row.liveType || 1,
    anchorName: row.anchorName || '',
    startTime: formatFormTime(row.startTime),
    endTime: formatFormTime(row.endTime),
    password: row.password || '',
    introduction: row.introduction || '',
    remark: row.remark || ''
  })
  showAddModal.value = true
}

const formatFormTime = (value) => {
  if (!value) return ''
  return String(value).replace('T', ' ').substring(0, 19)
}

const handleDetail = (row) => {
  Object.keys(detailData).forEach(key => delete detailData[key])
  Object.assign(detailData, row)
  showDetailModal.value = true
}

const handlePushInfo = async (row) => {
  try {
    const response = await getSessionPushInfoApi(row.id)
    if (response.data?.code === 200) {
      Object.keys(pushInfo).forEach(key => delete pushInfo[key])
      Object.assign(pushInfo, response.data.data || {})
      showPushModal.value = true
    }
  } catch (error) {
    ElMessage.error('获取推流信息失败')
    console.error(error)
  }
}

const handleSyncStatus = async (row) => {
  try {
    await syncSessionStatusApi(row.id)
    ElMessage.success('同步完成')
    loadSessionList()
  } catch (error) {
    ElMessage.error('同步状态失败')
    console.error(error)
  }
}

const handleReplay = (row) => {
  replayForm.id = row.id
  replayForm.replayUrl = row.replayUrl || ''
  replayForm.replayStatus = row.replayStatus ?? 0
  showReplayModal.value = true
}

const handleSaveReplay = async () => {
  if (!replayForm.id) return
  try {
    await updateReplayApi(replayForm.id, {
      replayUrl: replayForm.replayUrl,
      replayStatus: replayForm.replayStatus
    })
    ElMessage.success('回放已保存')
    showReplayModal.value = false
    loadSessionList()
  } catch (error) {
    ElMessage.error('保存回放失败')
    console.error(error)
  }
}

const handleDeleteReplay = async () => {
  if (!replayForm.id) return
  try {
    await deleteReplayApi(replayForm.id)
    ElMessage.success('回放已删除')
    showReplayModal.value = false
    loadSessionList()
  } catch (error) {
    ElMessage.error('删除回放失败')
    console.error(error)
  }
}

const handleSubmit = async () => {
  if (!form.sessionName || !form.platformId || !form.startTime || !form.endTime) {
    ElMessage.warning('请填写必填项')
    return
  }
  try {
    if (isEdit.value) {
      await updateSessionApi(form)
      ElMessage.success('更新成功')
    } else {
      await addSessionApi(form)
      ElMessage.success('新增成功')
    }
    showAddModal.value = false
    loadSessionList()
  } catch (error) {
    ElMessage.error('保存直播场次失败')
    console.error(error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除直播场次“${row.sessionName}”吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteSessionApi(row.id)
      ElMessage.success('删除成功')
      loadSessionList()
    })
    .catch(() => {})
}

const handleStart = (row) => {
  ElMessageBox.confirm(`确定开始直播“${row.sessionName}”吗？`, '提示', { type: 'info' })
    .then(async () => {
      await startLiveApi(row.id)
      ElMessage.success('开播成功')
      loadSessionList()
      openNotifyDialog({ ...row, status: 1 })
    })
    .catch(() => {})
}

const handleStop = (row) => {
  ElMessageBox.confirm(`确定结束直播“${row.sessionName}”吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await stopLiveApi(row.id)
      ElMessage.success('关播成功')
      loadSessionList()
    })
    .catch(() => {})
}

const openNotifyDialog = (row) => {
  notifySession.id = row.id
  notifySession.sessionName = row.sessionName || ''
  notifyForm.customerIds = []
  notifyForm.templateCode = templateOptions.value.find(item => item.templateCode === 'LIVE_START')?.templateCode || templateOptions.value[0]?.templateCode || 'LIVE_START'
  notifyForm.sendSystemMessage = true
  notifyForm.sendSms = false
  showNotifyModal.value = true
  if (customerOptions.value.length === 0 || templateOptions.value.length === 0) {
    loadNotifyOptions()
  }
}

const submitNotify = async () => {
  if (!notifySession.id) return
  if (!notifyForm.customerIds.length) {
    ElMessage.warning('请选择接收客户')
    return
  }
  if (!notifyForm.sendSystemMessage && !notifyForm.sendSms) {
    ElMessage.warning('请至少选择一种通知方式')
    return
  }

  notifyLoading.value = true
  try {
    const response = await notifyLiveStartApi(notifySession.id, {
      customerIds: notifyForm.customerIds,
      templateCode: notifyForm.templateCode,
      sendSystemMessage: notifyForm.sendSystemMessage,
      sendSms: notifyForm.sendSms
    })
    const result = response.data?.data || {}
    ElMessage.success(`通知完成：站内信${result.systemMessageCount || 0}条，短信${result.smsCount || 0}条，跳过${result.skippedCount || 0}条`)
    showNotifyModal.value = false
  } catch (error) {
    ElMessage.error('发送开播通知失败')
    console.error(error)
  } finally {
    notifyLoading.value = false
  }
}
</script>

<style scoped>
.session-list {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}

.search-form {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.metric-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 20px;
}

.notify-tip {
  margin-bottom: 16px;
}
</style>
