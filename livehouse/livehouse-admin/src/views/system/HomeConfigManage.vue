<template>
  <div class="home-config-manage">
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">首页Banner配置</span>
          <el-button type="primary" @click="handleAddBanner">添加Banner</el-button>
        </div>
      </template>

      <div class="banner-list">
        <draggable v-model="bannerList" item-key="id" handle=".drag-handle" @end="handleDragEnd">
          <template #item="{ element, index }">
            <div class="banner-item">
              <div class="drag-handle">
                <el-icon><Rank /></el-icon>
              </div>
              <div class="banner-content">
                <div class="banner-image">
                  <img :src="element.coverImage || element.image" alt="" />
                  <div class="image-actions">
                    <el-button type="primary" size="small" @click="handleUploadImage(index)">更换图片</el-button>
                  </div>
                </div>
                <div class="banner-form">
                  <el-form :model="element" label-width="80px" size="small">
                    <el-form-item label="标题">
                      <el-input v-model="element.title" placeholder="请输入标题" />
                    </el-form-item>
                    <el-form-item label="跳转类型">
                      <el-select v-model="element.linkType" placeholder="请选择跳转类型" @change="handleLinkTypeChange(element)">
                        <el-option label="楼盘详情" value="house" />
                        <el-option label="直播详情" value="live" />
                        <el-option label="VR全景" value="vr" />
                        <el-option label="外部链接" value="external" />
                      </el-select>
                    </el-form-item>
                    <el-form-item v-if="element.linkType === 'house'" label="选择楼盘">
                      <el-select v-model="element.linkId" placeholder="请选择楼盘" filterable @change="handleBuildingChange(element)">
                        <el-option v-for="item in buildingOptions" :key="item.id" :label="item.label" :value="item.id" />
                      </el-select>
                    </el-form-item>
                    <el-form-item v-if="element.linkType === 'live'" label="选择直播">
                      <el-select v-model="element.linkId" placeholder="请选择直播" filterable @change="handleLiveChange(element)">
                        <el-option v-for="item in liveOptions" :key="item.id" :label="item.label" :value="item.id" />
                      </el-select>
                    </el-form-item>
                    <el-form-item v-if="element.linkType === 'vr'" label="选择VR">
                      <el-select v-model="element.linkId" placeholder="请选择VR" filterable @change="handleVrChange(element)">
                        <el-option v-for="item in vrOptions" :key="item.id" :label="item.label" :value="item.id" />
                      </el-select>
                    </el-form-item>
                    <el-form-item v-if="element.linkType === 'external'" label="外部链接">
                      <el-input v-model="element.externalUrl" placeholder="请输入外部链接URL" />
                    </el-form-item>
                  </el-form>
                </div>
              </div>
              <div class="banner-actions">
                <el-button type="danger" size="small" @click="handleDeleteBanner(index)">删除</el-button>
              </div>
            </div>
          </template>
        </draggable>

        <el-empty v-if="bannerList.length === 0" description="暂无Banner配置" />
      </div>
    </el-card>

    <div class="save-bar">
      <el-button type="primary" size="large" @click="handleSave" :loading="saving">保存配置</el-button>
    </div>

    <input ref="fileInput" type="file" accept="image/*" style="display: none" @change="handleFileChange" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Rank } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'
import { getHomeConfigListApi, updateHomeConfigApi } from '@/api/user.js'
import { getBuildingOptionsApi } from '@/api/user.js'
import { getLiveOptionsApi,getVrOptionsApi  } from '@/api/user.js'
import axios from '@/utils/request.js'

const bannerList = ref([])
const buildingOptions = ref([])
const liveOptions = ref([])
const vrOptions = ref([])
const saving = ref(false)
const fileInput = ref(null)
const currentUploadIndex = ref(-1)

const loadOptions = async () => {
  try {
    const [buildingRes, liveRes, vrRes] = await Promise.all([
      getBuildingOptionsApi(),
      getLiveOptionsApi(),
      getVrOptionsApi()
    ])
    buildingOptions.value = buildingRes.data.data || []
    liveOptions.value = liveRes.data.data || []
    vrOptions.value = vrRes.data.data || []
  } catch (error) {
    console.error('加载选项失败:', error)
  }
}

const loadBannerConfig = async () => {
  try {
    const res = await getHomeConfigListApi({ configKey: 'banners' })
    if (res.data.data && res.data.data.length > 0) {
      const config = res.data.data[0]
      if (config.configValue) {
        bannerList.value = JSON.parse(config.configValue)
      }
    }
  } catch (error) {
    console.error('加载Banner配置失败:', error)
  }
}

const handleAddBanner = () => {
  bannerList.value.push({
    id: Date.now(),
    title: '',
    coverImage: '',
    linkType: 'house',
    linkId: null,
    externalUrl: ''
  })
}

const handleDeleteBanner = (index) => {
  bannerList.value.splice(index, 1)
}

const handleDragEnd = () => {
  console.log('拖拽排序完成')
}

const handleLinkTypeChange = (item) => {
  item.linkId = null
  item.externalUrl = ''
}

const handleBuildingChange = (item) => {
  const building = buildingOptions.value.find(b => b.id === item.linkId)
  if (building) {
    item.title = building.label
    item.coverImage = building.coverImage
  }
}

const handleLiveChange = (item) => {
  const live = liveOptions.value.find(l => l.id === item.linkId)
  if (live) {
    item.title = live.label
    item.coverImage = live.coverImage
  }
}

const handleVrChange = (item) => {
  const vr = vrOptions.value.find(v => v.id === item.linkId)
  if (vr) {
    item.title = vr.label
    item.coverImage = vr.coverImage
  }
}

const handleUploadImage = (index) => {
  currentUploadIndex.value = index
  fileInput.value.click()
}

const handleFileChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('bizType', 'banner')

    const res = await axios.post('/api/common/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    if (res.data.data && res.data.data.url) {
      bannerList.value[currentUploadIndex.value].coverImage = res.data.data.url
      ElMessage.success('图片上传成功')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败')
  } finally {
    event.target.value = ''
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const res = await getHomeConfigListApi({ configKey: 'banners' })
    let configId = null
    if (res.data.data && res.data.data.length > 0) {
      configId = res.data.data[0].id
    }

    const configValue = JSON.stringify(bannerList.value)

    if (configId) {
      await updateHomeConfigApi(configId, { configValue })
    } else {
      await addHomeConfigApi({
        configKey: 'banners',
        configName: '首页Banner',
        configType: 'json',
        configValue,
        status: 1,
        sortOrder: 1
      })
    }

    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const addHomeConfigApi = (data) => {
  return axios.post('/api/system/home-config', data)
}

onMounted(() => {
  loadOptions()
  loadBannerConfig()
})
</script>

<style scoped>
.home-config-manage {
  padding: 20px;
}

.config-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.banner-list {
  min-height: 200px;
}

.banner-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 16px;
  background: #fff;
  transition: all 0.3s;
}

.banner-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.drag-handle {
  cursor: move;
  padding: 8px;
  color: #909399;
}

.drag-handle:hover {
  color: #409eff;
}

.banner-content {
  flex: 1;
  display: flex;
  gap: 20px;
}

.banner-image {
  width: 200px;
  height: 120px;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
  background: #f5f7fa;
}

.banner-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-actions {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  opacity: 0;
  transition: opacity 0.3s;
}

.banner-image:hover .image-actions {
  opacity: 1;
}

.banner-form {
  flex: 1;
  min-width: 300px;
}

.banner-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.save-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 20px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  display: flex;
  justify-content: center;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.1);
}

.save-bar .el-button {
  min-width: 200px;
}
</style>