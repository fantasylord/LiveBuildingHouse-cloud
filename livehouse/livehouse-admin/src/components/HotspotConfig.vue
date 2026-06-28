<template>
  <div class="hotspot-config">
    <div class="config-header">
      <span class="title">热点配置</span>
      <el-button type="primary" size="small" @click="handleAddHotspot" :disabled="!imageUrl">
        <el-icon><Plus /></el-icon>
        添加热点
      </el-button>
    </div>

    <div v-if="imageUrl" class="image-container">
      <SphericalPreview
        :image-url="imageUrl"
        :hotspots="hotspots"
        @add="handlePreviewAdd"
        @marker-click="handleMarkerClick"
      />
    </div>

    <div v-else class="empty-image">
      <el-icon class="empty-icon"><Picture /></el-icon>
      <span>请先上传全景图</span>
    </div>

    <div class="hotspot-list">
      <div v-if="hotspots.length === 0" class="empty-list">
        <span>暂无热点配置</span>
      </div>
      <div
        v-for="(hotspot, index) in hotspots"
        :key="index"
        class="hotspot-item"
        :class="{ active: selectedIndex === index }"
        @click="handleMarkerClick(index)"
      >
        <div class="item-header">
          <span class="item-index">{{ index + 1 }}</span>
          <span class="item-title">{{ hotspot.title || '未命名热点' }}</span>
        </div>
        <div class="item-actions">
          <el-button size="small" @click.stop="handleEditHotspot(index)">编辑</el-button>
          <el-button size="small" type="danger" @click.stop="handleDeleteHotspot(index)">删除</el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="showEditModal" :title="editingIndex !== null ? '编辑热点' : '添加热点'" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="editForm.title" placeholder="请输入热点标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" rows="3" placeholder="请输入热点描述" />
        </el-form-item>
        <el-form-item label="位置">
          <div class="position-row">
            <el-input-number v-model="editForm.x" :min="0" :max="100" style="width: 100px" />
            <span class="position-separator">×</span>
            <el-input-number v-model="editForm.y" :min="0" :max="100" style="width: 100px" />
            <span class="position-unit">%</span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditModal = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, reactive } from 'vue'
import { Plus, Picture } from '@element-plus/icons-vue'
import SphericalPreview from './SphericalPreview.vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  imageUrl: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

const hotspots = ref([])
const selectedIndex = ref(null)
const showEditModal = ref(false)
const editingIndex = ref(null)

const editForm = reactive({
  title: '',
  description: '',
  x: 50,
  y: 50
})

watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    try {
      const parsed = JSON.parse(newValue)
      hotspots.value = Array.isArray(parsed) ? parsed : []
    } catch {
      hotspots.value = []
    }
  } else {
    hotspots.value = []
  }
}, { immediate: true })

watch(hotspots, (newValue) => {
  emit('update:modelValue', JSON.stringify(newValue))
}, { deep: true })

const handlePreviewAdd = (percent) => {
  editingIndex.value = null
  editForm.title = ''
  editForm.description = ''
  editForm.x = percent.x
  editForm.y = percent.y
  showEditModal.value = true
}

const handleMarkerClick = (index) => {
  selectedIndex.value = index
}

const handleAddHotspot = () => {
  editingIndex.value = null
  editForm.title = ''
  editForm.description = ''
  editForm.x = 50
  editForm.y = 50
  showEditModal.value = true
}

const handleEditHotspot = (index) => {
  editingIndex.value = index
  const hotspot = hotspots.value[index]
  editForm.title = hotspot.title || ''
  editForm.description = hotspot.description || ''
  editForm.x = hotspot.x || 50
  editForm.y = hotspot.y || 50
  selectedIndex.value = index
  showEditModal.value = true
}

const handleDeleteHotspot = (index) => {
  hotspots.value.splice(index, 1)
  if (selectedIndex.value === index) {
    selectedIndex.value = null
  }
}

const handleSaveEdit = () => {
  if (!editForm.title.trim()) {
    return
  }
  const newHotspot = {
    title: editForm.title,
    description: editForm.description,
    x: editForm.x,
    y: editForm.y,
    type: 'info'
  }
  if (editingIndex.value !== null) {
    hotspots.value[editingIndex.value] = newHotspot
  } else {
    hotspots.value.push(newHotspot)
    selectedIndex.value = hotspots.value.length - 1
  }
  showEditModal.value = false
}
</script>

<style scoped>
.hotspot-config {
  width: 100%;
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.title {
  font-weight: bold;
}

.image-container {
  position: relative;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  background: #fafafa;
}

.empty-image {
  width: 100%;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  color: #909399;
  background: #fafafa;
}

.empty-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.hotspot-list {
  margin-top: 16px;
  max-height: 200px;
  overflow-y: auto;
}

.empty-list {
  text-align: center;
  color: #909399;
  padding: 20px;
}

.hotspot-item {
  padding: 10px 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.hotspot-item:hover,
.hotspot-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.item-index {
  font-size: 12px;
  color: #fff;
  background: #409eff;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
}

.item-actions {
  display: flex;
  gap: 8px;
}

.position-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.position-separator {
  font-weight: bold;
  color: #666;
}

.position-unit {
  color: #909399;
}
</style>
