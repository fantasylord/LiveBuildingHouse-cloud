<template>
  <div class="platform-list">
    <div class="page-header">
      <h2>直播平台配置</h2>
      <el-button type="primary" @click="handleAddClick">
        <el-icon><Plus /></el-icon>
        添加平台
      </el-button>
    </div>
    
    <el-card>
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="平台名称">
          <el-input v-model="searchForm.platformName" placeholder="请输入平台名称" clearable />
        </el-form-item>
        <el-form-item label="平台类型">
          <el-select v-model="searchForm.platformType" placeholder="请选择类型" clearable>
            <el-option label="腾讯云直播" value="tencent" />
            <el-option label="阿里云直播" value="aliyun" />
            <el-option label="抖音直播" value="douyin" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form>
      
      <el-table :data="platformList" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="platformName" label="平台名称" width="120" />
        <el-table-column prop="platformCode" label="平台编码" width="120" />
        <el-table-column prop="platformType" label="平台类型" width="120">
          <template #default="scope">
            <el-tag :type="getPlatformTagType(scope.row.platformType)">
              {{ getPlatformTypeText(scope.row.platformType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pushDomain" label="推流域名" width="180" />
        <el-table-column prop="playDomain" label="播放域名" width="180" />
        <el-table-column prop="expireTime" label="地址有效期" width="120">
          <template #default="scope">
            {{ (scope.row.expireTime || 3600) / 3600 }}小时
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="150" />
        <el-table-column label="操作" width="200" fixed="right">
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
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        style="margin-top: 20px; text-align: right;"
      />
    </el-card>
    
    <el-dialog 
      :title="isEdit ? '编辑直播平台' : '添加直播平台'" 
      v-model="showAddModal"
      width="600px"
    >
      <el-form :model="form" label-width="120px">
        <el-form-item label="平台名称" required>
          <el-input v-model="form.platformName" placeholder="请输入平台名称" />
        </el-form-item>
        <el-form-item label="平台编码" required>
          <el-input v-model="form.platformCode" placeholder="如：TENCENT" />
        </el-form-item>
        <el-form-item label="平台类型" required>
          <el-select v-model="form.platformType" placeholder="请选择平台类型" style="width: 100%">
            <el-option label="腾讯云直播" value="tencent" />
            <el-option label="阿里云直播" value="aliyun" />
            <el-option label="抖音直播" value="douyin" />
          </el-select>
        </el-form-item>
        <el-form-item label="应用ID">
          <el-input v-model="form.appId" placeholder="请输入应用ID" />
        </el-form-item>
        <el-form-item label="应用Key">
          <el-input v-model="form.appKey" placeholder="请输入应用Key" />
        </el-form-item>
        <el-form-item label="应用Secret">
          <el-input v-model="form.appSecret" placeholder="请输入应用Secret" />
        </el-form-item>
        <el-form-item label="推流域名">
          <el-input v-model="form.pushDomain" placeholder="请输入推流域名" />
        </el-form-item>
        <el-form-item label="播放域名">
          <el-input v-model="form.playDomain" placeholder="请输入播放域名" />
        </el-form-item>
        <el-form-item label="地址有效期（小时）">
          <el-input-number v-model="form.expireTime" :min="1" :max="72" style="width: 100%" />
        </el-form-item>
        <el-form-item label="平台描述">
          <el-input 
            v-model="form.description" 
            placeholder="请输入平台描述" 
            type="textarea" 
            rows="2" 
          />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getPlatformListApi,
  addPlatformApi,
  updatePlatformApi,
  deletePlatformApi
} from '@/api/live.js'

const searchForm = reactive({
  platformName: '',
  platformType: '',
  status: null
})

const platformList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const showAddModal = ref(false)
const isEdit = ref(false)
const loading = ref(false)

const form = reactive({
  id: null,
  platformName: '',
  platformCode: '',
  platformType: 'tencent',
  appId: '',
  appKey: '',
  appSecret: '',
  pushDomain: '',
  playDomain: '',
  expireTime: 24,
  description: '',
  remark: ''
})

onMounted(() => {
  loadPlatformList()
})

const loadPlatformList = async () => {
  loading.value = true
  try {
    const response = await getPlatformListApi(pageNum.value, pageSize.value, {
      platformName: searchForm.platformName || undefined,
      platformType: searchForm.platformType || undefined,
      status: searchForm.status || undefined
    })
    
    if (response.data && response.data.code === 200 && response.data.data) {
      const pageData = response.data.data
      if (pageData.records) {
        platformList.value = pageData.records
        total.value = pageData.total || 0
      }
    }
  } catch (error) {
    ElMessage.error('获取直播平台列表失败')
    console.error('加载列表错误:', error)
  } finally {
    loading.value = false
  }
}

const getPlatformTagType = (type) => {
  const types = { tencent: 'success', aliyun: 'primary', douyin: 'warning' }
  return types[type] || 'info'
}

const getPlatformTypeText = (type) => {
  const texts = { tencent: '腾讯云', aliyun: '阿里云', douyin: '抖音' }
  return texts[type] || type
}

const handleSearch = () => {
  pageNum.value = 1
  loadPlatformList()
}

const handleReset = () => {
  searchForm.platformName = ''
  searchForm.platformType = ''
  searchForm.status = null
  pageNum.value = 1
  loadPlatformList()
}

const handlePageChange = () => {
  loadPlatformList()
}

const handleAddClick = () => {
  isEdit.value = false
  resetForm()
  showAddModal.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  resetForm()
  form.id = row.id
  form.platformName = row.platformName
  form.platformCode = row.platformCode
  form.platformType = row.platformType
  form.appId = row.appId || ''
  form.appKey = row.appKey || ''
  form.appSecret = row.appSecret || ''
  form.pushDomain = row.pushDomain || ''
  form.playDomain = row.playDomain || ''
  form.expireTime = (row.expireTime || 3600) / 3600
  form.description = row.description || ''
  form.remark = row.remark || ''
  showAddModal.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定删除直播平台 "${row.platformName}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deletePlatformApi(row.id)
      ElMessage.success('删除成功')
      loadPlatformList()
    } catch (error) {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }).catch(() => {
  })
}

const resetForm = () => {
  form.id = null
  form.platformName = ''
  form.platformCode = ''
  form.platformType = 'tencent'
  form.appId = ''
  form.appKey = ''
  form.appSecret = ''
  form.pushDomain = ''
  form.playDomain = ''
  form.expireTime = 24
  form.description = ''
  form.remark = ''
}

const handleSubmit = async () => {
  if (!form.platformName) {
    ElMessage.warning('请输入平台名称')
    return
  }
  if (!form.platformCode) {
    ElMessage.warning('请输入平台编码')
    return
  }
  
  form.expireTime = (form.expireTime || 24) * 3600
  
  try {
    if (isEdit.value) {
      await updatePlatformApi(form)
      ElMessage.success('更新成功')
    } else {
      await addPlatformApi(form)
      ElMessage.success('添加成功')
    }
    showAddModal.value = false
    loadPlatformList()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
    console.error(error)
  }
}
</script>