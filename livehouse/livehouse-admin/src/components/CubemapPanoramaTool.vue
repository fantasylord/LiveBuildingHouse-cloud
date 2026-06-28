<template>
  <div class="cubemap-tool">
    <div class="batch-panel">
      <div class="batch-actions">
        <input
          ref="batchFileInputRef"
          class="hidden-input"
          type="file"
          accept="image/*"
          multiple="multiple"
          @change="handleBatchChange"
        />
        <input
          ref="folderInputRef"
          class="hidden-input"
          type="file"
          accept="image/*"
          webkitdirectory
          directory
          multiple="multiple"
          @change="handleBatchChange"
        />
        <el-button type="primary" @click="openBatchFilePicker">批量选择六面图</el-button>
        <el-button @click="openFolderPicker">选择六面图文件夹</el-button>
        <el-button size="small" @click="resetRules">恢复默认规则</el-button>
      </div>
      <div class="batch-tip">
        默认识别 b/d/f/l/r/u 图片名；如果系统不允许一次选择多张图，请直接选择包含六面图的文件夹。
      </div>

      <div class="rule-grid">
        <div v-for="face in faceList" :key="face.key" class="rule-item">
          <span>{{ face.label }}</span>
          <el-input
            v-model="nameRules[face.key]"
            size="small"
            placeholder="多个名称用逗号分隔"
          />
        </div>
      </div>
    </div>

    <div class="face-grid">
      <div v-for="face in faceList" :key="face.key" class="face-tile">
        <div class="face-title">{{ face.label }}</div>
        <label class="face-picker">
          <input type="file" accept="image/*" @change="(event) => handleFaceChange(face.key, event)" />
          <img v-if="faces[face.key]?.preview" :src="faces[face.key].preview" alt="" />
          <span v-else>选择图片</span>
        </label>
      </div>
    </div>

    <div class="settings-row">
      <span>输出尺寸</span>
      <el-select v-model="outputWidth" style="width: 140px">
        <el-option label="2048 x 1024" :value="2048" />
        <el-option label="4096 x 2048" :value="4096" />
        <el-option label="8192 x 4096" :value="8192" />
      </el-select>
      <el-button type="primary" :loading="generating" @click="generatePanorama">合成全景图</el-button>
    </div>

    <div v-if="previewUrl" class="preview-panel">
      <img :src="previewUrl" alt="" />
      <div class="action-row">
        <el-button @click="downloadPanorama">下载全景图</el-button>
        <el-button type="primary" :loading="uploading" @click="uploadPanorama">上传并使用</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadImageApi } from '@/api/upload.js'

const emit = defineEmits(['uploaded'])

const faceList = [
  { key: 'px', label: '右 +X' },
  { key: 'nx', label: '左 -X' },
  { key: 'py', label: '上 +Y' },
  { key: 'ny', label: '下 -Y' },
  { key: 'pz', label: '前 +Z' },
  { key: 'nz', label: '后 -Z' }
]

const defaultNameRules = {
  px: 'r,right,px,+x',
  nx: 'l,left,nx,-x',
  py: 'u,up,py,+y',
  ny: 'd,down,ny,-y',
  pz: 'f,front,pz,+z',
  nz: 'b,back,nz,-z'
}

const faces = reactive({})
const nameRules = reactive({ ...defaultNameRules })
const batchFileInputRef = ref(null)
const folderInputRef = ref(null)
const outputWidth = ref(4096)
const previewUrl = ref('')
const outputBlob = ref(null)
const generating = ref(false)
const uploading = ref(false)

const handleFaceChange = async (key, event) => {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  await assignFaceFile(key, file)
}

const openBatchFilePicker = () => {
  batchFileInputRef.value?.click()
}

const openFolderPicker = () => {
  folderInputRef.value?.click()
}

const handleBatchChange = async (event) => {
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  if (files.length === 0) return

  const imageFiles = files.filter((file) => file.type.startsWith('image/'))
  if (imageFiles.length !== files.length) {
    ElMessage.warning('已忽略非图片文件')
  }

  let matchedCount = 0
  const duplicatedFaces = []
  for (const file of imageFiles) {
    const faceKey = matchFaceByName(file.name)
    if (!faceKey) continue
    if (faces[faceKey]?.image) {
      duplicatedFaces.push(faceLabel(faceKey))
    }
    await assignFaceFile(faceKey, file, false)
    matchedCount += 1
  }

  const missing = faceList.filter((face) => !faces[face.key]?.image).map((face) => face.label)
  if (matchedCount === 0) {
    ElMessage.error('未按当前规则匹配到六面图，请检查文件名或调整名称规则')
  } else if (missing.length > 0) {
    ElMessage.warning(`已匹配${matchedCount}张，还缺：${missing.join('、')}`)
  } else {
    ElMessage.success('六面图已全部匹配完成')
  }

  if (duplicatedFaces.length > 0) {
    console.warn(`批量上传覆盖了已有面：${duplicatedFaces.join('、')}`)
  }
}

const assignFaceFile = async (key, file, showSuccess = true) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能选择图片文件')
    return
  }

  try {
    const image = await loadImage(file)
    faces[key] = {
      file,
      image,
      preview: URL.createObjectURL(file)
    }
    previewUrl.value = ''
    outputBlob.value = null
    if (showSuccess) {
      ElMessage.success(`${faceLabel(key)} 已选择`)
    }
  } catch {
    ElMessage.error('图片读取失败')
  }
}

const matchFaceByName = (fileName) => {
  const baseName = fileName.replace(/\.[^.]+$/, '').trim().toLowerCase()
  const nameParts = baseName.split(/[\s._-]+/).filter(Boolean)
  return faceList.find((face) => {
    return parseRuleTokens(nameRules[face.key]).some((token) => {
      return token === baseName || nameParts.includes(token)
    })
  })?.key
}

const parseRuleTokens = (rule) => {
  return String(rule || '')
    .split(',')
    .map((item) => item.trim().toLowerCase())
    .filter(Boolean)
}

const faceLabel = (key) => faceList.find((face) => face.key === key)?.label || key

const resetRules = () => {
  Object.keys(defaultNameRules).forEach((key) => {
    nameRules[key] = defaultNameRules[key]
  })
}

const loadImage = (file) => {
  return new Promise((resolve, reject) => {
    const image = new Image()
    image.onload = () => resolve(image)
    image.onerror = reject
    image.src = URL.createObjectURL(file)
  })
}

const ensureAllFaces = () => {
  const missing = faceList.filter((face) => !faces[face.key]?.image)
  if (missing.length > 0) {
    ElMessage.error(`请补齐六面图：${missing.map((item) => item.label).join('、')}`)
    return false
  }
  return true
}

const buildFaceSources = () => {
  const sources = {}
  faceList.forEach((face) => {
    const image = faces[face.key].image
    const canvas = document.createElement('canvas')
    canvas.width = image.naturalWidth || image.width
    canvas.height = image.naturalHeight || image.height
    const context = canvas.getContext('2d')
    context.drawImage(image, 0, 0, canvas.width, canvas.height)
    sources[face.key] = {
      width: canvas.width,
      height: canvas.height,
      data: context.getImageData(0, 0, canvas.width, canvas.height).data
    }
  })
  return sources
}

const pickFace = (x, y, z) => {
  const ax = Math.abs(x)
  const ay = Math.abs(y)
  const az = Math.abs(z)
  let face
  let u
  let v

  if (ax >= ay && ax >= az) {
    if (x > 0) {
      face = 'px'
      u = -z / ax
      v = -y / ax
    } else {
      face = 'nx'
      u = z / ax
      v = -y / ax
    }
  } else if (ay >= ax && ay >= az) {
    if (y > 0) {
      face = 'py'
      u = x / ay
      v = z / ay
    } else {
      face = 'ny'
      u = x / ay
      v = -z / ay
    }
  } else if (z > 0) {
    face = 'pz'
    u = x / az
    v = -y / az
  } else {
    face = 'nz'
    u = -x / az
    v = -y / az
  }

  return {
    face,
    u: (u + 1) / 2,
    v: (v + 1) / 2
  }
}

const sampleFace = (source, u, v) => {
  const x = Math.max(0, Math.min(source.width - 1, Math.floor(u * source.width)))
  const y = Math.max(0, Math.min(source.height - 1, Math.floor(v * source.height)))
  const index = (y * source.width + x) * 4
  return [
    source.data[index],
    source.data[index + 1],
    source.data[index + 2],
    source.data[index + 3]
  ]
}

const generatePanorama = async () => {
  if (!ensureAllFaces()) return

  generating.value = true
  previewUrl.value = ''
  outputBlob.value = null

  try {
    const width = outputWidth.value
    const height = Math.floor(width / 2)
    const sources = buildFaceSources()
    const canvas = document.createElement('canvas')
    canvas.width = width
    canvas.height = height
    const context = canvas.getContext('2d')
    const imageData = context.createImageData(width, height)
    const data = imageData.data

    for (let py = 0; py < height; py++) {
      const lat = Math.PI * (0.5 - py / height)
      const sinLat = Math.sin(lat)
      const cosLat = Math.cos(lat)

      for (let px = 0; px < width; px++) {
        const lon = 2 * Math.PI * (px / width - 0.5)
        const directionX = cosLat * Math.sin(lon)
        const directionY = sinLat
        const directionZ = cosLat * Math.cos(lon)
        const picked = pickFace(directionX, directionY, directionZ)
        const color = sampleFace(sources[picked.face], picked.u, picked.v)
        const index = (py * width + px) * 4
        data[index] = color[0]
        data[index + 1] = color[1]
        data[index + 2] = color[2]
        data[index + 3] = color[3]
      }
    }

    context.putImageData(imageData, 0, 0)
    const blob = await new Promise((resolve) => canvas.toBlob(resolve, 'image/png', 0.95))
    if (!blob) {
      throw new Error('Canvas output failed')
    }
    outputBlob.value = blob
    previewUrl.value = URL.createObjectURL(blob)
    ElMessage.success('全景图合成成功')
  } catch (error) {
    console.error(error)
    ElMessage.error('全景图合成失败')
  } finally {
    generating.value = false
  }
}

const downloadPanorama = () => {
  if (!outputBlob.value) {
    ElMessage.warning('请先合成全景图')
    return
  }
  const link = document.createElement('a')
  link.href = previewUrl.value
  link.download = `vr-panorama-${Date.now()}.png`
  link.click()
}

const uploadPanorama = async () => {
  if (!outputBlob.value) {
    ElMessage.warning('请先合成全景图')
    return
  }
  uploading.value = true
  try {
    const file = new File([outputBlob.value], `vr-panorama-${Date.now()}.png`, { type: 'image/png' })
    const response = await uploadImageApi(file, 'vr')
    const url = response?.data?.data?.url
    if (!url) {
      throw new Error(response?.data?.message || '上传失败')
    }
    emit('uploaded', url)
    ElMessage.success('上传成功，已回填全景图')
  } catch (error) {
    ElMessage.error(error.message || '上传失败')
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.cubemap-tool {
  width: 100%;
}

.batch-panel {
  margin-bottom: 14px;
}

.batch-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 8px;
}

.hidden-input {
  display: none;
}

.batch-tip {
  margin-bottom: 12px;
  font-size: 12px;
  color: #909399;
}

.rule-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 12px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fafafa;
}

.rule-item {
  display: grid;
  grid-template-columns: 58px minmax(0, 1fr);
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.face-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.face-tile {
  min-width: 0;
}

.face-title {
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 600;
}

.face-picker {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 110px;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  color: #909399;
  background: #fafafa;
}

.face-picker input {
  display: none;
}

.face-picker img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.settings-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 14px;
}

.preview-panel {
  margin-top: 14px;
}

.preview-panel img {
  width: 100%;
  max-height: 240px;
  object-fit: contain;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #111;
}

.action-row {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 12px;
}
</style>
