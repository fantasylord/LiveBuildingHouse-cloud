<template>
  <div class="building-list">
    <div class="page-header">
      <h2>楼盘管理</h2>
      <el-button type="primary" @click="handleAddClick">
        <el-icon name="plus" />
        添加楼盘
      </el-button>
    </div>
    
    <el-card>
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="楼盘名称">
          <el-input v-model="searchForm.buildingName" placeholder="请输入楼盘名称" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="searchForm.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="在售" :value="1" />
            <el-option label="售罄" :value="0" />
          </el-select>
        </el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form>
      
      <el-table :data="buildingList" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="封面图" width="100">
          <template #default="scope">
            <div 
              v-if="scope.row.coverImage"
              class="table-image"
              @mouseenter="handleTableImageHover($event, getFullImageUrl(scope.row.coverImage))"
              @mouseleave="handleImageLeave"
            >
              <img :src="getFullImageUrl(scope.row.coverImage)" alt="" />
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="buildingName" label="楼盘名称" min-width="150" />
        <el-table-column prop="city" label="城市" width="100" />
        <el-table-column prop="address" label="地址" min-width="200" />
        <el-table-column prop="avgPrice" label="均价(元/㎡)" width="120">
          <template #default="scope">
            {{ scope.row.avgPrice ? scope.row.avgPrice.toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
              {{ scope.row.status === 1 ? '在售' : '售罄' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="scope">
            <el-button size="small" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
            <el-button size="small" link @click="handleViewDetail(scope.row)">详情</el-button>
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
      :title="isEdit ? '编辑楼盘' : '添加楼盘'" 
      v-model="showAddModal"
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="楼盘名称" required>
          <el-input v-model="form.buildingName" placeholder="请输入楼盘名称" />
        </el-form-item>
        <el-form-item label="楼盘编码">
          <el-input v-model="form.buildingCode" placeholder="请输入楼盘编码" />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="form.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市" required>
          <el-input v-model="form.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="区域">
          <el-input v-model="form.district" placeholder="请输入区域" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="均价(元/㎡)">
          <el-input-number v-model="form.avgPrice" :min="0" />
        </el-form-item>
        <el-form-item label="最低价(元/㎡)">
          <el-input-number v-model="form.minPrice" :min="0" />
        </el-form-item>
        <el-form-item label="最高价(元/㎡)">
          <el-input-number v-model="form.maxPrice" :min="0" />
        </el-form-item>
        <el-form-item label="楼盘类型">
          <el-select v-model="form.buildingType" placeholder="请选择楼盘类型" style="width: 100%">
            <el-option label="普通住宅" value="普通住宅" />
            <el-option label="别墅" value="别墅" />
            <el-option label="公寓" value="公寓" />
            <el-option label="商业" value="商业" />
          </el-select>
        </el-form-item>
        <el-form-item label="装修类型">
          <el-select v-model="form.decorationType" placeholder="请选择装修类型" style="width: 100%">
            <el-option label="毛坯" value="毛坯" />
            <el-option label="简装" value="简装" />
            <el-option label="精装" value="精装" />
          </el-select>
        </el-form-item>
        <el-form-item label="开发商">
          <el-input v-model="form.developer" placeholder="请输入开发商名称" />
        </el-form-item>
        <el-form-item label="物业公司">
          <el-input v-model="form.propertyCompany" placeholder="请输入物业公司名称" />
        </el-form-item>
        <el-form-item label="绿化率(%)">
          <el-input-number v-model="form.greenRate" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="容积率">
          <el-input-number v-model="form.plotRatio" :min="0" :step="0.1" />
        </el-form-item>
        <el-form-item label="总套数">
          <el-input-number v-model="form.totalUnits" :min="0" />
        </el-form-item>
        <el-form-item label="楼盘介绍">
          <el-input v-model="form.description" placeholder="请输入楼盘介绍" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-upload
            class="cover-uploader"
            :http-request="uploadCoverRequest"
            :show-file-list="false"
            :before-upload="beforeCoverUpload"
            accept="image/*"
          >
            <img v-if="coverImageUrl" :src="getFullImageUrl(coverImageUrl)" class="cover-image" />
            <el-icon v-else class="uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议尺寸: 800x600px，支持jpg/png格式，大小不超过5MB</div>
        </el-form-item>
        <el-form-item label="楼盘图片">
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
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="在售" :value="1" />
            <el-option label="售罄" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="请输入备注" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 详情对话框 -->
    <el-dialog 
      title="楼盘详情" 
      v-model="showDetailModal"
      width="800px"
    >
      <div class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="楼盘名称">{{ detailData.buildingName }}</el-descriptions-item>
          <el-descriptions-item label="楼盘编码">{{ detailData.buildingCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所在城市">{{ detailData.province }} {{ detailData.city }} {{ detailData.district }}</el-descriptions-item>
          <el-descriptions-item label="详细地址">{{ detailData.address }}</el-descriptions-item>
          <el-descriptions-item label="均价">{{ detailData.avgPrice ? detailData.avgPrice.toLocaleString() + ' 元/㎡' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="价格区间">{{ detailData.minPrice && detailData.maxPrice ? `${detailData.minPrice.toLocaleString()} - ${detailData.maxPrice.toLocaleString()} 元/㎡` : '-' }}</el-descriptions-item>
          <el-descriptions-item label="楼盘类型">{{ detailData.buildingType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="装修类型">{{ detailData.decorationType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="开发商">{{ detailData.developer || '-' }}</el-descriptions-item>
          <el-descriptions-item label="物业公司">{{ detailData.propertyCompany || '-' }}</el-descriptions-item>
          <el-descriptions-item label="绿化率">{{ detailData.greenRate ? detailData.greenRate + '%' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="容积率">{{ detailData.plotRatio || '-' }}</el-descriptions-item>
          <el-descriptions-item label="总套数">{{ detailData.totalUnits || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detailData.status === 1 ? 'success' : 'warning'">
              {{ detailData.status === 1 ? '在售' : '售罄' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="楼盘介绍" :span="2">
            <div class="description-text">{{ detailData.description || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            <div class="description-text">{{ detailData.remark || '-' }}</div>
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 封面图 -->
        <div v-if="detailData.coverImage" class="detail-section">
          <h4>封面图</h4>
          <div class="cover-preview">
            <img :src="getFullImageUrl(detailData.coverImage)" alt="封面图" />
          </div>
        </div>
        
        <!-- 楼盘图片 -->
        <div v-if="detailImages.length > 0" class="detail-section">
          <h4>楼盘图片</h4>
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
  getBuildingListApi,
  addBuildingApi,
  updateBuildingApi,
  deleteBuildingApi,
  changeBuildingStatusApi
} from '@/api/house.js'
import { initUploadConfig, getFullImageUrl } from '@/utils/imageUrl.js'
import { uploadImageApi } from '@/api/upload.js'

const searchForm = reactive({
  buildingName: '',
  city: '',
  status: null
})

const buildingList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const showAddModal = ref(false)
const isEdit = ref(false)
const loading = ref(false)

// 详情对话框
const showDetailModal = ref(false)
const detailData = ref({})
const detailImages = ref([])

// 图片预览
const showImagePreview = ref(false)
const previewImageUrl = ref('')
const previewPosition = ref({ x: 0, y: 0 })

const form = reactive({
  id: null,
  buildingName: '',
  buildingCode: '',
  province: '',
  city: '',
  district: '',
  address: '',
  avgPrice: 0,
  minPrice: 0,
  maxPrice: 0,
  buildingType: '',
  decorationType: '',
  developer: '',
  propertyCompany: '',
  greenRate: 0,
  plotRatio: 0,
  totalUnits: 0,
  description: '',
  coverImage: '',
  images: '',
  status: 1,
  isPrivate: 0,
  remark: ''
})

// 封面图预览URL
const coverImageUrl = ref('')
// 图片文件列表
const imageFileList = ref([])

// 初始化获取数据
onMounted(async () => {
  await initUploadConfig()
  loadBuildingList()
})

// 获取楼盘列表
const loadBuildingList = async () => {
  loading.value = true
  try {
    const response = await getBuildingListApi(pageNum.value, pageSize.value, {
      buildingName: searchForm.buildingName || undefined,
      city: searchForm.city || undefined,
      status: searchForm.status || undefined
    })
    
    // 后端返回结构为 Result<PageResponse<HouseBuilding>>
    // response.data 是 PageResponse 对象，包含 records, total 等字段
    if (response.data && response.data.records) {
      buildingList.value = response.data.records
      total.value = response.data.total
    } else if (response.data && response.data.data && response.data.data.records) {
      // 如果响应包装了两层 data，则使用 response.data.data
      buildingList.value = response.data.data.records
      total.value = response.data.data.total
    }
  } catch (error) {
    ElMessage.error('获取楼盘列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  loadBuildingList()
}

// 重置
const handleReset = () => {
  searchForm.buildingName = ''
  searchForm.city = ''
  searchForm.status = null
  pageNum.value = 1
  loadBuildingList()
}

// 添加
const handleAddClick = () => {
  isEdit.value = false
  resetForm()
  showAddModal.value = true
}

// 编辑
const handleEdit = async (row) => {
  isEdit.value = true
  resetForm()
  form.id = row.id
  form.buildingName = row.buildingName
  form.buildingCode = row.buildingCode
  form.province = row.province
  form.city = row.city
  form.district = row.district
  form.address = row.address
  form.avgPrice = row.avgPrice
  form.minPrice = row.minPrice
  form.maxPrice = row.maxPrice
  form.buildingType = row.buildingType || ''
  form.decorationType = row.decorationType || ''
  form.developer = row.developer
  form.propertyCompany = row.propertyCompany || ''
  form.greenRate = row.greenRate || 0
  form.plotRatio = row.plotRatio || 0
  form.totalUnits = row.totalUnits || 0
  form.description = row.description || ''
  form.coverImage = row.coverImage || ''
  form.images = row.images || ''
  form.status = row.status
  form.isPrivate = row.isPrivate || 0
  form.remark = row.remark
  
  // 设置封面图预览
  if (form.coverImage) {
    coverImageUrl.value = form.coverImage
  }
  
  // 设置图片列表
  if (form.images) {
    try {
      const imagesArray = JSON.parse(form.images)
      imageFileList.value = imagesArray.map((url, index) => ({
        name: `image${index + 1}`,
        url: getFullImageUrl(url)
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
    `确定删除楼盘 "${row.buildingName}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteBuildingApi(row.id)
      ElMessage.success('删除成功')
      loadBuildingList()
    } catch (error) {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }).catch(() => {
    // 用户取消删除
  })
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

// 提交表单
const handleSubmit = async () => {
  if (!form.buildingName || !form.city) {
    ElMessage.error('请填写必填项')
    return
  }

  try {
    if (isEdit.value) {
      await updateBuildingApi(form)
      ElMessage.success('更新成功')
    } else {
      await addBuildingApi(form)
      ElMessage.success('添加成功')
    }
    showAddModal.value = false
    loadBuildingList()
  } catch (error) {
    ElMessage.error('操作失败')
    console.error(error)
  }
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.buildingName = ''
  form.buildingCode = ''
  form.province = ''
  form.city = ''
  form.district = ''
  form.address = ''
  form.avgPrice = 0
  form.minPrice = 0
  form.maxPrice = 0
  form.buildingType = ''
  form.decorationType = ''
  form.developer = ''
  form.propertyCompany = ''
  form.greenRate = 0
  form.plotRatio = 0
  form.totalUnits = 0
  form.description = ''
  form.coverImage = ''
  form.images = ''
  form.status = 1
  form.isPrivate = 0
  form.remark = ''
  coverImageUrl.value = ''
  imageFileList.value = []
}

// 封面图上传前检查
const beforeCoverUpload = (file) => {
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('封面图大小不能超过5MB')
    return false
  }
  return true
}

// 封面图上传请求
const uploadCoverRequest = async (options) => {
  const { file } = options
  try {
    const response = await uploadImageApi(file, 'cover')
    if (response.data.code === 200) {
      const url = response.data.data.url
      coverImageUrl.value = url
      form.coverImage = url
      options.onSuccess?.()
    } else {
      ElMessage.error('封面图上传失败')
      options.onError?.()
    }
  } catch (error) {
    ElMessage.error('封面图上传失败')
    options.onError?.()
  }
}

// 楼盘图片上传请求
const uploadImageRequest = async (options) => {
  const { file } = options
  try {
    const response = await uploadImageApi(file, 'building')
    if (response.data.code === 200) {
      const url = response.data.data.url
      imageFileList.value.push({
        name: file.name,
        url: getFullImageUrl(url),
        responseUrl: url
      })
      if (imageFileList.value.length > 9) {
        imageFileList.value = imageFileList.value.slice(0, 9)
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

// 状态变化
const handleStatusChange = async (row) => {
  try {
    await changeBuildingStatusApi(row.id, row.status)
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    // 恢复状态
    loadBuildingList()
  }
}

// 分页变化
const handlePageChange = (newPage) => {
  pageNum.value = newPage
  loadBuildingList()
}
</script>

<style scoped>
.building-list {
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

.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.cover-uploader:hover {
  border-color: #409eff;
}

.cover-image {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
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

.cover-preview {
  width: 100%;
  max-width: 400px;
}

.cover-preview img {
  width: 100%;
  height: auto;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
</style>