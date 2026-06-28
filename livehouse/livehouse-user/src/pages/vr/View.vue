<template>
  <div class="vr-view-page">
    <van-nav-bar title="VR全景看房" left-arrow @click-left="goBack">
      <template #right>
        <van-icon
          :name="favorited ? 'star' : 'star-o'"
          size="22"
          :color="favorited ? '#ee0a24' : '#fff'"
          @click="toggleFavorite"
        />
      </template>
    </van-nav-bar>

    <div
      ref="containerRef"
      class="vr-container"
      @pointerdown="handlePointerDown"
      @pointermove="handlePointerMove"
      @pointerup="handlePointerUp"
      @pointercancel="handlePointerUp"
      @pointerleave="handlePointerUp"
    >
      <div ref="canvasRef" class="vr-canvas"></div>

      <div v-if="loading" class="vr-state">
        <van-loading color="#fff" />
        <span>正在加载全景</span>
      </div>

      <div v-else-if="errorText" class="vr-state">
        <van-icon name="warning-o" size="26" />
        <span>{{ errorText }}</span>
      </div>

      <button
        v-for="(hotspot, index) in visibleHotspots"
        :key="`${activeSceneId}-${index}`"
        class="hotspot"
        :class="{ navigable: isSceneHotspot(hotspot.source) }"
        :style="{ left: hotspot.screenX + 'px', top: hotspot.screenY + 'px' }"
        type="button"
        @click.stop="handleHotspotClick(hotspot.source)"
      >
        <span class="hotspot-inner">
          <van-icon :name="isSceneHotspot(hotspot.source) ? 'exchange' : 'location'" />
        </span>
        <span v-if="hotspot.source.title || hotspot.source.name" class="hotspot-label">
          {{ hotspot.source.title || hotspot.source.name }}
        </span>
      </button>

      <div v-if="showMiniMap" class="floor-map">
        <div class="floor-map-header">
          <span>{{ vrInfo.unitName || '户型模型' }}</span>
          <span>{{ currentScene?.name || currentScene?.title || '' }}</span>
        </div>
        <div class="floor-map-body" :class="{ image: !!floorPlanImage }">
          <AppImage v-if="floorPlanImage" :src="floorPlanImage" alt="" class="floor-map-image" />
          <button
            v-for="scene in sceneList"
            :key="scene.id"
            class="scene-dot"
            :class="{ active: scene.id === activeSceneId }"
            :style="getSceneDotStyle(scene)"
            type="button"
            @click.stop="switchScene(scene.id)"
          >
            <span>{{ scene.shortName }}</span>
          </button>
        </div>
      </div>
    </div>

    <div class="vr-controls">
      <van-button icon="arrow-left" @click="rotateLeft" size="large" />
      <van-button icon="arrow" @click="rotateRight" size="large" />
    </div>

    <div class="scene-tabs" v-if="sceneList.length > 1">
      <button
        v-for="scene in sceneList"
        :key="scene.id"
        class="scene-tab"
        :class="{ active: scene.id === activeSceneId }"
        type="button"
        @click="switchScene(scene.id)"
      >
        {{ scene.name }}
      </button>
    </div>

    <div class="vr-info-bar">
      <div class="info-item">
        <van-icon name="camera-o" />
        <span>{{ currentScene?.name || vrInfo.title || 'VR看房' }}</span>
      </div>
    </div>

    <van-popup v-model:show="showHotspotInfo" position="bottom">
      <div class="hotspot-popup">
        <h3>{{ currentHotspot?.title || currentHotspot?.name || '热点信息' }}</h3>
        <p>{{ currentHotspot?.description || currentHotspot?.desc || '暂无说明' }}</p>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import * as THREE from 'three'
import { getVrDetailApi } from '@/api/vr.js'
import { checkFavoriteApi, toggleFavoriteApi } from '@/api/favorite.js'
import { recordHistoryApi } from '@/api/history.js'
import AppImage from '@/components/AppImage.vue'

const BASE_URL = 'http://localhost:8080'
const CAMERA_FOV = 75
const ROTATE_STEP = 18

const router = useRouter()
const route = useRoute()

const containerRef = ref(null)
const canvasRef = ref(null)
const loading = ref(false)
const errorText = ref('')
const showHotspotInfo = ref(false)
const currentHotspot = ref(null)
const favorited = ref(false)
const sceneList = ref([])
const activeSceneId = ref('')
const visibleHotspots = ref([])
const floorPlanImage = ref('')

const vrInfo = ref({
  title: '',
  imageUrl: '',
  thumbUrl: '',
  description: '',
  unitName: ''
})

let scene
let camera
let renderer
let mesh
let animationFrameId = 0
let lon = 0
let lat = 0
let isDragging = false
let pointerStartX = 0
let pointerStartY = 0
let pointerStartLon = 0
let pointerStartLat = 0

const currentScene = computed(() => sceneList.value.find((item) => item.id === activeSceneId.value))
const currentHotspots = computed(() => currentScene.value?.hotspots || [])
const showMiniMap = computed(() => sceneList.value.length > 1 || !!floorPlanImage.value)

const goBack = () => router.back()

const resolveAssetUrl = (url) => {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  return url.startsWith('/') ? `${BASE_URL}${url}` : `${BASE_URL}/${url}`
}

const parseJsonValue = (value, fallback) => {
  if (!value) return fallback
  if (Array.isArray(value) || typeof value === 'object') return value
  try {
    return JSON.parse(value)
  } catch {
    return fallback
  }
}

const parseArray = (value) => {
  const parsed = parseJsonValue(value, [])
  return Array.isArray(parsed) ? parsed : []
}

const clampLat = (value) => Math.max(-80, Math.min(80, value))

const sceneKey = (sceneItem, index) => {
  return String(sceneItem.id || sceneItem.sceneId || sceneItem.code || sceneItem.sceneCode || `scene-${index}`)
}

const sceneName = (sceneItem, index) => {
  return sceneItem.name || sceneItem.title || sceneItem.sceneName || `场景${index + 1}`
}

const normalizeScenes = (data) => {
  const sceneConfig = parseJsonValue(data.scenes, null)
  const rawScenes = Array.isArray(sceneConfig)
    ? sceneConfig
    : Array.isArray(sceneConfig?.scenes)
      ? sceneConfig.scenes
      : []

  floorPlanImage.value = sceneConfig?.floorPlanImage || sceneConfig?.floorPlanUrl || sceneConfig?.modelImage || ''

  if (rawScenes.length === 0) {
    return [{
      id: 'default',
      code: 'default',
      name: data.vrName || data.title || 'VR看房',
      shortName: 'VR',
      panoramaUrl: data.panoramaUrl || data.imageUrl || data.coverImage || '',
      thumbnailUrl: data.thumbnailUrl || data.thumbUrl || '',
      hotspots: [],
      modelX: 50,
      modelY: 50,
      initialLon: 0,
      initialLat: 0
    }]
  }

  return rawScenes.map((item, index) => {
    const name = sceneName(item, index)
    return {
      id: sceneKey(item, index),
      code: item.code || item.sceneCode || sceneKey(item, index),
      name,
      shortName: item.shortName || name.slice(0, 2),
      panoramaUrl: item.panoramaUrl || item.imageUrl || item.url || data.panoramaUrl || '',
      thumbnailUrl: item.thumbnailUrl || item.thumbUrl || item.coverImage || data.thumbnailUrl || '',
      hotspots: parseArray(item.hotspots),
      modelX: Number.isFinite(Number(item.modelX ?? item.x)) ? Number(item.modelX ?? item.x) : 50,
      modelY: Number.isFinite(Number(item.modelY ?? item.y)) ? Number(item.modelY ?? item.y) : 50,
      initialLon: Number(item.initialLon || item.lon || 0),
      initialLat: Number(item.initialLat || item.lat || 0)
    }
  })
}

const hotspotToVector = (hotspot) => {
  const x = Number.isFinite(Number(hotspot.x)) ? Number(hotspot.x) : 50
  const y = Number.isFinite(Number(hotspot.y)) ? Number(hotspot.y) : 50
  const phi = THREE.MathUtils.degToRad((y / 100) * 180)
  const theta = THREE.MathUtils.degToRad((x / 100) * 360 - 180)

  return new THREE.Vector3(
    500 * Math.sin(phi) * Math.cos(theta),
    500 * Math.cos(phi),
    500 * Math.sin(phi) * Math.sin(theta)
  )
}

const updateCamera = () => {
  if (!camera) return
  lat = clampLat(lat)
  const phi = THREE.MathUtils.degToRad(90 - lat)
  const theta = THREE.MathUtils.degToRad(lon)

  camera.target.set(
    500 * Math.sin(phi) * Math.cos(theta),
    500 * Math.cos(phi),
    500 * Math.sin(phi) * Math.sin(theta)
  )
  camera.lookAt(camera.target)
}

const updateHotspots = () => {
  if (!camera || !renderer || !containerRef.value) return
  const width = containerRef.value.clientWidth
  const height = containerRef.value.clientHeight
  const nextHotspots = []

  currentHotspots.value.forEach((hotspot) => {
    const worldPosition = hotspotToVector(hotspot)
    const cameraDirection = new THREE.Vector3()
    camera.getWorldDirection(cameraDirection)

    if (cameraDirection.dot(worldPosition.clone().normalize()) <= 0) return

    const projected = worldPosition.clone().project(camera)
    if (projected.z < -1 || projected.z > 1) return

    const screenX = (projected.x + 1) * width / 2
    const screenY = (-projected.y + 1) * height / 2
    const edgePadding = 52

    if (
      screenX < -edgePadding ||
      screenX > width + edgePadding ||
      screenY < -edgePadding ||
      screenY > height + edgePadding
    ) {
      return
    }

    nextHotspots.push({ source: hotspot, screenX, screenY })
  })

  visibleHotspots.value = nextHotspots
}

const animate = () => {
  updateCamera()
  updateHotspots()
  renderer?.render(scene, camera)
  animationFrameId = window.requestAnimationFrame(animate)
}

const initRenderer = () => {
  if (!canvasRef.value || !containerRef.value || renderer) return

  scene = new THREE.Scene()
  camera = new THREE.PerspectiveCamera(
    CAMERA_FOV,
    containerRef.value.clientWidth / containerRef.value.clientHeight,
    1,
    1100
  )
  camera.target = new THREE.Vector3()

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
  renderer.setSize(containerRef.value.clientWidth, containerRef.value.clientHeight)
  canvasRef.value.appendChild(renderer.domElement)

  animate()
}

const disposeCurrentMesh = () => {
  if (!mesh || !scene) return
  scene.remove(mesh)
  mesh.geometry.dispose()
  mesh.material.map?.dispose()
  mesh.material.dispose()
  mesh = null
}

const loadPanorama = (imageUrl) => {
  if (!imageUrl) {
    errorText.value = '暂无全景图'
    loading.value = false
    disposeCurrentMesh()
    return
  }

  loading.value = true
  errorText.value = ''
  initRenderer()

  const loader = new THREE.TextureLoader()
  loader.setCrossOrigin('anonymous')
  loader.load(
    resolveAssetUrl(imageUrl),
    (texture) => {
      texture.colorSpace = THREE.SRGBColorSpace
      texture.minFilter = THREE.LinearFilter

      disposeCurrentMesh()
      const geometry = new THREE.SphereGeometry(500, 64, 40)
      geometry.scale(-1, 1, 1)
      const material = new THREE.MeshBasicMaterial({ map: texture })
      mesh = new THREE.Mesh(geometry, material)
      scene.add(mesh)

      loading.value = false
      updateHotspots()
    },
    undefined,
    () => {
      loading.value = false
      errorText.value = '全景图加载失败'
    }
  )
}

const switchScene = (sceneId) => {
  const nextScene = sceneList.value.find((item) => item.id === sceneId)
  if (!nextScene || activeSceneId.value === sceneId) return

  activeSceneId.value = sceneId
  lon = nextScene.initialLon || 0
  lat = nextScene.initialLat || 0
  visibleHotspots.value = []
  loadPanorama(nextScene.panoramaUrl)
}

const rotateLeft = () => {
  lon -= ROTATE_STEP
}

const rotateRight = () => {
  lon += ROTATE_STEP
}

const handlePointerDown = (event) => {
  if (!containerRef.value) return
  isDragging = true
  pointerStartX = event.clientX
  pointerStartY = event.clientY
  pointerStartLon = lon
  pointerStartLat = lat
  containerRef.value.setPointerCapture?.(event.pointerId)
}

const handlePointerMove = (event) => {
  if (!isDragging) return
  lon = pointerStartLon - (event.clientX - pointerStartX) * 0.12
  lat = clampLat(pointerStartLat + (event.clientY - pointerStartY) * 0.12)
}

const handlePointerUp = (event) => {
  isDragging = false
  containerRef.value?.releasePointerCapture?.(event.pointerId)
}

const resolveTargetSceneId = (hotspot) => {
  const target = hotspot.targetSceneId || hotspot.targetSceneCode || hotspot.targetScene || hotspot.sceneId || hotspot.sceneCode
  if (!target) return ''
  const targetText = String(target)
  const found = sceneList.value.find((item) => {
    return item.id === targetText || item.code === targetText || item.name === targetText
  })
  return found?.id || ''
}

const isSceneHotspot = (hotspot) => {
  return !!resolveTargetSceneId(hotspot) || !!(hotspot.targetVrId || hotspot.targetId)
}

const handleHotspotClick = (hotspot) => {
  const targetSceneId = resolveTargetSceneId(hotspot)
  if (targetSceneId) {
    switchScene(targetSceneId)
    return
  }

  if (hotspot.targetVrId || hotspot.targetId) {
    router.push(`/vr/view/${hotspot.targetVrId || hotspot.targetId}`)
    return
  }

  currentHotspot.value = hotspot
  showHotspotInfo.value = true
}

const getSceneDotStyle = (sceneItem) => ({
  left: `${Math.max(6, Math.min(94, sceneItem.modelX))}%`,
  top: `${Math.max(10, Math.min(90, sceneItem.modelY))}%`
})

const favoritePayload = () => ({
  targetType: 'vr',
  targetId: Number(route.params.id),
  targetTitle: vrInfo.value.title,
  targetCover: vrInfo.value.thumbUrl || vrInfo.value.imageUrl,
  targetDesc: vrInfo.value.description
})

const fetchVrDetail = async () => {
  loading.value = true
  errorText.value = ''
  showHotspotInfo.value = false
  visibleHotspots.value = []

  try {
    const res = await getVrDetailApi(route.params.id)
    const data = res.data?.data || {}
    const normalizedScenes = normalizeScenes(data)

    vrInfo.value = {
      title: data.vrName || data.title || 'VR看房',
      imageUrl: data.panoramaUrl || data.imageUrl || data.coverImage || '',
      thumbUrl: data.thumbnailUrl || data.thumbUrl || '',
      description: data.narrationText || data.description || '',
      unitName: data.unitName || ''
    }
    sceneList.value = normalizedScenes

    const firstScene = normalizedScenes[0]
    activeSceneId.value = firstScene?.id || ''
    lon = firstScene?.initialLon || 0
    lat = firstScene?.initialLat || 0

    await nextTick()
    loadPanorama(firstScene?.panoramaUrl || '')
    await Promise.all([recordHistory(), fetchFavorite()])
  } catch (error) {
    loading.value = false
    errorText.value = '获取VR详情失败'
    showToast('获取VR详情失败')
  }
}

const recordHistory = async () => {
  if (!localStorage.getItem('token')) return
  try {
    await recordHistoryApi(favoritePayload())
  } catch {}
}

const fetchFavorite = async () => {
  favorited.value = false
  if (!localStorage.getItem('token')) return
  try {
    const response = await checkFavoriteApi({ targetType: 'vr', targetId: route.params.id })
    favorited.value = !!response.data?.data?.favorited
  } catch {}
}

const toggleFavorite = async () => {
  if (!localStorage.getItem('token')) {
    router.push('/login')
    return
  }
  const response = await toggleFavoriteApi(favoritePayload())
  favorited.value = !!response.data?.data?.favorited
  showToast(favorited.value ? '已收藏' : '已取消收藏')
}

const resizeRenderer = () => {
  if (!containerRef.value || !renderer || !camera) return
  const width = containerRef.value.clientWidth
  const height = containerRef.value.clientHeight
  camera.aspect = width / height
  camera.updateProjectionMatrix()
  renderer.setSize(width, height)
  updateHotspots()
}

const destroyRenderer = () => {
  window.cancelAnimationFrame(animationFrameId)
  disposeCurrentMesh()
  renderer?.dispose()
  renderer?.domElement?.remove()
  renderer = null
  camera = null
  scene = null
}

onMounted(() => {
  fetchVrDetail()
  window.addEventListener('resize', resizeRenderer)
})

watch(() => route.params.id, () => {
  fetchVrDetail()
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeRenderer)
  destroyRenderer()
})
</script>

<style scoped>
.vr-view-page {
  min-height: 100vh;
  background: #000;
  position: relative;
  overflow: hidden;
  touch-action: none;
}

.vr-view-page :deep(.van-nav-bar) {
  background: rgba(0, 0, 0, 0.82);
}

.vr-view-page :deep(.van-nav-bar__title),
.vr-view-page :deep(.van-icon-arrow-left) {
  color: #fff;
}

.vr-container {
  width: 100%;
  height: calc(100vh - 120px);
  overflow: hidden;
  position: relative;
  background: #111;
  cursor: grab;
}

.vr-container:active {
  cursor: grabbing;
}

.vr-canvas,
.vr-canvas :deep(canvas) {
  width: 100%;
  height: 100%;
  display: block;
}

.vr-state {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  background: #111;
  z-index: 12;
  font-size: 14px;
}

.hotspot {
  position: absolute;
  border: 0;
  padding: 0;
  background: transparent;
  transform: translate(-50%, -50%);
  z-index: 10;
}

.hotspot-inner {
  width: 42px;
  height: 42px;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1989fa;
  font-size: 23px;
  box-shadow: 0 3px 14px rgba(0, 0, 0, 0.35);
  transition: transform 0.2s;
}

.hotspot.navigable .hotspot-inner {
  color: #07c160;
}

.hotspot:active .hotspot-inner {
  transform: scale(1.14);
}

.hotspot-label {
  position: absolute;
  top: 47px;
  left: 50%;
  max-width: 86px;
  transform: translateX(-50%);
  padding: 3px 7px;
  border-radius: 10px;
  color: #fff;
  background: rgba(0, 0, 0, 0.58);
  font-size: 11px;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.floor-map {
  position: absolute;
  right: 12px;
  top: 12px;
  width: 128px;
  border-radius: 8px;
  overflow: hidden;
  background: rgba(18, 20, 24, 0.76);
  border: 1px solid rgba(255, 255, 255, 0.22);
  z-index: 18;
  backdrop-filter: blur(6px);
}

.floor-map-header {
  display: flex;
  justify-content: space-between;
  gap: 6px;
  padding: 7px 8px;
  color: #fff;
  font-size: 11px;
  line-height: 1;
}

.floor-map-header span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.floor-map-body {
  position: relative;
  height: 92px;
  margin: 0 8px 8px;
  border-radius: 6px;
  overflow: hidden;
  background:
    linear-gradient(90deg, rgba(255,255,255,0.22) 1px, transparent 1px),
    linear-gradient(0deg, rgba(255,255,255,0.22) 1px, transparent 1px),
    rgba(255, 255, 255, 0.12);
  background-size: 33.33% 50%;
}

.floor-map-body.image {
  background: rgba(255, 255, 255, 0.1);
}

.floor-map-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.scene-dot {
  position: absolute;
  width: 26px;
  height: 26px;
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 50%;
  color: #fff;
  background: rgba(25, 137, 250, 0.92);
  transform: translate(-50%, -50%);
  padding: 0;
  font-size: 10px;
  line-height: 1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.36);
}

.scene-dot.active {
  background: #07c160;
}

.scene-dot span {
  display: block;
  transform: scale(0.9);
}

.vr-controls {
  position: absolute;
  bottom: 92px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 56px;
  z-index: 20;
}

.vr-controls .van-button {
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  padding: 0;
  border: 0;
}

.scene-tabs {
  position: absolute;
  left: 12px;
  right: 12px;
  bottom: 78px;
  z-index: 19;
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 2px;
}

.scene-tab {
  flex: 0 0 auto;
  max-width: 96px;
  height: 30px;
  padding: 0 12px;
  border: 0;
  border-radius: 15px;
  color: #fff;
  background: rgba(0, 0, 0, 0.5);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.scene-tab.active {
  background: #1989fa;
}

.vr-info-bar {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  padding: 10px 15px;
  background: rgba(0, 0, 0, 0.6);
  z-index: 20;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  font-size: 14px;
}

.info-item span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hotspot-popup {
  padding: 20px;
}

.hotspot-popup h3 {
  font-size: 18px;
  font-weight: bold;
  margin: 0 0 10px;
}

.hotspot-popup p {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0;
}
</style>
