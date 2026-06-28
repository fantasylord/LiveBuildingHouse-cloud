<template>
  <div class="spherical-preview" ref="rootRef" @click="handleClick">
    <div ref="canvasRef" class="spherical-canvas" @mousedown="handleCanvasMouseDown"></div>

    <div
      v-for="(marker, index) in screenMarkers"
      :key="`marker-${index}`"
      class="spherical-marker"
      :style="{ left: marker.x + 'px', top: marker.y + 'px' }"
      @mousedown.stop="(e) => startDrag(e, index)"
      @click.stop="handleMarkerClick(index)"
    >
      <el-icon class="spherical-marker-icon"><Location /></el-icon>
      <span class="spherical-marker-index">{{ index + 1 }}</span>
    </div>

    <div v-if="loading" class="spherical-state">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载全景中</span>
    </div>

    <div v-else-if="errorText" class="spherical-state">
      <el-icon><Warning /></el-icon>
      <span>{{ errorText }}</span>
    </div>

    <div class="spherical-tip">点击球面添加热点（与H5端坐标一致）</div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as THREE from 'three'
import { Location, Loading, Warning } from '@element-plus/icons-vue'
import { getFullImageUrl } from '@/utils/imageUrl.js'

const props = defineProps({
  imageUrl: {
    type: String,
    default: ''
  },
  hotspots: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['add', 'pick', 'markerClick'])

const rootRef = ref(null)
const canvasRef = ref(null)
const loading = ref(false)
const errorText = ref('')
const screenMarkers = ref([])

const internal = {
  scene: null,
  camera: null,
  renderer: null,
  mesh: null,
  animationId: 0,
  lon: 0,
  lat: 0,
  isDragging: false,
  pointerStartX: 0,
  pointerStartY: 0,
  pointerStartLon: 0,
  pointerStartLat: 0,
  pointerMoved: false,
  resizeObserver: null
}

const CAMERA_FOV = 75
const SPHERE_RADIUS = 500

const disposeMesh = () => {
  if (!internal.mesh || !internal.scene) return
  internal.scene.remove(internal.mesh)
  internal.mesh.geometry.dispose()
  if (internal.mesh.material.map) internal.mesh.material.map.dispose()
  internal.mesh.material.dispose()
  internal.mesh = null
}

const destroyRenderer = () => {
  if (internal.animationId) {
    cancelAnimationFrame(internal.animationId)
    internal.animationId = 0
  }
  disposeMesh()
  if (internal.renderer) {
    internal.renderer.dispose()
    internal.renderer.domElement?.remove()
    internal.renderer = null
  }
  if (internal.resizeObserver) {
    internal.resizeObserver.disconnect()
    internal.resizeObserver = null
  }
  internal.scene = null
  internal.camera = null
}

const initScene = () => {
  if (!canvasRef.value || !rootRef.value || internal.renderer) return
  const width = rootRef.value.clientWidth
  const height = rootRef.value.clientHeight

  internal.scene = new THREE.Scene()
  internal.camera = new THREE.PerspectiveCamera(CAMERA_FOV, width / height, 1, 1100)
  internal.camera.target = new THREE.Vector3()

  internal.renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  internal.renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
  internal.renderer.setSize(width, height)
  canvasRef.value.appendChild(internal.renderer.domElement)

  internal.resizeObserver = new ResizeObserver(() => resize())
  internal.resizeObserver.observe(rootRef.value)

  animate()
}

const animate = () => {
  updateCamera()
  updateMarkers()
  if (internal.renderer && internal.scene && internal.camera) {
    internal.renderer.render(internal.scene, internal.camera)
  }
  internal.animationId = requestAnimationFrame(animate)
}

const updateCamera = () => {
  if (!internal.camera) return
  const lat = Math.max(-80, Math.min(80, internal.lat))
  const phi = THREE.MathUtils.degToRad(90 - lat)
  const theta = THREE.MathUtils.degToRad(internal.lon)
  internal.camera.target.set(
    SPHERE_RADIUS * Math.sin(phi) * Math.cos(theta),
    SPHERE_RADIUS * Math.cos(phi),
    SPHERE_RADIUS * Math.sin(phi) * Math.sin(theta)
  )
  internal.camera.lookAt(internal.camera.target)
}

const hotspotToVector = (hotspot) => {
  const x = Number.isFinite(Number(hotspot.x)) ? Number(hotspot.x) : 50
  const y = Number.isFinite(Number(hotspot.y)) ? Number(hotspot.y) : 50
  const phi = THREE.MathUtils.degToRad((y / 100) * 180)
  const theta = THREE.MathUtils.degToRad((x / 100) * 360 - 180)
  return new THREE.Vector3(
    SPHERE_RADIUS * Math.sin(phi) * Math.cos(theta),
    SPHERE_RADIUS * Math.cos(phi),
    SPHERE_RADIUS * Math.sin(phi) * Math.sin(theta)
  )
}

const updateMarkers = () => {
  if (!internal.camera || !rootRef.value) {
    screenMarkers.value = []
    return
  }
  const width = rootRef.value.clientWidth
  const height = rootRef.value.clientHeight
  const cameraDirection = new THREE.Vector3()
  internal.camera.getWorldDirection(cameraDirection)
  const next = []

  props.hotspots.forEach((hotspot) => {
    const world = hotspotToVector(hotspot)
    if (world.clone().normalize().dot(cameraDirection) <= 0) return
    const projected = world.clone().project(internal.camera)
    if (projected.z < -1 || projected.z > 1) return

    const sx = (projected.x + 1) * width / 2
    const sy = (-projected.y + 1) * height / 2
    const pad = 60
    if (sx < -pad || sx > width + pad || sy < -pad || sy > height + pad) return

    next.push({ x: sx, y: sy })
  })

  screenMarkers.value = next
}

const resize = () => {
  if (!rootRef.value || !internal.renderer || !internal.camera) return
  const width = rootRef.value.clientWidth
  const height = rootRef.value.clientHeight
  internal.camera.aspect = width / height
  internal.camera.updateProjectionMatrix()
  internal.renderer.setSize(width, height)
}

const loadImage = (url) => {
  if (!url) {
    errorText.value = '暂无全景图'
    loading.value = false
    return
  }
  loading.value = true
  errorText.value = ''

  if (!internal.renderer) {
    initScene()
  }
  if (!internal.scene) return

  const loader = new THREE.TextureLoader()
  loader.setCrossOrigin('anonymous')
  loader.load(
    getFullImageUrl(url),
    (texture) => {
      texture.colorSpace = THREE.SRGBColorSpace
      texture.minFilter = THREE.LinearFilter
      disposeMesh()
      const geometry = new THREE.SphereGeometry(SPHERE_RADIUS, 64, 40)
      geometry.scale(-1, 1, 1)
      const material = new THREE.MeshBasicMaterial({ map: texture })
      internal.mesh = new THREE.Mesh(geometry, material)
      internal.scene.add(internal.mesh)
      loading.value = false
    },
    undefined,
    () => {
      loading.value = false
      errorText.value = '全景图加载失败'
    }
  )
}

const screenToPercent = (clientX, clientY) => {
  if (!rootRef.value || !internal.camera || !internal.mesh) return null
  const rect = rootRef.value.getBoundingClientRect()
  const ndcX = ((clientX - rect.left) / rect.width) * 2 - 1
  const ndcY = -(((clientY - rect.top) / rect.height) * 2 - 1)
  const raycaster = new THREE.Raycaster()
  raycaster.setFromCamera(new THREE.Vector2(ndcX, ndcY), internal.camera)
  const intersects = raycaster.intersectObject(internal.mesh, false)
  const world = intersects[0]?.point || raycaster.ray.direction.clone().multiplyScalar(SPHERE_RADIUS)
  const normal = world.clone().normalize()

  // 与 H5 端 hotspotToVector 保持完全一致的逆运算
  const theta = Math.atan2(normal.z, normal.x)
  const phi = Math.acos(Math.max(-1, Math.min(1, normal.y)))

  const x = ((theta + Math.PI) / (2 * Math.PI)) * 100
  const y = (phi / Math.PI) * 100

  return {
    x: Math.max(0, Math.min(100, Math.round(x))),
    y: Math.max(0, Math.min(100, Math.round(y)))
  }
}

const handleClick = (e) => {
  if (internal.isDragging || internal.pointerMoved) {
    internal.pointerMoved = false
    return
  }
  const percent = screenToPercent(e.clientX, e.clientY)
  if (!percent) return
  emit('add', percent)
}

const handleMarkerClick = (index) => {
  emit('markerClick', index)
}

const startDrag = (e, index) => {
  internal.isDragging = true
  internal.pointerStartX = e.clientX
  internal.pointerStartY = e.clientY
  internal.pointerStartLon = internal.lon
  internal.pointerStartLat = internal.lat
  internal.pointerMoved = false
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  e.preventDefault()
}

const handleCanvasMouseDown = (e) => {
  if (e.button !== 0) return
  internal.isDragging = true
  internal.pointerStartX = e.clientX
  internal.pointerStartY = e.clientY
  internal.pointerStartLon = internal.lon
  internal.pointerStartLat = internal.lat
  internal.pointerMoved = false
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  e.preventDefault()
}

const onDrag = (e) => {
  if (!internal.isDragging) return
  const dx = e.clientX - internal.pointerStartX
  const dy = e.clientY - internal.pointerStartY
  if (Math.abs(dx) + Math.abs(dy) > 3) {
    internal.pointerMoved = true
  }
  internal.lon = internal.pointerStartLon - (e.clientX - internal.pointerStartX) * 0.2
  internal.lat = Math.max(-80, Math.min(80, internal.pointerStartLat + (e.clientY - internal.pointerStartY) * 0.2))
}

const stopDrag = () => {
  internal.isDragging = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

watch(() => props.imageUrl, (url) => {
  if (url) {
    loadImage(url)
  } else {
    destroyRenderer()
    errorText.value = ''
    screenMarkers.value = []
  }
}, { immediate: false })

onMounted(() => {
  nextTick(() => {
    if (props.imageUrl) loadImage(props.imageUrl)
  })
})

onBeforeUnmount(() => {
  destroyRenderer()
})
</script>

<style scoped>
.spherical-preview {
  position: relative;
  width: 100%;
  height: 240px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
  background: #111;
  cursor: crosshair;
}

.spherical-canvas {
  position: absolute;
  inset: 0;
  cursor: grab;
}

.spherical-canvas:active {
  cursor: grabbing;
}

.spherical-canvas :deep(canvas) {
  width: 100% !important;
  height: 100% !important;
  display: block;
}

.spherical-marker {
  position: absolute;
  transform: translate(-50%, -50%);
  z-index: 10;
  cursor: move;
}

.spherical-marker-icon {
  font-size: 22px;
  color: #ee0a24;
  background: rgba(255, 255, 255, 0.94);
  border-radius: 50%;
  padding: 3px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
}

.spherical-marker-index {
  position: absolute;
  top: -6px;
  right: -8px;
  font-size: 10px;
  color: #fff;
  background: #ee0a24;
  border-radius: 50%;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.spherical-state {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 13px;
  background: rgba(0, 0, 0, 0.45);
  z-index: 5;
}

.spherical-state .el-icon {
  font-size: 26px;
}

.spherical-tip {
  position: absolute;
  left: 8px;
  bottom: 6px;
  padding: 2px 8px;
  font-size: 11px;
  color: #fff;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 10px;
  z-index: 6;
  pointer-events: none;
}
</style>
