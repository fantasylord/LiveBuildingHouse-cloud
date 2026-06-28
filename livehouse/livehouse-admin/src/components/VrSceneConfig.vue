<template>
  <div class="vr-scene-config">
    <div class="config-header">
      <span class="title">场景配置</span>
      <el-button type="primary" size="small" @click="handleAddScene">
        <el-icon><Plus /></el-icon>
        添加场景
      </el-button>
    </div>

    <div class="floor-plan-section">
      <span class="section-title">户型模型图</span>
      <el-upload
        class="image-uploader"
        :http-request="uploadFloorPlanRequest"
        :show-file-list="false"
        :before-upload="beforeFloorPlanUpload"
        accept="image/*"
      >
        <img v-if="scenesData.floorPlanImage" :src="getFullImageUrl(scenesData.floorPlanImage)" class="uploaded-image" />
        <el-icon v-else class="image-uploader-icon"><Picture /></el-icon>
      </el-upload>
      <div class="upload-tip">支持jpg/png格式，单张不超过10MB</div>
    </div>

    <div class="scene-list">
      <div v-if="scenesData.scenes.length === 0" class="empty-list">
        <span>暂无场景配置</span>
      </div>
      <div
        v-for="(scene, index) in scenesData.scenes"
        :key="scene.id || index"
        class="scene-item"
        :class="{ active: selectedSceneIndex === index }"
      >
        <div class="scene-header" @click="toggleSceneExpand(index)">
          <div class="scene-icon">
            <el-icon><MapLocation /></el-icon>
          </div>
          <div class="scene-info">
            <span class="scene-name">{{ scene.name || '未命名场景' }}</span>
            <span class="scene-code">{{ scene.code || '' }}</span>
          </div>
          <div class="scene-actions">
            <el-button size="small" @click.stop="handleEditScene(index)">编辑</el-button>
            <el-button size="small" type="danger" @click.stop="handleDeleteScene(index)">删除</el-button>
          </div>
          <el-icon :class="{ expanded: expandedScenes[index] }" class="expand-icon"><ArrowRight /></el-icon>
        </div>

        <div v-if="expandedScenes[index]" class="scene-detail">
          <div class="detail-row">
            <span class="detail-label">全景图</span>
            <el-upload
              class="mini-uploader"
              :http-request="(options) => uploadSceneImageRequest(options, index, 'panoramaUrl')"
              :show-file-list="false"
              :before-upload="beforeSceneImageUpload"
              accept="image/*"
            >
              <img v-if="scene.panoramaUrl" :src="getFullImageUrl(scene.panoramaUrl)" class="mini-image" />
              <el-icon v-else class="mini-upload-icon"><Plus /></el-icon>
            </el-upload>
          </div>
          <div class="detail-row">
            <span class="detail-label">缩略图</span>
            <el-upload
              class="mini-uploader"
              :http-request="(options) => uploadSceneImageRequest(options, index, 'thumbnailUrl')"
              :show-file-list="false"
              :before-upload="beforeSceneImageUpload"
              accept="image/*"
            >
              <img v-if="scene.thumbnailUrl" :src="getFullImageUrl(scene.thumbnailUrl)" class="mini-image" />
              <el-icon v-else class="mini-upload-icon"><Plus /></el-icon>
            </el-upload>
          </div>
          <div class="detail-row">
            <span class="detail-label">户型点位</span>
            <div class="position-inputs">
              <el-input-number v-model="scene.modelX" :min="0" :max="100" style="width: 80px" />
              <span>×</span>
              <el-input-number v-model="scene.modelY" :min="0" :max="100" style="width: 80px" />
              <span>%</span>
            </div>
          </div>
          <div class="detail-row">
            <span class="detail-label">初始视角</span>
            <div class="position-inputs">
              <span>左右</span>
              <el-input-number v-model="scene.initialLon" :min="-180" :max="180" style="width: 80px" />
              <span>上下</span>
              <el-input-number v-model="scene.initialLat" :min="-90" :max="90" style="width: 80px" />
            </div>
          </div>

          <div class="hotspot-section">
            <div class="hotspot-header">
              <span class="hotspot-title">热点配置</span>
              <el-button size="small" type="primary" @click="handleAddHotspot(index)">
                <el-icon><Plus /></el-icon>
                添加热点
              </el-button>
            </div>
            <div v-if="!scene.panoramaUrl" class="no-image-tip">
              请先上传全景图以配置热点
            </div>
            <div v-else class="hotspot-preview">
              <SphericalPreview
                :image-url="scene.panoramaUrl"
                :hotspots="scene.hotspots || []"
                @add="(percent) => handlePreviewAddHotspot(index, percent)"
                @marker-click="(hIndex) => handleEditHotspot(index, hIndex)"
              />
            </div>
            <div class="hotspot-list">
              <div
                v-for="(hotspot, hIndex) in scene.hotspots"
                :key="hIndex"
                class="hotspot-item"
              >
                <div class="hotspot-info">
                  <span class="hotspot-title-text">{{ hotspot.title || '未命名' }}</span>
                  <span class="hotspot-target" v-if="hotspot.targetSceneId">
                    → {{ getSceneName(hotspot.targetSceneId) }}
                  </span>
                </div>
                <div class="hotspot-actions">
                  <el-button size="small" @click="handleEditHotspot(index, hIndex)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleDeleteHotspot(index, hIndex)">删除</el-button>
                </div>
              </div>
              <div v-if="!scene.hotspots || scene.hotspots.length === 0" class="empty-hotspots">
                暂无热点
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showSceneModal" :title="editingSceneIndex !== null ? '编辑场景' : '添加场景'" width="500px">
      <el-form :model="sceneForm" label-width="80px">
        <el-form-item label="场景名称" required>
          <el-input v-model="sceneForm.name" placeholder="请输入场景名称" />
        </el-form-item>
        <el-form-item label="场景编码" required>
          <el-input v-model="sceneForm.code" placeholder="请输入场景编码" />
        </el-form-item>
        <el-form-item label="简短名称">
          <el-input v-model="sceneForm.shortName" placeholder="如：客、卧" maxlength="2" />
        </el-form-item>
        <el-form-item label="全景图">
          <el-upload
            class="image-uploader"
            :http-request="uploadSceneImageModalRequest"
            :show-file-list="false"
            :before-upload="beforeSceneImageUpload"
            accept="image/*"
          >
            <img v-if="sceneForm.panoramaUrl" :src="getFullImageUrl(sceneForm.panoramaUrl)" class="uploaded-image" />
            <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="缩略图">
          <el-upload
            class="image-uploader"
            :http-request="uploadSceneThumbModalRequest"
            :show-file-list="false"
            :before-upload="beforeSceneImageUpload"
            accept="image/*"
          >
            <img v-if="sceneForm.thumbnailUrl" :src="getFullImageUrl(sceneForm.thumbnailUrl)" class="uploaded-image" />
            <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="户型点位">
          <div class="position-inputs">
            <el-input-number v-model="sceneForm.modelX" :min="0" :max="100" style="width: 80px" />
            <span>×</span>
            <el-input-number v-model="sceneForm.modelY" :min="0" :max="100" style="width: 80px" />
            <span>%</span>
          </div>
        </el-form-item>
        <el-form-item label="初始视角">
          <div class="position-inputs">
            <span>左右</span>
            <el-input-number v-model="sceneForm.initialLon" :min="-180" :max="180" style="width: 80px" />
            <span>上下</span>
            <el-input-number v-model="sceneForm.initialLat" :min="-90" :max="90" style="width: 80px" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSceneModal = false">取消</el-button>
        <el-button type="primary" @click="handleSaveScene">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showHotspotModal" :title="editingHotspotIndex !== null ? '编辑热点' : '添加热点'" width="400px">
      <el-form :model="hotspotForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="hotspotForm.title" placeholder="请输入热点标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="hotspotForm.description" type="textarea" rows="3" placeholder="请输入热点描述" />
        </el-form-item>
        <el-form-item label="位置">
          <div class="position-inputs">
            <el-input-number v-model="hotspotForm.x" :min="0" :max="100" style="width: 80px" />
            <span>×</span>
            <el-input-number v-model="hotspotForm.y" :min="0" :max="100" style="width: 80px" />
            <span>%</span>
          </div>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="hotspotForm.type">
            <el-option label="场景跳转" value="scene" />
            <el-option label="信息展示" value="info" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标场景" v-if="hotspotForm.type === 'scene'">
          <el-select v-model="hotspotForm.targetSceneId" placeholder="请选择目标场景">
            <el-option
              v-for="(s, idx) in scenesData.scenes"
              :key="s.id || idx"
              :label="s.name || '未命名'"
              :value="s.id || s.code || String(idx)"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showHotspotModal = false">取消</el-button>
        <el-button type="primary" @click="handleSaveHotspot">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { Plus, Picture, MapLocation, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { uploadImageApi } from '@/api/upload.js'
import { getFullImageUrl } from '@/utils/imageUrl.js'
import SphericalPreview from './SphericalPreview.vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  defaultPanoramaUrl: {
    type: String,
    default: ''
  },
  defaultThumbnailUrl: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

const scenesData = reactive({
  floorPlanImage: '',
  scenes: []
})

const selectedSceneIndex = ref(null)
const expandedScenes = ref({})
const showSceneModal = ref(false)
const editingSceneIndex = ref(null)
const showHotspotModal = ref(false)
const editingHotspotIndex = ref(null)
const editingParentSceneIndex = ref(null)

const sceneForm = reactive({
  id: '',
  code: '',
  name: '',
  shortName: '',
  panoramaUrl: '',
  thumbnailUrl: '',
  modelX: 50,
  modelY: 50,
  initialLon: 0,
  initialLat: 0
})

const hotspotForm = reactive({
  title: '',
  description: '',
  x: 50,
  y: 50,
  type: 'scene',
  targetSceneId: ''
})

watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    try {
      const parsed = JSON.parse(newValue)
      scenesData.floorPlanImage = parsed.floorPlanImage || ''
      scenesData.scenes = parsed.scenes || []
      if (!scenesData.scenes.length && props.defaultPanoramaUrl) {
        scenesData.scenes = [{
          id: 'scene1',
          code: 'scene1',
          name: '默认场景',
          shortName: '',
          panoramaUrl: props.defaultPanoramaUrl,
          thumbnailUrl: props.defaultThumbnailUrl,
          modelX: 50,
          modelY: 50,
          initialLon: 0,
          initialLat: 0,
          hotspots: []
        }]
      }
    } catch {
      scenesData.floorPlanImage = ''
      scenesData.scenes = []
    }
  } else {
    scenesData.floorPlanImage = ''
    scenesData.scenes = []
    if (props.defaultPanoramaUrl) {
      scenesData.scenes = [{
        id: 'scene1',
        code: 'scene1',
        name: '默认场景',
        shortName: '',
        panoramaUrl: props.defaultPanoramaUrl,
        thumbnailUrl: props.defaultThumbnailUrl,
        modelX: 50,
        modelY: 50,
        initialLon: 0,
        initialLat: 0,
        hotspots: []
      }]
    }
  }
}, { immediate: true })

watch(scenesData, (newValue) => {
  emit('update:modelValue', JSON.stringify({
    floorPlanImage: newValue.floorPlanImage,
    scenes: newValue.scenes
  }))
}, { deep: true })

const toggleSceneExpand = (index) => {
  expandedScenes.value[index] = !expandedScenes.value[index]
}

const handleAddScene = () => {
  editingSceneIndex.value = null
  Object.assign(sceneForm, {
    id: '',
    code: `scene${Date.now()}`,
    name: '',
    shortName: '',
    panoramaUrl: '',
    thumbnailUrl: '',
    modelX: 50,
    modelY: 50,
    initialLon: 0,
    initialLat: 0
  })
  showSceneModal.value = true
}

const handleEditScene = (index) => {
  editingSceneIndex.value = index
  const scene = scenesData.scenes[index]
  Object.assign(sceneForm, {
    id: scene.id || '',
    code: scene.code || '',
    name: scene.name || '',
    shortName: scene.shortName || '',
    panoramaUrl: scene.panoramaUrl || '',
    thumbnailUrl: scene.thumbnailUrl || '',
    modelX: scene.modelX || 50,
    modelY: scene.modelY || 50,
    initialLon: scene.initialLon || 0,
    initialLat: scene.initialLat || 0
  })
  showSceneModal.value = true
}

const handleDeleteScene = (index) => {
  scenesData.scenes.splice(index, 1)
  delete expandedScenes.value[index]
  if (selectedSceneIndex.value === index) {
    selectedSceneIndex.value = null
  }
}

const handleSaveScene = () => {
  if (!sceneForm.name.trim() || !sceneForm.code.trim()) {
    ElMessage.error('请填写场景名称和编码')
    return
  }
  const newScene = {
    id: sceneForm.id || sceneForm.code,
    code: sceneForm.code,
    name: sceneForm.name,
    shortName: sceneForm.shortName,
    panoramaUrl: sceneForm.panoramaUrl,
    thumbnailUrl: sceneForm.thumbnailUrl,
    modelX: sceneForm.modelX,
    modelY: sceneForm.modelY,
    initialLon: sceneForm.initialLon,
    initialLat: sceneForm.initialLat,
    hotspots: sceneForm.hotspots || []
  }
  if (editingSceneIndex.value !== null) {
    scenesData.scenes[editingSceneIndex.value] = { ...scenesData.scenes[editingSceneIndex.value], ...newScene }
  } else {
    scenesData.scenes.push(newScene)
  }
  showSceneModal.value = false
}

const handleAddHotspot = (sceneIndex) => {
  editingParentSceneIndex.value = sceneIndex
  editingHotspotIndex.value = null
  Object.assign(hotspotForm, {
    title: '',
    description: '',
    x: 50,
    y: 50,
    type: 'scene',
    targetSceneId: ''
  })
  showHotspotModal.value = true
}

const handlePreviewAddHotspot = (sceneIndex, percent) => {
  editingParentSceneIndex.value = sceneIndex
  editingHotspotIndex.value = null
  Object.assign(hotspotForm, {
    title: '',
    description: '',
    x: percent.x,
    y: percent.y,
    type: 'scene',
    targetSceneId: ''
  })
  showHotspotModal.value = true
}

const handleEditHotspot = (sceneIndex, hotspotIndex) => {
  editingParentSceneIndex.value = sceneIndex
  editingHotspotIndex.value = hotspotIndex
  const hotspot = scenesData.scenes[sceneIndex].hotspots[hotspotIndex]
  Object.assign(hotspotForm, {
    title: hotspot.title || '',
    description: hotspot.description || '',
    x: hotspot.x || 50,
    y: hotspot.y || 50,
    type: hotspot.type || 'scene',
    targetSceneId: hotspot.targetSceneId || ''
  })
  showHotspotModal.value = true
}

const handleDeleteHotspot = (sceneIndex, hotspotIndex) => {
  scenesData.scenes[sceneIndex].hotspots.splice(hotspotIndex, 1)
}

const handleSaveHotspot = () => {
  if (!hotspotForm.title.trim()) {
    ElMessage.error('请填写热点标题')
    return
  }
  const newHotspot = {
    title: hotspotForm.title,
    description: hotspotForm.description,
    x: hotspotForm.x,
    y: hotspotForm.y,
    type: hotspotForm.type,
    targetSceneId: hotspotForm.type === 'scene' ? hotspotForm.targetSceneId : undefined
  }
  if (!scenesData.scenes[editingParentSceneIndex.value].hotspots) {
    scenesData.scenes[editingParentSceneIndex.value].hotspots = []
  }
  if (editingHotspotIndex.value !== null) {
    scenesData.scenes[editingParentSceneIndex.value].hotspots[editingHotspotIndex.value] = newHotspot
  } else {
    scenesData.scenes[editingParentSceneIndex.value].hotspots.push(newHotspot)
  }
  showHotspotModal.value = false
}

const getSceneName = (targetSceneId) => {
  const scene = scenesData.scenes.find(s => s.id === targetSceneId || s.code === targetSceneId)
  return scene ? scene.name : '未知场景'
}

const uploadFloorPlanRequest = async (options) => {
  try {
    const response = await uploadImageApi(options.file, 'vr')
    if (response && response.data && response.data.code === 200 && response.data.data) {
      scenesData.floorPlanImage = response.data.data.url
      ElMessage.success('户型模型图上传成功')
      options.onSuccess(response.data)
    } else {
      options.onError(new Error('上传失败'))
    }
  } catch (error) {
    options.onError(error)
  }
}

const uploadSceneImageRequest = async (options, sceneIndex, field) => {
  try {
    const response = await uploadImageApi(options.file, 'vr')
    if (response && response.data && response.data.code === 200 && response.data.data) {
      scenesData.scenes[sceneIndex][field] = response.data.data.url
      ElMessage.success('图片上传成功')
      options.onSuccess(response.data)
    } else {
      options.onError(new Error('上传失败'))
    }
  } catch (error) {
    options.onError(error)
  }
}

const uploadSceneImageModalRequest = async (options) => {
  try {
    const response = await uploadImageApi(options.file, 'vr')
    if (response && response.data && response.data.code === 200 && response.data.data) {
      sceneForm.panoramaUrl = response.data.data.url
      ElMessage.success('全景图上传成功')
      options.onSuccess(response.data)
    } else {
      options.onError(new Error('上传失败'))
    }
  } catch (error) {
    options.onError(error)
  }
}

const uploadSceneThumbModalRequest = async (options) => {
  try {
    const response = await uploadImageApi(options.file, 'vr')
    if (response && response.data && response.data.code === 200 && response.data.data) {
      sceneForm.thumbnailUrl = response.data.data.url
      ElMessage.success('缩略图上传成功')
      options.onSuccess(response.data)
    } else {
      options.onError(new Error('上传失败'))
    }
  } catch (error) {
    options.onError(error)
  }
}

const beforeFloorPlanUpload = (file) => {
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过10MB')
    return false
  }
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  return true
}

const beforeSceneImageUpload = (file) => {
  const maxSize = 50 * 1024 * 1024
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
</script>

<style scoped>
.vr-scene-config {
  width: 100%;
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.title {
  font-weight: bold;
}

.floor-plan-section {
  margin-bottom: 20px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.section-title {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
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

.scene-list {
  max-height: 500px;
  overflow-y: auto;
}

.empty-list {
  text-align: center;
  color: #909399;
  padding: 20px;
}

.scene-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 12px;
  overflow: hidden;
}

.scene-item.active {
  border-color: #409eff;
}

.scene-header {
  display: flex;
  align-items: center;
  padding: 12px;
  cursor: pointer;
  background: #fafafa;
  transition: background 0.2s;
}

.scene-header:hover {
  background: #ecf5ff;
}

.scene-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.scene-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.scene-name {
  font-weight: 500;
}

.scene-code {
  font-size: 12px;
  color: #909399;
}

.scene-actions {
  display: flex;
  gap: 8px;
  margin-right: 12px;
}

.expand-icon {
  font-size: 16px;
  color: #909399;
  transition: transform 0.2s;
}

.expand-icon.expanded {
  transform: rotate(90deg);
}

.scene-detail {
  padding: 16px;
  border-top: 1px solid #ebeef5;
}

.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.detail-label {
  width: 80px;
  font-size: 14px;
  color: #606266;
}

.position-inputs {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mini-uploader {
  width: 80px;
  height: 80px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mini-uploader:hover {
  border-color: #409eff;
}

.mini-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.mini-upload-icon {
  font-size: 20px;
  color: #8c939d;
}

.hotspot-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #d9d9d9;
}

.hotspot-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.hotspot-title {
  font-weight: 500;
}

.no-image-tip {
  text-align: center;
  color: #909399;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 6px;
}

.hotspot-preview {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 12px;
}

.hotspot-preview-image {
  width: 100%;
  height: auto;
}

.hotspot-marker {
  position: absolute;
  transform: translate(-50%, -50%);
  cursor: pointer;
}

.marker-icon {
  font-size: 20px;
  color: #ee0a24;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  padding: 4px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

.hotspot-list {
  margin-top: 8px;
}

.hotspot-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #fafafa;
  border-radius: 6px;
  margin-bottom: 6px;
}

.hotspot-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.hotspot-title-text {
  font-size: 14px;
}

.hotspot-target {
  font-size: 12px;
  color: #409eff;
}

.hotspot-actions {
  display: flex;
  gap: 8px;
}

.empty-hotspots {
  text-align: center;
  color: #909399;
  padding: 12px;
  font-size: 13px;
}
</style>
