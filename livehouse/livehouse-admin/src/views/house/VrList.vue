<template>
  <div class="vr-list">
    <div class="page-header">
      <h2>VR素材管理</h2>
      <div class="header-actions">
        <el-button @click="openCubemapTool('standalone')">六面图合成全景</el-button>
        <el-button type="primary" @click="handleAddClick">
          <el-icon name="plus" />
          添加VR素材
        </el-button>
      </div>
    </div>
    
    <el-card>
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="素材名称">
          <el-input v-model="searchForm.vrName" placeholder="请输入素材名称" />
        </el-form-item>
        <el-form-item label="所属楼盘">
          <el-select 
            v-model="searchForm.buildingName" 
            placeholder="请选择或搜索楼盘" 
            filterable
            remote
            clearable
            :remote-method="searchBuildings"
            :loading="buildingLoading"
            style="width: 200px"
          >
            <el-option
              v-for="building in buildingOptions"
              :key="building.id"
              :label="building.buildingName"
              :value="building.buildingName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form>
      
      <el-table :data="vrList" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="vrName" label="素材名称" min-width="150" />
        <el-table-column prop="buildingName" label="所属楼盘" min-width="150" />
        <el-table-column prop="unitName" label="所属户型" min-width="120" />
        <el-table-column label="缩略图" width="100">
          <template #default="scope">
            <div 
              v-if="scope.row.thumbnailUrl"
              class="table-image"
              @mouseenter="handleTableImageHover($event, getFullImageUrl(scope.row.thumbnailUrl))"
              @mouseleave="handleImageLeave"
            >
              <img :src="getFullImageUrl(scope.row.thumbnailUrl)" alt="" />
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
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
      :title="isEdit ? '编辑VR素材' : '添加VR素材'" 
      v-model="showAddModal"
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="所属楼盘" required>
          <el-select 
            v-model="form.buildingId" 
            placeholder="请选择楼盘" 
            filterable
            remote
            clearable
            :remote-method="searchBuildingsForForm"
            :loading="buildingLoading"
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
        <el-form-item label="所属户型">
          <el-select 
            v-model="form.unitId" 
            placeholder="请选择户型" 
            filterable
            clearable
            :loading="unitLoading"
            style="width: 100%"
            :disabled="!form.buildingId"
          >
            <el-option
              v-for="unit in unitOptions"
              :key="unit.id"
              :label="unit.unitName"
              :value="unit.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="素材名称" required>
          <el-input v-model="form.vrName" placeholder="请输入素材名称" />
        </el-form-item>
        <el-form-item label="素材编码">
          <el-input v-model="form.vrCode" placeholder="请输入素材编码" />
        </el-form-item>
        <el-form-item label="全景图" required>
          <el-upload
            class="image-uploader"
            :http-request="uploadPanoramaRequest"
            :show-file-list="false"
            :before-upload="beforePanoramaUpload"
            accept="image/*"
          >
            <img v-if="form.panoramaUrl" :src="getFullImageUrl(form.panoramaUrl)" class="uploaded-image" />
            <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">支持jpg/png格式，单张不超过50MB</div>
          <el-button size="small" style="margin-top: 8px" @click="openCubemapTool('form')">
            六面图合成全景
          </el-button>
        </el-form-item>
        <el-form-item label="缩略图">
          <el-upload
            class="image-uploader"
            :http-request="uploadThumbRequest"
            :show-file-list="false"
            :before-upload="beforeThumbUpload"
            accept="image/*"
          >
            <img v-if="form.thumbnailUrl" :src="getFullImageUrl(form.thumbnailUrl)" class="uploaded-image" />
            <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">支持jpg/png格式，单张不超过10MB</div>
        </el-form-item>
        <el-form-item label="场景配置">
          <VrSceneConfig
            v-model="form.scenes"
            :default-panorama-url="form.panoramaUrl"
            :default-thumbnail-url="form.thumbnailUrl"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      title="六面图合成VR全景"
      v-model="showCubemapTool"
      width="780px"
      destroy-on-close
    >
      <CubemapPanoramaTool @uploaded="handleCubemapUploaded" />
    </el-dialog>

    <!-- 图片预览浮层 -->
    <div 
      v-if="showImagePreview" 
      class="image-preview-popup"
      :style="{ left: previewPosition.x + 'px', top: previewPosition.y + 'px' }"
    >
      <img :src="previewImageUrl" alt="预览" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import VrSceneConfig from '@/components/VrSceneConfig.vue'
import CubemapPanoramaTool from '@/components/CubemapPanoramaTool.vue'
import {
  getVrListApi,
  addVrApi,
  updateVrApi,
  deleteVrApi,
  changeVrStatusApi,
  getBuildingListApi,
  getUnitListApi
} from '@/api/house.js'
import { uploadImageApi } from '@/api/upload.js'
import { initUploadConfig, getFullImageUrl } from '@/utils/imageUrl.js'

const searchForm = reactive({
  vrName: '',
  buildingName: '',
  status: null
})

const vrList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const showAddModal = ref(false)
const showCubemapTool = ref(false)
const cubemapSource = ref('form')
const isEdit = ref(false)
const loading = ref(false)

// 图片预览
const showImagePreview = ref(false)
const previewImageUrl = ref('')
const previewPosition = ref({ x: 0, y: 0 })

// 楼盘列表（用于搜索和表单）
const buildingOptions = ref([])
const buildingLoading = ref(false)
const buildingPageNum = ref(1)
const buildingPageSize = ref(50)

// 户型列表（用于表单）
const unitOptions = ref([])
const unitLoading = ref(false)

const form = reactive({
  id: null,
  buildingId: null,
  unitId: null,
  vrName: '',
  vrCode: '',
  panoramaUrl: '',
  thumbnailUrl: '',
  hotspots: '',
  scenes: '',
  status: 1,
  sortOrder: 0
})

// 初始化
onMounted(async () => {
  await initUploadConfig()
  loadVrList()
  loadInitialBuildings()
})

// 初始加载部分楼盘数据
const loadInitialBuildings = async () => {
  try {
    const response = await getBuildingListApi(1, buildingPageSize.value)
    
    if (response.data && response.data.records) {
      buildingOptions.value = response.data.records
    } else if (response.data && response.data.data && response.data.data.records) {
      buildingOptions.value = response.data.data.records
    }
  } catch (error) {
    console.error('获取楼盘列表失败', error)
  }
}

// 远程搜索楼盘
const searchBuildings = async (query) => {
  if (query !== '') {
    buildingLoading.value = true
    try {
      const response = await getBuildingListApi(1, buildingPageSize.value, { buildingName: query })
      
      if (response.data && response.data.records) {
        buildingOptions.value = response.data.records
      } else if (response.data && response.data.data && response.data.data.records) {
        buildingOptions.value = response.data.data.records
      }
    } catch (error) {
      console.error('搜索楼盘失败', error)
    } finally {
      buildingLoading.value = false
    }
  } else {
    loadInitialBuildings()
  }
}

// 表单中的楼盘搜索
const searchBuildingsForForm = searchBuildings

// 楼盘选择变化
const handleBuildingChange = async (buildingId) => {
  form.unitId = null
  unitOptions.value = []
  
  if (buildingId) {
    unitLoading.value = true
    try {
      const response = await getUnitListApi(1, 100, { buildingId })
      
      if (response.data && response.data.records) {
        unitOptions.value = response.data.records
      } else if (response.data && response.data.data && response.data.data.records) {
        unitOptions.value = response.data.data.records
      }
    } catch (error) {
      console.error('获取户型列表失败', error)
    } finally {
      unitLoading.value = false
    }
  }
}

// 获取VR列表
const loadVrList = async () => {
  loading.value = true
  try {
    const response = await getVrListApi(pageNum.value, pageSize.value, {
      buildingName: searchForm.buildingName || undefined,
      vrName: searchForm.vrName || undefined,
      status: searchForm.status || undefined
    })
    
    if (response.data && response.data.records) {
      vrList.value = response.data.records
      total.value = response.data.total
    } else if (response.data && response.data.data && response.data.data.records) {
      vrList.value = response.data.data.records
      total.value = response.data.data.total
    }
  } catch (error) {
    ElMessage.error('获取VR列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  loadVrList()
}

// 重置
const handleReset = () => {
  searchForm.vrName = ''
  searchForm.buildingName = ''
  searchForm.status = null
  pageNum.value = 1
  loadVrList()
}

// 添加
const handleAddClick = () => {
  isEdit.value = false
  resetForm()
  showAddModal.value = true
}

const openCubemapTool = (source = 'form') => {
  cubemapSource.value = source
  showCubemapTool.value = true
}

// 编辑
const handleEdit = async (row) => {
  isEdit.value = true
  resetForm()
  form.id = row.id
  form.buildingId = row.buildingId
  form.unitId = row.unitId
  form.vrName = row.vrName
  form.vrCode = row.vrCode || ''
  form.panoramaUrl = row.panoramaUrl
  form.thumbnailUrl = row.thumbnailUrl
  form.hotspots = row.hotspots || ''
  form.scenes = row.scenes || ''
  form.status = row.status
  form.sortOrder = row.sortOrder || 0

  // 如果有楼盘ID，加载户型列表
  if (row.buildingId) {
    await handleBuildingChange(row.buildingId)
  }

  showAddModal.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定删除VR素材 "${row.vrName}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteVrApi(row.id)
      ElMessage.success('删除成功')
      loadVrList()
    } catch (error) {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }).catch(() => {})
}

// 提交
const handleSubmit = async () => {
  if (!form.buildingId || !form.vrName || !form.panoramaUrl) {
    ElMessage.error('请填写必填项')
    return
  }

  try {
    if (isEdit.value) {
      await updateVrApi(form)
      ElMessage.success('更新成功')
    } else {
      await addVrApi(form)
      ElMessage.success('添加成功')
    }
    showAddModal.value = false
    loadVrList()
  } catch (error) {
    ElMessage.error('操作失败')
    console.error(error)
  }
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.buildingId = null
  form.unitId = null
  form.vrName = ''
  form.vrCode = ''
  form.panoramaUrl = ''
  form.thumbnailUrl = ''
  form.hotspots = ''
  form.scenes = ''
  form.status = 1
  form.sortOrder = 0
  unitOptions.value = []
}

// 全景图上传 - 自定义上传请求
const uploadPanoramaRequest = async (options) => {
  try {
    const response = await uploadImageApi(options.file, 'vr')
    if (response && response.data && response.data.code === 200 && response.data.data) {
      form.panoramaUrl = response.data.data.url
      ElMessage.success('全景图上传成功')
      options.onSuccess(response.data)
    } else {
      const errorMsg = response?.data?.message || '上传失败'
      ElMessage.error('全景图上传失败：' + errorMsg)
      options.onError(new Error(errorMsg))
    }
  } catch (error) {
    console.error('全景图上传错误', error)
    const message = error?.response?.data?.message || error.message || '文件上传失败'
    ElMessage.error('全景图上传失败：' + message)
    options.onError(error)
  }
}

const handleCubemapUploaded = (url) => {
  if (cubemapSource.value === 'standalone') {
    isEdit.value = false
    resetForm()
    showAddModal.value = true
  }
  form.panoramaUrl = url
  showCubemapTool.value = false
}

// 缩略图上传 - 自定义上传请求
const uploadThumbRequest = async (options) => {
  try {
    const response = await uploadImageApi(options.file, 'vr')
    if (response && response.data && response.data.code === 200 && response.data.data) {
      form.thumbnailUrl = response.data.data.url
      ElMessage.success('缩略图上传成功')
      options.onSuccess(response.data)
    } else {
      const errorMsg = response?.data?.message || '上传失败'
      ElMessage.error('缩略图上传失败：' + errorMsg)
      options.onError(new Error(errorMsg))
    }
  } catch (error) {
    console.error('缩略图上传错误', error)
    const message = error?.response?.data?.message || error.message || '文件上传失败'
    ElMessage.error('缩略图上传失败：' + message)
    options.onError(error)
  }
}

// 全景图上传前校验
const beforePanoramaUpload = (file) => {
  const maxSize = 50 * 1024 * 1024 // 50MB
  if (file.size > maxSize) {
    ElMessage.error('全景图大小不能超过50MB')
    return false
  }
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  return true
}

// 缩略图上传前校验
const beforeThumbUpload = (file) => {
  const maxSize = 10 * 1024 * 1024 // 10MB
  if (file.size > maxSize) {
    ElMessage.error('缩略图大小不能超过10MB')
    return false
  }
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  return true
}

// 分页变化
const handlePageChange = (newPage) => {
  pageNum.value = newPage
  loadVrList()
}

// 表格图片hover显示预览
const handleTableImageHover = (event, imageUrl) => {
  previewImageUrl.value = imageUrl
  showImagePreview.value = true
  
  const rect = event.currentTarget.getBoundingClientRect()
  previewPosition.value = {
    x: rect.right + 10,
    y: rect.top
  }
}

// 图片leave隐藏预览
const handleImageLeave = () => {
  showImagePreview.value = false
}
</script>

<style scoped>
.vr-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.page-header h2 {
  margin: 0;
}

.search-form {
  margin-bottom: 20px;
}

.table-image {
  width: 60px;
  height: 40px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
}

.table-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-preview-popup {
  position: fixed;
  z-index: 9999;
  max-width: 600px;
  max-height: 400px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 10px;
}

.image-preview-popup img {
  max-width: 100%;
  max-height: 100%;
  display: block;
}

.image-uploader {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-uploader:hover {
  border-color: #409eff;
}

.image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
