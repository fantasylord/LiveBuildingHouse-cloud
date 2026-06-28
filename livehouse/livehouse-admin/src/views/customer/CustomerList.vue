<template>
  <div class="customer-list">
    <div class="page-header">
      <h2>客户管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加客户
      </el-button>
    </div>

    <el-card>
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="客户姓名">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="来源渠道">
          <el-select v-model="searchForm.customerSource" placeholder="请选择渠道" clearable style="width: 150px">
            <el-option label="预约看房" value="1" />
            <el-option label="直播留资" value="2" />
            <el-option label="自然到访" value="3" />
            <el-option label="渠道推广" value="4" />
          </el-select>
        </el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form>

      <el-table :data="customerList" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="customerName" label="客户姓名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="customerSource" label="来源渠道" width="120">
          <template #default="scope">
            <el-tag>{{ getSourceText(scope.row.customerSource) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="customerLevel" label="客户等级" width="100" />
        <el-table-column prop="intentionStatus" label="意向状态" width="100">
          <template #default="scope">
            <el-tag :type="getIntentionStatusType(scope.row.intentionStatus)">{{ getIntentionStatusText(scope.row.intentionStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="budget" label="预算(万)" width="110" />
        <el-table-column prop="interestedUnit" label="意向户型" width="120" />
        <el-table-column label="置业顾问" width="140">
          <template #default="scope">{{ getConsultantName(scope.row.assignConsultantId) }}</template>
        </el-table-column>
        <el-table-column prop="followCount" label="跟进次数" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="210">
          <template #default="scope">
            <el-button size="small" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" link type="primary" @click="handleAssign(scope.row)">分配</el-button>
            <el-button size="small" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next, jumper"
        @current-change="loadCustomers"
        style="margin-top: 20px; text-align: right;"
      />
    </el-card>

    <el-dialog :title="isEdit ? '编辑客户' : '添加客户'" v-model="showDialog" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="客户姓名" required>
          <el-input v-model="form.customerName" placeholder="请输入客户姓名" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" clearable style="width: 100%">
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="form.age" :min="0" :max="120" style="width: 100%" />
        </el-form-item>
        <el-form-item label="微信">
          <el-input v-model="form.wechat" placeholder="请输入微信号" />
        </el-form-item>
        <el-form-item label="来源渠道">
          <el-select v-model="form.customerSource" style="width: 100%">
            <el-option label="预约看房" value="1" />
            <el-option label="直播留资" value="2" />
            <el-option label="自然到访" value="3" />
            <el-option label="渠道推广" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="预算">
          <el-input-number v-model="form.budget" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="意向户型">
          <el-input v-model="form.interestedUnit" placeholder="如：三室两厅 / 大平层" />
        </el-form-item>
        <el-form-item label="客户等级">
          <el-select v-model="form.customerLevel" style="width: 100%">
            <el-option label="普通" :value="1" />
            <el-option label="意向" :value="2" />
            <el-option label="VIP" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="意向状态">
          <el-select v-model="form.intentionStatus" style="width: 100%">
            <el-option label="无意向" :value="0" />
            <el-option label="了解中" :value="1" />
            <el-option label="有意向" :value="2" />
            <el-option label="高意向" :value="3" />
            <el-option label="已成交" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog title="分配置业顾问" v-model="showAssignDialog" width="420px">
      <el-form label-width="100px">
        <el-form-item label="客户">
          <span>{{ assignTarget.customerName || '-' }}</span>
        </el-form-item>
        <el-form-item label="置业顾问" required>
          <el-select
            v-model="assignForm.consultantId"
            filterable
            clearable
            placeholder="请输入顾问姓名/账号/手机号搜索"
            style="width: 100%"
          >
            <el-option
              v-for="consultant in consultantOptions"
              :key="consultant.id"
              :label="getConsultantOptionLabel(consultant)"
              :value="consultant.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAssignDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAssignSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { addCustomerApi, assignCustomerApi, deleteCustomerApi, getCustomerListApi, updateCustomerApi } from '@/api/customer.js'
import { getUserListApi } from '@/api/user.js'

const searchForm = reactive({ customerName: '', phone: '', customerSource: '' })
const customerList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const showDialog = ref(false)
const showAssignDialog = ref(false)
const isEdit = ref(false)
const consultantOptions = ref([])
const consultantMap = ref({})

const form = reactive({
  id: null,
  customerName: '',
  phone: '',
  gender: null,
  age: null,
  wechat: '',
  customerSource: '2',
  budget: 0,
  interestedUnit: '',
  customerLevel: 1,
  intentionStatus: 1,
  remark: ''
})

const assignTarget = reactive({})
const assignForm = reactive({ consultantId: null })

const loadCustomers = async () => {
  loading.value = true
  try {
    const response = await getCustomerListApi(pageNum.value, pageSize.value, {
      customerName: searchForm.customerName || undefined,
      phone: searchForm.phone || undefined,
      customerSource: searchForm.customerSource || undefined
    })
    const data = response.data.data
    customerList.value = data?.records || []
    total.value = data?.total || 0
  } catch (error) {
    ElMessage.error('获取客户列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCustomers()
  loadConsultantOptions()
})

const loadConsultantOptions = async () => {
  try {
    const response = await getUserListApi()
    const users = response.data.data || []
    consultantOptions.value = users.filter((user) => user.status !== 0)
    consultantMap.value = users.reduce((map, user) => {
      map[user.id] = user
      return map
    }, {})
  } catch (error) {
    console.error('加载顾问列表失败:', error)
  }
}

const getSourceText = (source) => {
  return { 1: '预约看房', 2: '直播留资', 3: '自然到访', 4: '渠道推广' }[source] || '未知'
}

const getIntentionStatusText = (status) => {
  return { 0: '无意向', 1: '了解中', 2: '有意向', 3: '高意向', 4: '已成交' }[status] || '未知'
}

const getIntentionStatusType = (status) => {
  return { 0: 'danger', 1: 'info', 2: 'warning', 3: 'success', 4: '' }[status] || 'info'
}

const formatDateTime = (value) => value ? String(value).replace('T', ' ').substring(0, 19) : '-'
const getConsultantName = (consultantId) => {
  const consultant = consultantMap.value[consultantId]
  if (!consultantId) return '-'
  return consultant?.realName || consultant?.username || `顾问#${consultantId}`
}
const getConsultantOptionLabel = (consultant) => {
  const name = consultant.realName || consultant.username || `顾问#${consultant.id}`
  const phone = consultant.phone ? ` / ${consultant.phone}` : ''
  return `${name}${phone}`
}

const handleSearch = () => {
  pageNum.value = 1
  loadCustomers()
}

const handleReset = () => {
  searchForm.customerName = ''
  searchForm.phone = ''
  searchForm.customerSource = ''
  handleSearch()
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    customerName: '',
    phone: '',
    gender: null,
    age: null,
    wechat: '',
    customerSource: '3',
    budget: 0,
    interestedUnit: '',
    customerLevel: 1,
    intentionStatus: 1,
    remark: ''
  })
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  showDialog.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  showDialog.value = true
}

const handleSubmit = async () => {
  if (!form.customerName || !form.phone) {
    ElMessage.warning('请填写客户姓名和手机号')
    return
  }
  try {
    if (isEdit.value) {
      await updateCustomerApi(form)
    } else {
      await addCustomerApi(form)
    }
    ElMessage.success('保存成功')
    showDialog.value = false
    loadCustomers()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除客户 "${row.customerName}" 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteCustomerApi(row.id)
      ElMessage.success('删除成功')
      loadCustomers()
    })
    .catch(() => {})
}

const handleAssign = (row) => {
  Object.assign(assignTarget, row)
  assignForm.consultantId = row.assignConsultantId || null
  if (consultantOptions.value.length === 0) {
    loadConsultantOptions()
  }
  showAssignDialog.value = true
}

const handleAssignSubmit = async () => {
  if (!assignTarget.id || !assignForm.consultantId) {
    ElMessage.warning('请选择置业顾问')
    return
  }
  try {
    await assignCustomerApi(assignTarget.id, assignForm.consultantId)
    ElMessage.success('分配成功')
    showAssignDialog.value = false
    loadCustomers()
  } catch (error) {
    ElMessage.error('分配失败')
  }
}
</script>

<style scoped>
.customer-list {
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
