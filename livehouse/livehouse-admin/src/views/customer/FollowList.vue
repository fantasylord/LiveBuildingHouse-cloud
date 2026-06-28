<template>
  <div class="follow-list">
    <div class="page-header">
      <h2>客户跟进记录</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加跟进
      </el-button>
    </div>

    <el-card>
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="客户姓名">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户姓名" clearable />
        </el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form>

      <el-table :data="followList" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="customerName" label="客户姓名" />
        <el-table-column prop="customerPhone" label="客户电话" />
        <el-table-column prop="followType" label="跟进方式" width="100">
          <template #default="scope">
            <el-tag>{{ getFollowTypeText(scope.row.followType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="followContent" label="跟进内容" />
        <el-table-column prop="nextFollowTime" label="下次跟进时间" width="180">
          <template #default="scope">{{ formatDateTime(scope.row.nextFollowTime) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">{{ formatDateTime(scope.row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="scope">
            <el-button size="small" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next, jumper"
        @current-change="loadFollows"
        style="margin-top: 20px; text-align: right;"
      />
    </el-card>

    <el-dialog :title="isEdit ? '编辑跟进' : '添加跟进'" v-model="showDialog" width="560px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="客户" required>
          <el-select v-model="form.customerId" filterable placeholder="请选择客户" style="width: 100%">
            <el-option
              v-for="customer in customerOptions"
              :key="customer.id"
              :label="getCustomerOptionLabel(customer)"
              :value="customer.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="跟进方式">
          <el-select v-model="form.followType" style="width: 100%">
            <el-option label="电话" :value="0" />
            <el-option label="微信" :value="1" />
            <el-option label="短信" :value="2" />
            <el-option label="面谈" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="跟进内容" required>
          <el-input v-model="form.followContent" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="意向等级">
          <el-select v-model="form.intentionLevel" style="width: 100%">
            <el-option label="普通" :value="1" />
            <el-option label="意向" :value="2" />
            <el-option label="高意向" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="下一步计划">
          <el-input v-model="form.nextPlan" type="textarea" rows="2" />
        </el-form-item>
        <el-form-item label="下次跟进">
          <el-date-picker v-model="form.nextFollowTime" type="datetime" style="width: 100%" />
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
import { Plus } from '@element-plus/icons-vue'
import { addFollowApi, deleteFollowApi, getFollowListApi, updateFollowApi } from '@/api/customer.js'
import { getCustomerListApi } from '@/api/customer.js'

const searchForm = reactive({ customerName: '' })
const followList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const customerOptions = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const form = reactive({ id: null, customerId: null, followType: 0, followContent: '', intentionLevel: 1, nextPlan: '', nextFollowTime: '' })

onMounted(() => {
  loadFollows()
  loadCustomerOptions()
})

const loadFollows = async () => {
  loading.value = true
  try {
    const response = await getFollowListApi(pageNum.value, pageSize.value, {
      customerName: searchForm.customerName || undefined
    })
    const data = response.data.data
    followList.value = data?.records || []
    total.value = data?.total || 0
  } catch (error) {
    ElMessage.error('获取跟进记录失败')
  } finally {
    loading.value = false
  }
}

const getFollowTypeText = (type) => ({ 0: '电话', 1: '微信', 2: '短信', 3: '面谈' }[type] || '其他')
const formatDateTime = (value) => value ? String(value).replace('T', ' ').substring(0, 19) : '-'
const getCustomerOptionLabel = (customer) => `${customer.customerName || '未命名客户'}（${customer.phone || '无手机号'}）`

const loadCustomerOptions = async () => {
  try {
    const response = await getCustomerListApi(1, 500, {})
    customerOptions.value = response.data.data?.records || []
  } catch (error) {
    console.error('加载客户列表失败:', error)
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadFollows()
}

const handleReset = () => {
  searchForm.customerName = ''
  handleSearch()
}

const resetForm = () => Object.assign(form, { id: null, customerId: null, followType: 0, followContent: '', intentionLevel: 1, nextPlan: '', nextFollowTime: '' })

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
  if (!form.customerId || !form.followContent) {
    ElMessage.warning('请填写客户ID和跟进内容')
    return
  }
  try {
    if (isEdit.value) {
      await updateFollowApi(form)
    } else {
      await addFollowApi(form)
    }
    ElMessage.success('保存成功')
    showDialog.value = false
    loadFollows()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该跟进记录吗？', '提示', { type: 'warning' })
    .then(async () => {
      await deleteFollowApi(row.id)
      ElMessage.success('删除成功')
      loadFollows()
    })
    .catch(() => {})
}
</script>

<style scoped>
.follow-list {
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
