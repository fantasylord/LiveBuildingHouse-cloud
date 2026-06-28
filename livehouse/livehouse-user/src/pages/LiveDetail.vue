<template>
  <div class="live-detail">
    <van-nav-bar title="直播详情" left-arrow @click-left="goBack">
      <template #right>
        <van-icon :name="favorited ? 'star' : 'star-o'" size="22" :color="favorited ? '#ee0a24' : '#333'" @click="toggleFavorite" />
      </template>
    </van-nav-bar>

    <div class="player-wrap">
      <video
        v-if="mediaUrl && canNativePlay && accessGranted"
        class="live-video"
        :src="mediaUrl"
        controls
        playsinline
        autoplay
        @play="recordView"
      />
      <div v-else class="player-placeholder">
        <van-icon name="play-circle-o" size="72" color="#1989fa" />
        <p>{{ playerTip }}</p>
      </div>

      <div v-if="accessGranted" class="danmu-layer">
        <div v-for="item in floatingDanmu" :key="item.id" class="danmu-float">
          {{ item.nickname || '游客' }}：{{ item.content }}
        </div>
      </div>
    </div>

    <div class="interaction-bar">
      <button class="like-btn" :class="{ liked }" @click="handleLike">
        <van-icon :name="liked ? 'like' : 'like-o'" />
        <span>{{ likeCount }}</span>
      </button>
      <van-field
        v-model="danmuContent"
        class="danmu-input"
        placeholder="发条弹幕互动一下"
        maxlength="80"
        clearable
        @keyup.enter="handleDanmu"
      />
      <van-button size="small" type="primary" @click="handleDanmu">发送</van-button>
    </div>

    <div class="danmu-list">
      <div class="section-title">实时弹幕</div>
      <div v-if="danmuList.length" class="danmu-items">
        <div v-for="item in danmuList" :key="item.id" class="danmu-item">
          <span>{{ item.nickname || '游客' }}</span>
          <p>{{ item.content }}</p>
        </div>
      </div>
      <van-empty v-else image-size="72" description="暂无弹幕" />
    </div>

    <div class="live-info">
      <div class="live-title">{{ live.sessionName || '直播详情' }}</div>
      <div class="live-meta">
        <span>主播：{{ live.anchorName || '-' }}</span>
        <span>观看：{{ live.totalViewer || 0 }}</span>
      </div>
      <p class="intro">{{ live.introduction || '暂无直播简介' }}</p>
    </div>

    <van-cell-group v-if="live.needAccess && !accessGranted" inset title="私密直播访问">
      <van-field v-model="accessForm.phone" label="手机号" placeholder="请输入白名单手机号" />
      <van-field v-model="accessForm.password" label="密码" type="password" placeholder="请输入观看密码" />
      <div class="form-actions">
        <van-button block type="primary" @click="handleAccess">验证并观看</van-button>
      </div>
    </van-cell-group>

    <van-cell-group inset title="预约看房">
      <van-field v-model="reserveForm.customerName" label="姓名" placeholder="请输入姓名" />
      <van-field v-model="reserveForm.phone" label="手机号" placeholder="请输入手机号" />
      <van-field v-model="reserveForm.remark" label="备注" placeholder="意向户型、到访时间等" />
      <div class="form-actions">
        <van-button block type="success" @click="handleReserve">提交预约</van-button>
      </div>
    </van-cell-group>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import {
  checkLiveAccessApi,
  getLiveDetailApi,
  getLiveInteractionApi,
  recordLiveViewApi,
  reserveLiveApi,
  sendLiveDanmuApi,
  toggleLiveLikeApi
} from '@/api/live.js'
import { checkFavoriteApi, toggleFavoriteApi } from '@/api/favorite.js'
import { recordHistoryApi } from '@/api/history.js'
import { getUserInfoApi } from '@/api/user.js'

const route = useRoute()
const router = useRouter()
const live = reactive({})
const accessGranted = ref(false)
const hasRecordedView = ref(false)
const favorited = ref(false)
const liked = ref(false)
const likeCount = ref(0)
const danmuList = ref([])
const floatingDanmu = ref([])
const danmuContent = ref('')
const pollTimer = ref(null)

const getClientId = () => {
  const key = 'livehouse_live_client_id'
  let value = localStorage.getItem(key)
  if (!value) {
    value = `guest_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`
    localStorage.setItem(key, value)
  }
  return value
}

const clientId = ref(getClientId())

const accessForm = reactive({ phone: '', password: '' })
const reserveForm = reactive({ customerName: '', phone: '', remark: '' })

const mediaUrl = computed(() => {
  if (live.status === 2) {
    return live.replayUrl
  }
  return live.playUrl || live.hlsUrl
})

const canNativePlay = computed(() => {
  if (!mediaUrl.value) return false
  return /\.(m3u8|mp4|flv)(\?|$)/i.test(mediaUrl.value)
})

const playerTip = computed(() => {
  if (live.needAccess && !accessGranted.value) return '请先完成私密直播验证'
  if (!mediaUrl.value) return live.status === 0 ? '直播尚未开始' : '暂无可播放地址'
  if (!canNativePlay.value) {
    if (mediaUrl.value.includes('.flv')) return 'FLV格式需要专用播放器，建议配置HLS格式地址'
    if (mediaUrl.value.startsWith('rtmp://')) return '浏览器不支持RTMP协议，请配置HLS或FLV播放地址'
    return '当前地址不是浏览器原生格式，请配置 HLS、FLV 或 MP4 播放地址'
  }
  if (live.status === 1 && mediaUrl.value.includes('livehouse.cn')) {
    return '当前为测试环境，需配置真实腾讯云直播地址才能观看'
  }
  return '播放器加载中'
})

onMounted(async () => {
  await loadDetail()
  await prefillUser()
  await loadInteraction()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})

const loadDetail = async () => {
  try {
    const response = await getLiveDetailApi(route.params.id)
    Object.assign(live, response.data.data || {})
    accessGranted.value = !live.needAccess
    await Promise.all([recordHistory(), fetchFavorite()])
  } catch (error) {
    showToast('获取直播详情失败')
  }
}

const loadInteraction = async () => {
  if (!accessGranted.value) return
  const sinceId = danmuList.value.length ? danmuList.value[danmuList.value.length - 1].id : undefined
  try {
    const response = await getLiveInteractionApi(route.params.id, {
      sinceId,
      limit: 30,
      clientId: clientId.value
    })
    const data = response.data?.data || {}
    likeCount.value = Number(data.likeCount || 0)
    liked.value = !!data.liked
    const incoming = data.danmuList || []
    if (incoming.length) {
      danmuList.value = [...danmuList.value, ...incoming].slice(-60)
      showFloatingDanmu(incoming)
    }
  } catch {}
}

const startPolling = () => {
  stopPolling()
  pollTimer.value = window.setInterval(() => {
    if (live.status === 1 && accessGranted.value) {
      loadInteraction()
    }
  }, 3000)
}

const stopPolling = () => {
  if (pollTimer.value) {
    window.clearInterval(pollTimer.value)
    pollTimer.value = null
  }
}

const showFloatingDanmu = (items) => {
  floatingDanmu.value = items.slice(-4)
  window.setTimeout(() => {
    floatingDanmu.value = []
  }, 2800)
}

const handleLike = async () => {
  try {
    const response = await toggleLiveLikeApi(route.params.id, {
      clientId: clientId.value,
      nickname: reserveForm.customerName || '游客'
    })
    const data = response.data?.data || {}
    liked.value = !!data.liked
    likeCount.value = Number(data.likeCount || 0)
  } catch (error) {
    showToast(error.response?.data?.message || '点赞失败')
  }
}

const handleDanmu = async () => {
  const content = danmuContent.value.trim()
  if (!content) {
    showToast('请输入弹幕内容')
    return
  }
  try {
    const response = await sendLiveDanmuApi(route.params.id, {
      clientId: clientId.value,
      nickname: reserveForm.customerName || '游客',
      content
    })
    danmuContent.value = ''
    const danmu = response.data?.data
    if (danmu?.id) {
      danmuList.value = [...danmuList.value, danmu].slice(-60)
      showFloatingDanmu([danmu])
    }
  } catch (error) {
    showToast(error.response?.data?.message || '弹幕发送失败')
  }
}

const handleAccess = async () => {
  if (!accessForm.phone && !accessForm.password) {
    showToast('请输入手机号或观看密码')
    return
  }
  try {
    const response = await checkLiveAccessApi(route.params.id, accessForm)
    Object.assign(live, response.data.data || {})
    accessGranted.value = true
    showToast('验证通过')
    await loadInteraction()
  } catch (error) {
    showToast(error.response?.data?.message || '验证失败')
  }
}

const recordView = async () => {
  if (hasRecordedView.value) return
  hasRecordedView.value = true
  try {
    await recordLiveViewApi(route.params.id)
  } catch (error) {
    hasRecordedView.value = false
  }
}

const handleReserve = async () => {
  if (!reserveForm.customerName || !reserveForm.phone) {
    showToast('请填写姓名和手机号')
    return
  }
  try {
    await reserveLiveApi(route.params.id, {
      ...reserveForm,
      buildingId: live.buildingId,
      unitId: live.unitId
    })
    showToast('预约提交成功')
    reserveForm.remark = ''
  } catch (error) {
    showToast('预约提交失败')
  }
}

const favoritePayload = () => ({
  targetType: 'live',
  targetId: Number(route.params.id),
  targetTitle: live.sessionName,
  targetCover: live.coverImage,
  targetDesc: live.introduction
})

const recordHistory = async () => {
  if (!localStorage.getItem('token')) return
  try {
    await recordHistoryApi(favoritePayload())
  } catch {}
}

const fetchFavorite = async () => {
  if (!localStorage.getItem('token')) return
  try {
    const response = await checkFavoriteApi({ targetType: 'live', targetId: route.params.id })
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

const prefillUser = async () => {
  if (!localStorage.getItem('token')) return
  try {
    const response = await getUserInfoApi()
    const user = response.data?.data || {}
    reserveForm.customerName = user.realName || ''
    reserveForm.phone = user.phone || ''
    accessForm.phone = user.phone || ''
  } catch {}
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.live-detail {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 24px;
}

.player-wrap {
  position: relative;
  background: #000;
  min-height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.live-video {
  width: 100%;
  min-height: 220px;
  background: #000;
}

.player-placeholder {
  text-align: center;
  color: #bbb;
}

.player-placeholder p {
  margin-top: 10px;
  padding: 0 20px;
}

.danmu-layer {
  pointer-events: none;
  position: absolute;
  left: 0;
  right: 0;
  top: 16px;
  height: 120px;
  overflow: hidden;
}

.danmu-float {
  display: inline-block;
  max-width: 86%;
  margin: 6px 0;
  padding: 5px 10px;
  border-radius: 16px;
  color: #fff;
  background: rgba(0, 0, 0, 0.42);
  white-space: nowrap;
  animation: danmu-move 2.8s linear forwards;
}

@keyframes danmu-move {
  from {
    transform: translateX(100vw);
  }
  to {
    transform: translateX(-100%);
  }
}

.interaction-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.like-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 34px;
  padding: 0 10px;
  border: 1px solid #e8e8e8;
  border-radius: 18px;
  background: #fff;
  color: #666;
}

.like-btn.liked {
  color: #ee0a24;
  border-color: #ffd6de;
  background: #fff5f7;
}

.danmu-input {
  flex: 1;
  padding: 0;
  background: #f7f8fa;
  border-radius: 18px;
}

.danmu-list {
  background: #fff;
  margin-bottom: 10px;
  padding: 12px 15px;
}

.section-title {
  font-weight: 600;
  margin-bottom: 10px;
}

.danmu-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 180px;
  overflow-y: auto;
}

.danmu-item {
  display: flex;
  gap: 8px;
  font-size: 14px;
  line-height: 1.5;
}

.danmu-item span {
  color: #1989fa;
  flex-shrink: 0;
}

.danmu-item p {
  margin: 0;
  color: #333;
}

.live-info {
  background: #fff;
  padding: 15px;
  margin-bottom: 10px;
}

.live-title {
  font-size: 17px;
  font-weight: bold;
  margin-bottom: 10px;
}

.live-meta {
  color: #666;
  font-size: 14px;
  display: flex;
  gap: 18px;
}

.intro {
  color: #555;
  font-size: 14px;
  line-height: 1.6;
  margin: 12px 0 0;
}

.form-actions {
  padding: 12px 16px 16px;
}
</style>
