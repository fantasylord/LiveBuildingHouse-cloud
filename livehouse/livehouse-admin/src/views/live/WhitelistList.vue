<template>
  <div class="whitelist-list">
    <div class="page-header">
      <h2>直播白名单管理</h2>
      <div class="header-actions">
        <el-button @click="handleBatchClick">批量导入</el-button>
        <el-button type="primary" @click="handleAddClick">
          <el-icon><Plus /></el-icon>
          添加白名单
        </el-button>
      </div>
    </div>
    
    <el-card>
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="关联场次">
          <el-select v-model="searchForm.sessionId" placeholder="请选择直播场次" clearable filterable style="width: 220px">
            <el-option
              v-for="session in sessionOptions"
              :key="session.id"
              :label="getSessionOptionLabel(session)"
              :value="session.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form>
      
      <el-table :data="whitelistList" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="sessionId" label="直播场次" min-width="180">
          <template #default="scope">
            {{ getSessionName(scope.row.sessionId) }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="customerName" label="客户姓名" width="120" />
        <el-table-column prop="createTime" label="添加时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button size="small" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination 
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        style="margin-top: 20px; text-align: right;"
      />
    </el-card>
    
    <el-dialog 
      title="添加白名单" 
      v-model="showAddModal"
      width="500px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="场次ID" required>
          <el-select v-model="form.sessionId" placeholder="请选择直播场次" filterable style="width: 100%">
            <el-option
              v-for="session in sessionOptions"
              :key="session.id"
              :label="getSessionOptionLabel(session)"
              :value="session.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="客户" required>
          <el-select
            v-model="form.customerId"
            placeholder="请选择客户"
            filterable
            style="width: 100%"
            @change="handleCustomerChange"
          >
            <el-option
              v-for="customer in customerOptions"
              :key="customer.id"
              :label="getCustomerOptionLabel(customer)"
              :value="customer.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="form.phone" placeholder="从客户列表带出手机号" disabled />
        </el-form-item>
        <el-form-item label="客户姓名">
          <el-input v-model="form.customerName" placeholder="从客户列表带出姓名" disabled />
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="form.remark" 
            placeholder="请输入备注" 
            type="textarea" 
            rows="2" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog title="批量导入白名单" v-model="showBatchModal" width="620px">
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="直播场次" required>
          <el-select v-model="batchForm.sessionId" placeholder="请选择直播场次" filterable style="width: 100%">
            <el-option
              v-for="session in sessionOptions"
              :key="session.id"
              :label="getSessionOptionLabel(session)"
              :value="session.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择客户" required>
          <el-select
            v-model="batchForm.customerIds"
            multiple
            filterable
            collapse-tags
            collapse-tags-tooltip
            placeholder="请选择要加入白名单的客户"
            style="width: 100%"
          >
            <el-option
              v-for="customer in customerOptions"
              :key="customer.id"
              :label="getCustomerOptionLabel(customer)"
              :value="customer.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="batchForm.remark" type="textarea" rows="2" placeholder="批量加入白名单备注" />
        </el-form-item>
      </el-form>
      <el-alert
        v-if="batchResult"
        class="batch-result"
        type="success"
        :closable="false"
        show-icon
      >
        <template #title>
          共 {{ batchResult.total }} 条，成功 {{ batchResult.success }} 条，重复 {{ batchResult.duplicate }} 条，无效 {{ batchResult.invalid }} 条
        </template>
      </el-alert>
      <div v-if="batchResult && batchResult.errors && batchResult.errors.length" class="batch-errors">
        <div v-for="error in batchResult.errors" :key="error">{{ error }}</div>
      </div>
      <template #footer>
        <el-button @click="showBatchModal = false">关闭</el-button>
        <el-button type="primary" @click="handleBatchSubmit">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getWhitelistListApi,
  addWhitelistApi,
  deleteWhitelistApi,
  batchAddWhitelistCustomersApi,
  getSessionListApi
} from '@/api/live.js'
import { getCustomerListApi } from '@/api/customer.js'

const searchForm = reactive({
  sessionId: '',
  phone: ''
})

const whitelistList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const showAddModal = ref(false)
const showBatchModal = ref(false)
const loading = ref(false)
const sessionOptions = ref([])
const customerOptions = ref([])
const batchResult = ref(null)

const form = reactive({
  sessionId: null,
  customerId: null,
  phone: '',
  customerName: '',
  remark: ''
})

const batchForm = reactive({
  sessionId: null,
  customerIds: [],
  remark: ''
})

// 初始化获取数据
onMounted(() => {
  loadWhitelistList()
  loadSessionOptions()
  loadCustomerOptions()
})

const loadSessionOptions = async () => {
  try {
    const response = await getSessionListApi(1, 200, {})
    if (response.data && response.data.code === 200) {
      sessionOptions.value = response.data.data?.records || []
    }
  } catch (error) {
    console.error('加载直播场次失败:', error)
  }
}

const loadCustomerOptions = async () => {
  try {
    const response = await getCustomerListApi(1, 500, {})
    if (response.data && response.data.code === 200) {
      customerOptions.value = response.data.data?.records || []
    }
  } catch (error) {
    console.error('加载客户列表失败:', error)
  }
}

// 获取白名单列表
const loadWhitelistList = async () => {
  loading.value = true
  try {
    const response = await getWhitelistListApi(pageNum.value, pageSize.value, {
      sessionId: searchForm.sessionId || undefined,
      phone: searchForm.phone || undefined
    })
    
    // axios拦截器返回完整response，后端返回Result包装，实际数据在response.data.data
    if (response.data && response.data.code === 200 && response.data.data) {
      const pageData = response.data.data
      if (pageData.records) {
        whitelistList.value = pageData.records
        total.value = pageData.total || 0
      }
    }
  } catch (error) {
    ElMessage.error('获取白名单列表失败')
    console.error('加载列表错误:', error)
  } finally {
    loading.value = false
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 19)
}

const getStatusText = (status) => {
  return { 0: '未开始', 1: '直播中', 2: '已结束', 3: '已关闭' }[status] || '未知'
}

const getSessionOptionLabel = (session) => {
  return `${session.sessionName || '未命名场次'}（${getStatusText(session.status)}）`
}

const getSessionName = (sessionId) => {
  const session = sessionOptions.value.find(item => item.id === sessionId)
  return session ? session.sessionName : `场次 ${sessionId}`
}

const getCustomerOptionLabel = (customer) => {
  return `${customer.customerName || '未命名客户'}（${customer.phone || '无手机号'}）`
}

const handleCustomerChange = (customerId) => {
  const customer = customerOptions.value.find(item => item.id === customerId)
  form.phone = customer?.phone || ''
  form.customerName = customer?.customerName || ''
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  loadWhitelistList()
}

// 重置
const handleReset = () => {
  searchForm.sessionId = ''
  searchForm.phone = ''
  pageNum.value = 1
  loadWhitelistList()
}

// 添加
const handleAddClick = () => {
  resetForm()
  showAddModal.value = true
}

const handleBatchClick = () => {
  batchForm.sessionId = searchForm.sessionId || null
  batchForm.customerIds = []
  batchForm.remark = ''
  batchResult.value = null
  showBatchModal.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定删除手机号 "${row.phone}" 的白名单吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteWhitelistApi(row.id)
      ElMessage.success('删除成功')
      loadWhitelistList()
    } catch (error) {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }).catch(() => {
    // 用户取消删除
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!form.sessionId || !form.phone) {
    ElMessage.error('请填写必填项')
    return
  }

  // 验证手机号格式
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(form.phone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }

  try {
    await addWhitelistApi(form)
    ElMessage.success('添加成功')
    showAddModal.value = false
    loadWhitelistList()
  } catch (error) {
    ElMessage.error('添加失败')
    console.error(error)
  }
}

const handleBatchSubmit = async () => {
  if (!batchForm.sessionId || batchForm.customerIds.length === 0) {
    ElMessage.warning('请选择直播场次和客户')
    return
  }
  try {
    const response = await batchAddWhitelistCustomersApi({
      sessionId: batchForm.sessionId,
      customerIds: batchForm.customerIds,
      remark: batchForm.remark
    })
    batchResult.value = response.data.data
    ElMessage.success(`导入完成，成功 ${batchResult.value?.success || 0} 条`)
    loadWhitelistList()
  } catch (error) {
    ElMessage.error('批量导入失败')
    console.error(error)
  }
}

// 重置表单
const resetForm = () => {
  form.sessionId = null
  form.customerId = null
  form.phone = ''
  form.customerName = ''
  form.remark = ''
}

// 分页变化
const handlePageChange = (newPage) => {
  pageNum.value = newPage
  loadWhitelistList()
}
</script>

<style scoped>
.whitelist-list {
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

.batch-result {
  margin-top: 8px;
}

.batch-errors {
  margin-top: 10px;
  max-height: 140px;
  overflow: auto;
  color: #f56c6c;
  font-size: 13px;
  line-height: 1.6;
}
</style>
