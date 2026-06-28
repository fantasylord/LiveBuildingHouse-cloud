<template>
  <div class="unit-list">
    <div class="page-header">
      <h2>户型管理</h2>
      <el-button type="primary" @click="handleAddClick">
        <el-icon name="plus" />
        添加户型
      </el-button>
    </div>
    
    <el-card>
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="户型名称">
          <el-input v-model="searchForm.unitName" placeholder="请输入户型名称" />
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
            <el-option label="可售" :value="1" />
            <el-option label="已售" :value="0" />
          </el-select>
        </el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form>
      
      <el-table :data="unitList" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="户型图" width="100">
          <template #default="scope">
            <div 
              v-if="scope.row.images && getFirstImage(scope.row.images)"
              class="table-image"
              @mouseenter="handleTableImageHover($event, getFullImageUrl(getFirstImage(scope.row.images)))"
              @mouseleave="handleImageLeave"
            >
              <img :src="getFullImageUrl(getFirstImage(scope.row.images))" alt="" />
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="unitName" label="户型名称" min-width="120" />
        <el-table-column prop="buildingName" label="所属楼盘" min-width="150" />
        <el-table-column prop="area" label="面积(㎡)" width="100" />
        <el-table-column label="户型" width="100">
          <template #default="scope">
            {{ scope.row.rooms }}室{{ scope.row.halls }}厅
          </template>
        </el-table-column>
        <el-table-column prop="price" label="单价(元/㎡)" width="120">
          <template #default="scope">
            {{ scope.row.price ? scope.row.price.toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
              {{ scope.row.status === 1 ? '可售' : '已售' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" link @click="handleViewDetail(scope.row)">详情</el-button>
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
      :title="isEdit ? '编辑户型' : '添加户型'" 
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
          >
            <el-option
              v-for="building in buildingOptions"
              :key="building.id"
              :label="building.buildingName"
              :value="building.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="户型名称" required>
          <el-input v-model="form.unitName" placeholder="请输入户型名称" />
        </el-form-item>
        <el-form-item label="户型编码">
          <el-input v-model="form.unitCode" placeholder="请输入户型编码" />
        </el-form-item>
        <el-form-item label="面积(㎡)" required>
          <el-input-number v-model="form.area" :min="0" />
        </el-form-item>
        <el-form-item label="室数">
          <el-input-number v-model="form.rooms" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="厅数">
          <el-input-number v-model="form.halls" :min="1" :max="5" />
        </el-form-item>
        <el-form-item label="卫数">
          <el-input-number v-model="form.bathrooms" :min="1" :max="5" />
        </el-form-item>
        <el-form-item label="单价(元/㎡)">
          <el-input-number v-model="form.price" :min="0" />
        </el-form-item>
        <el-form-item label="朝向">
          <el-input v-model="form.orientation" placeholder="请输入朝向" />
        </el-form-item>
        <el-form-item label="楼层范围">
          <el-input v-model="form.floorRange" placeholder="请输入楼层范围" />
        </el-form-item>
        <el-form-item label="总楼层">
          <el-input-number v-model="form.totalFloor" :min="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="可售" :value="1" />
            <el-option label="已售" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="请输入描述" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="户型图">
          <el-upload
            class="images-uploader"
            :http-request="uploadImageRequest"
            list-type="picture-card"
            :show-file-list="true"
            :file-list="imageFileList"
            accept="image/*"
            multiple
            :on-remove="handleImageRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多上传9张图片，支持jpg/png格式，单张不超过5MB</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 详情对话框 -->
    <el-dialog 
      title="户型详情" 
      v-model="showDetailModal"
      width="800px"
    >
      <div class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="户型名称">{{ detailData.unitName }}</el-descriptions-item>
          <el-descriptions-item label="户型编码">{{ detailData.unitCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所属楼盘">{{ detailData.buildingName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="面积">{{ detailData.area ? detailData.area + '㎡' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="户型结构">{{ detailData.rooms }}室{{ detailData.halls }}厅{{ detailData.bathrooms }}卫</el-descriptions-item>
          <el-descriptions-item label="单价">{{ detailData.price ? detailData.price.toLocaleString() + ' 元/㎡' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="朝向">{{ detailData.orientation || '-' }}</el-descriptions-item>
          <el-descriptions-item label="楼层范围">{{ detailData.floorRange || '-' }}</el-descriptions-item>
          <el-descriptions-item label="总楼层">{{ detailData.totalFloor || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detailData.status === 1 ? 'success' : 'warning'">
              {{ detailData.status === 1 ? '可售' : '已售' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="户型描述" :span="2">
            <div class="description-text">{{ detailData.description || '-' }}</div>
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 户型图片 -->
        <div v-if="detailImages.length > 0" class="detail-section">
          <h4>户型图</h4>
          <div class="images-preview">
            <div 
              v-for="(img, index) in detailImages" 
              :key="index" 
              class="image-item"
              @mouseenter="handleImageHover($event, getFullImageUrl(img))"
              @mouseleave="handleImageLeave"
            >
              <img :src="getFullImageUrl(img)" alt="" />
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDetailModal = false">关闭</el-button>
      </template>
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
import {
  getUnitListApi,
  addUnitApi,
  updateUnitApi,
  deleteUnitApi,
  changeUnitStatusApi,
  getBuildingListApi
} from '@/api/house.js'
import { initUploadConfig, getFullImageUrl } from '@/utils/imageUrl.js'
import { uploadImageApi } from '@/api/upload.js'

const searchForm = reactive({
  unitName: '',
  buildingName: '',
  status: null
})

const unitList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const showAddModal = ref(false)
const isEdit = ref(false)
const loading = ref(false)

// 图片预览
const showImagePreview = ref(false)
const previewImageUrl = ref('')
const previewPosition = ref({ x: 0, y: 0 })

// 详情对话框
const showDetailModal = ref(false)
const detailData = ref({})
const detailImages = ref([])

const form = reactive({
  id: null,
  buildingId: null,
  unitName: '',
  unitCode: '',
  rooms: 1,
  halls: 1,
  bathrooms: 1,
  area: 0,
  price: 0,
  orientation: '',
  floorRange: '',
  totalFloor: 0,
  description: '',
  images: '',
  status: 1
})

// 图片文件列表
const imageFileList = ref([])

// 楼盘列表（用于搜索）
const buildingOptions = ref([])
const buildingLoading = ref(false)
const buildingPageNum = ref(1)
const buildingPageSize = ref(50)
const buildingTotal = ref(0)

// 初始化获取数据
onMounted(async () => {
  await initUploadConfig()
  loadUnitList()
  loadInitialBuildings()
})

// 初始加载部分楼盘数据
const loadInitialBuildings = async () => {
  try {
    const response = await getBuildingListApi(1, buildingPageSize.value)
    
    if (response.data && response.data.records) {
      buildingOptions.value = response.data.records
      buildingTotal.value = response.data.total || 0
    } else if (response.data && response.data.data && response.data.data.records) {
      buildingOptions.value = response.data.data.records
      buildingTotal.value = response.data.data.total || 0
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
      // 使用楼盘名称模糊查询
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
    // 如果搜索为空，重新加载初始数据
    loadInitialBuildings()
  }
}

// 表单中的楼盘搜索（与搜索条件共用）
const searchBuildingsForForm = searchBuildings

// 获取户型列表
const loadUnitList = async () => {
  loading.value = true
  try {
    const response = await getUnitListApi(pageNum.value, pageSize.value, {
      buildingName: searchForm.buildingName || undefined,
      unitName: searchForm.unitName || undefined,
      status: searchForm.status || undefined
    })
    
    // 后端返回结构为 Result<PageResponse<HouseUnit>>
    // response.data 是 PageResponse 对象，包含 records, total 等字段
    if (response.data && response.data.records) {
      unitList.value = response.data.records
      total.value = response.data.total
    } else if (response.data && response.data.data && response.data.data.records) {
      // 如果响应包装了两层 data，则使用 response.data.data
      unitList.value = response.data.data.records
      total.value = response.data.data.total
    }
  } catch (error) {
    ElMessage.error('获取户型列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  loadUnitList()
}

// 重置
const handleReset = () => {
  searchForm.unitName = ''
  searchForm.buildingName = ''
  searchForm.status = null
  pageNum.value = 1
  loadUnitList()
}

// 添加
const handleAddClick = () => {
  isEdit.value = false
  resetForm()
  showAddModal.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  resetForm()
  form.id = row.id
  form.buildingId = row.buildingId
  form.unitName = row.unitName
  form.unitCode = row.unitCode
  form.rooms = row.rooms
  form.halls = row.halls
  form.bathrooms = row.bathrooms
  form.area = row.area
  form.price = row.price
  form.orientation = row.orientation
  form.floorRange = row.floorRange
  form.totalFloor = row.totalFloor
  form.description = row.description || ''
  form.images = row.images || ''
  form.status = row.status
  
  console.log('编辑户型数据:', row)
  console.log('images字段值:', row.images)
  
  // 设置图片列表
  if (form.images) {
    try {
      const imagesArray = JSON.parse(form.images)
      imageFileList.value = imagesArray.map((url, index) => ({
        name: `image${index + 1}`,
        url: getFullImageUrl(url),
        responseUrl: url
      }))
    } catch (e) {
      console.error('解析图片列表失败', e)
    }
  }
  
  showAddModal.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定删除户型 "${row.unitName}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteUnitApi(row.id)
      ElMessage.success('删除成功')
      loadUnitList()
    } catch (error) {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }).catch(() => {})
}

// 查看详情
const handleViewDetail = (row) => {
  detailData.value = { ...row }
  
  // 解析图片列表并转换URL
  if (row.images) {
    try {
      const imagesArray = JSON.parse(row.images)
      detailImages.value = imagesArray.map(url => getFullImageUrl(url))
    } catch (e) {
      console.error('解析图片列表失败', e)
      detailImages.value = []
    }
  } else {
    detailImages.value = []
  }
  
  showDetailModal.value = true
}

// 图片hover显示预览
const handleImageHover = (event, imageUrl) => {
  previewImageUrl.value = imageUrl
  showImagePreview.value = true
  
  // 计算预览位置（在鼠标右侧显示）
  const rect = event.target.getBoundingClientRect()
  previewPosition.value = {
    x: rect.right + 10,
    y: rect.top
  }
}

// 提交
const handleSubmit = async () => {
  if (!form.buildingId || !form.unitName) {
    ElMessage.error('请填写必填项')
    return
  }

  console.log('提交表单数据:', form)
  console.log('images字段值:', form.images)

  try {
    if (isEdit.value) {
      await updateUnitApi(form)
      ElMessage.success('更新成功')
    } else {
      await addUnitApi(form)
      ElMessage.success('添加成功')
    }
    showAddModal.value = false
    loadUnitList()
  } catch (error) {
    ElMessage.error('操作失败')
    console.error(error)
  }
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.buildingId = null
  form.unitName = ''
  form.unitCode = ''
  form.rooms = 1
  form.halls = 1
  form.bathrooms = 1
  form.area = 0
  form.price = 0
  form.orientation = ''
  form.floorRange = ''
  form.totalFloor = 0
  form.description = ''
  form.images = ''
  form.status = 1
  imageFileList.value = []
}

// 户型图上传请求
const uploadImageRequest = async (options) => {
  const { file } = options
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error(`图片 "${file.name}" 大小不能超过5MB`)
    options.onError?.()
    return
  }
  
  try {
    const response = await uploadImageApi(file, 'unit')
    if (response.data.code === 200) {
      const url = response.data.data.url
      imageFileList.value.push({
        name: file.name,
        url: getFullImageUrl(url),
        responseUrl: url
      })
      if (imageFileList.value.length > 9) {
        imageFileList.value = imageFileList.value.slice(0, 9)
        ElMessage.warning('最多只能上传9张图片')
      }
      updateImagesForm()
      options.onSuccess?.()
    } else {
      ElMessage.error('图片上传失败')
      options.onError?.()
    }
  } catch (error) {
    ElMessage.error('图片上传失败')
    options.onError?.()
  }
}

// 更新图片表单
const updateImagesForm = () => {
  const urls = imageFileList.value.map(f => f.responseUrl || f.url)
  form.images = JSON.stringify(urls)
}

// 处理图片移除
const handleImageRemove = (file, fileList) => {
  imageFileList.value = fileList
  updateImagesForm()
}

// 分页变化
const handlePageChange = (newPage) => {
  pageNum.value = newPage
  loadUnitList()
}

// 获取第一张图片
const getFirstImage = (imagesStr) => {
  if (!imagesStr) return ''
  try {
    const images = JSON.parse(imagesStr)
    return images.length > 0 ? images[0] : ''
  } catch (e) {
    return ''
  }
}

// 表格图片hover显示预览
const handleTableImageHover = (event, imageUrl) => {
  previewImageUrl.value = imageUrl
  showImagePreview.value = true
  
  // 计算预览位置（在鼠标右侧显示）
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
.unit-list {
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

.images-uploader {
  :deep(.el-upload--picture-card) {
    width: 100px;
    height: 100px;
  }
  
  :deep(.el-upload-list--picture-card .el-upload-list__item) {
    width: 100px;
    height: 100px;
  }
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.table-image {
  width: 60px;
  height: 60px;
  cursor: pointer;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  transition: all 0.3s;
}

.table-image:hover {
  border-color: #409eff;
  transform: scale(1.1);
}

.table-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-preview-popup {
  position: fixed;
  z-index: 9999;
  background: white;
  padding: 10px;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  max-width: 400px;
  max-height: 400px;
  pointer-events: none;
}

.image-preview-popup img {
  width: 100%;
  height: auto;
  display: block;
}

.detail-content {
  max-height: 600px;
  overflow-y: auto;
}

.description-text {
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.6;
}

.detail-section {
  margin-top: 20px;
}

.detail-section h4 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 14px;
}

.images-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image-item {
  width: 100px;
  height: 100px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s;
}

.image-item:hover {
  border-color: #409eff;
  transform: scale(1.05);
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>