<template>
  <div class="reserve-list">
    <div class="page-header">
      <h2>看房预约管理</h2>
      <el-button type="primary" @click="handleAdd">新增预约</el-button>
    </div>

    <el-card>
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="客户姓名">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="楼盘">
          <el-select v-model="searchForm.buildingId" placeholder="请选择楼盘" clearable filterable style="width: 180px">
            <el-option
              v-for="building in buildingOptions"
              :key="building.id"
              :label="building.buildingName"
              :value="building.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.visitStatus" placeholder="请选择状态" clearable style="width: 130px">
            <el-option label="待确认" :value="0" />
            <el-option label="已确认" :value="1" />
            <el-option label="已到访" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form>

      <el-table :data="reserveList" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="customerName" label="客户姓名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column label="预约楼盘" min-width="150">
          <template #default="scope">{{ getBuildingName(scope.row.buildingId) }}</template>
        </el-table-column>
        <el-table-column label="预约户型" min-width="130">
          <template #default="scope">{{ getUnitName(scope.row.unitId) }}</template>
        </el-table-column>
        <el-table-column label="直播场次" min-width="170">
          <template #default="scope">{{ getLiveSessionText(scope.row.liveSessionId) }}</template>
        </el-table-column>
        <el-table-column prop="visitTime" label="预约到访时间" width="180">
          <template #default="scope">{{ formatDateTime(scope.row.visitTime) }}</template>
        </el-table-column>
        <el-table-column prop="visitStatus" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.visitStatus)">
              {{ getStatusText(scope.row.visitStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180">
          <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="210">
          <template #default="scope">
            <el-button size="small" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" link type="primary" v-if="scope.row.visitStatus === 0" @click="handleStatus(scope.row, 1)">确认</el-button>
            <el-button size="small" link type="success" v-if="scope.row.visitStatus === 1" @click="handleStatus(scope.row, 2)">完成</el-button>
            <el-button size="small" link type="warning" @click="handleStatus(scope.row, 3)">取消</el-button>
            <el-button size="small" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next, jumper"
        @current-change="loadReserves"
        style="margin-top: 20px; text-align: right;"
      />
    </el-card>

    <el-dialog :title="isEdit ? '编辑预约' : '新增预约'" v-model="showDialog" width="560px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="客户" required>
          <el-select v-model="form.customerId" filterable placeholder="请选择客户" style="width: 100%" @change="handleCustomerChange">
            <el-option
              v-for="customer in customerOptions"
              :key="customer.id"
              :label="getCustomerOptionLabel(customer)"
              :value="customer.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="客户姓名" required>
          <el-input v-model="form.customerName" disabled />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="form.phone" disabled />
        </el-form-item>
        <el-form-item label="预约楼盘" required>
          <el-select
            v-model="form.buildingId"
            filterable
            placeholder="请选择楼盘"
            style="width: 100%"
            @change="handleBuildingChange"
          >
            <el-option
              v-for="building in buildingOptions"
              :key="building.id"
              :label="building.buildingName"
              :value="building.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预约户型">
          <el-select
            v-model="form.unitId"
            filterable
            clearable
            placeholder="请先选择楼盘"
            style="width: 100%"
            :disabled="!form.buildingId"
            :loading="unitLoading"
          >
            <el-option
              v-for="unit in unitOptions"
              :key="unit.id"
              :label="getUnitOptionLabel(unit)"
              :value="unit.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="到访时间">
          <el-date-picker v-model="form.visitTime" type="datetime" style="width: 100%" />
        </el-form-item>
        <el-form-item label="看房方式">
          <el-select v-model="form.visitType" style="width: 100%">
            <el-option label="实地到访" :value="0" />
            <el-option label="在线VR" :value="1" />
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addReserveApi, changeReserveStatusApi, deleteReserveApi, getCustomerListApi, getReserveListApi, updateReserveApi } from '@/api/customer.js'
import { getBuildingListApi, getUnitListApi } from '@/api/house.js'
import { getSessionListApi } from '@/api/live.js'

const searchForm = reactive({ customerName: '', phone: '', buildingId: null, visitStatus: null })
const reserveList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const customerOptions = ref([])
const buildingOptions = ref([])
const unitOptions = ref([])
const buildingMap = ref({})
const unitMap = ref({})
const liveSessionMap = ref({})
const unitLoading = ref(false)
const form = reactive({
  id: null,
  customerId: null,
  customerName: '',
  phone: '',
  buildingId: 0,
  unitId: null,
  visitTime: '',
  visitType: 0,
  visitStatus: 0,
  remark: ''
})

onMounted(() => {
  loadReserves()
  loadCustomerOptions()
  loadBuildingOptions()
  loadAllUnitOptions()
  loadLiveSessionOptions()
})

const loadReserves = async () => {
  loading.value = true
  try {
    const response = await getReserveListApi(pageNum.value, pageSize.value, {
      customerName: searchForm.customerName || undefined,
      phone: searchForm.phone || undefined,
      buildingId: searchForm.buildingId || undefined,
      visitStatus: searchForm.visitStatus ?? undefined
    })
    const data = response.data.data
    reserveList.value = data?.records || []
    total.value = data?.total || 0
  } catch (error) {
    ElMessage.error('获取预约列表失败')
  } finally {
    loading.value = false
  }
}

const formatDateTime = (value) => value ? String(value).replace('T', ' ').substring(0, 19) : '-'
const getStatusTagType = (status) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger' }[status] || 'info')
const getStatusText = (status) => ({ 0: '待确认', 1: '已确认', 2: '已到访', 3: '已取消' }[status] || '未知')
const getCustomerOptionLabel = (customer) => `${customer.customerName || '未命名客户'}（${customer.phone || '无手机号'}）`
const getUnitOptionLabel = (unit) => {
  const area = unit.area ? ` / ${unit.area}㎡` : ''
  return `${unit.unitName || '未命名户型'}${area}`
}
const getBuildingName = (buildingId) => buildingMap.value[buildingId] || (buildingId ? `楼盘#${buildingId}` : '-')
const getUnitName = (unitId) => unitMap.value[unitId] || (unitId ? `户型#${unitId}` : '-')
const getLiveSessionText = (liveSessionId) => {
  if (!liveSessionId) return '-'
  const session = liveSessionMap.value[liveSessionId]
  const name = session?.sessionName || session?.title || ''
  return name ? `${liveSessionId}: ${name}` : `${liveSessionId}: -`
}

const loadCustomerOptions = async () => {
  try {
    const response = await getCustomerListApi(1, 500, {})
    customerOptions.value = response.data.data?.records || []
  } catch (error) {
    console.error('加载客户列表失败:', error)
  }
}

const loadBuildingOptions = async () => {
  try {
    const response = await getBuildingListApi(1, 1000, { status: 1 })
    const records = response.data.data?.records || []
    buildingOptions.value = records
    buildingMap.value = records.reduce((map, building) => {
      map[building.id] = building.buildingName
      return map
    }, {})
  } catch (error) {
    console.error('加载楼盘列表失败:', error)
  }
}

const loadAllUnitOptions = async () => {
  try {
    const response = await getUnitListApi(1, 1000, {})
    const records = response.data.data?.records || []
    unitMap.value = records.reduce((map, unit) => {
      map[unit.id] = unit.unitName
      return map
    }, {})
  } catch (error) {
    console.error('加载户型列表失败:', error)
  }
}

const loadLiveSessionOptions = async () => {
  try {
    const response = await getSessionListApi(1, 1000, {})
    const records = response.data.data?.records || []
    liveSessionMap.value = records.reduce((map, session) => {
      map[session.id] = session
      return map
    }, {})
  } catch (error) {
    console.error('加载直播场次列表失败:', error)
  }
}

const loadUnitOptions = async (buildingId) => {
  if (!buildingId) {
    unitOptions.value = []
    return
  }
  unitLoading.value = true
  try {
    const response = await getUnitListApi(1, 500, { buildingId, status: 1 })
    unitOptions.value = response.data.data?.records || []
  } catch (error) {
    ElMessage.error('加载户型列表失败')
  } finally {
    unitLoading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadReserves()
}

const handleReset = () => {
  searchForm.customerName = ''
  searchForm.phone = ''
  searchForm.buildingId = null
  searchForm.visitStatus = null
  handleSearch()
}

const handleStatus = async (row, status) => {
  try {
    await changeReserveStatusApi(row.id, status)
    ElMessage.success('状态已更新')
    loadReserves()
  } catch (error) {
    ElMessage.error('更新状态失败')
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    customerId: null,
    customerName: '',
    phone: '',
    buildingId: null,
    unitId: null,
    visitTime: '',
    visitType: 0,
    visitStatus: 0,
    remark: ''
  })
}

const handleCustomerChange = (customerId) => {
  const customer = customerOptions.value.find(item => item.id === customerId)
  form.customerName = customer?.customerName || ''
  form.phone = customer?.phone || ''
}

const handleBuildingChange = (buildingId) => {
  form.unitId = null
  loadUnitOptions(buildingId)
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  unitOptions.value = []
  showDialog.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  Object.assign(form, row)
  const customer = customerOptions.value.find(item => item.phone === row.phone)
  form.customerId = customer?.id || null
  await loadUnitOptions(row.buildingId)
  showDialog.value = true
}

const handleSubmit = async () => {
  if (!form.customerName || !form.phone || !form.buildingId) {
    ElMessage.warning('请选择客户和预约楼盘')
    return
  }
  try {
    if (isEdit.value) {
      await updateReserveApi(form)
    } else {
      await addReserveApi(form)
    }
    ElMessage.success('保存成功')
    showDialog.value = false
    loadReserves()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除 ${row.customerName} 的预约吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteReserveApi(row.id)
      ElMessage.success('删除成功')
      loadReserves()
    })
    .catch(() => {})
}
</script>

<style scoped>
.reserve-list {
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
