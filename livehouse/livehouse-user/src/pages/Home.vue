<template>
  <div class="home-page">
    <van-swipe :autoplay="3000" indicator-color="white" indicator-active-color="#1890ff">
      <van-swipe-item v-for="item in bannerList" :key="item.id" @click="handleBannerClick(item)">
        <AppImage :src="item.coverImage || item.image" alt="" class="banner-image" />
      </van-swipe-item>
    </van-swipe>

    <div class="quick-entry">
      <van-grid :column-num="4">
        <van-grid-item v-for="item in entryList" :key="item.id" clickable @click="handleEntryClick(item.path)">
          <van-icon :name="item.icon" class="entry-icon" />
          <span class="entry-text">{{ item.text }}</span>
        </van-grid-item>
      </van-grid>
    </div>

    <van-card title="热门楼盘" class="section-card">
      <div class="house-list">
        <van-cell v-for="house in hotHouses" :key="house.id" clickable @click="goHouseDetail(house.id)">
          <template #icon>
            <AppImage :src="house.coverImage" alt="" class="house-cover" />
          </template>
          <template #title>
            <span class="house-name">{{ house.buildingName }}</span>
          </template>
          <template #label>
            <span class="house-price">{{ house.avgPrice }}元/㎡</span>
          </template>
          <template #extra>
            <span class="house-tag">{{ house.tag || '推荐' }}</span>
          </template>
        </van-cell>
      </div>
    </van-card>

    <van-card title="直播预告" class="section-card">
      <div class="live-list">
        <van-cell v-for="live in liveList" :key="live.id" clickable @click="goLiveDetail(live.id)">
          <template #icon>
            <div class="live-cover-wrap">
              <AppImage :src="live.coverImage" alt="" class="live-cover" />
              <span class="live-status">{{ live.status === 1 ? '直播中' : '预告' }}</span>
            </div>
          </template>
          <template #title>
            <span class="live-name">{{ live.title }}</span>
          </template>
          <template #label>
            <span class="live-time">{{ formatTime(live.startTime) }}</span>
          </template>
          <template #extra>
            <van-icon name="play-circle-o" class="play-icon" />
          </template>
        </van-cell>
      </div>
    </van-card>

    <van-card title="今日预约" class="section-card">
      <div class="stats-row">
        <div class="stat-item">
          <span class="stat-value">{{ stats.totalHouse || 0 }}</span>
          <span class="stat-label">楼盘总数</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">{{ stats.totalLive || 0 }}</span>
          <span class="stat-label">直播中</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">{{ visitRate }}%</span>
          <span class="stat-label">到访率</span>
        </div>
      </div>
    </van-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHomeIndexApi, getHomeStatsApi } from '@/api/home.js'
import AppImage from '@/components/AppImage.vue'
const router = useRouter()

const bannerList = ref([])
const entryList = ref([
  { id: 1, icon: 'home-o', text: '楼盘', path: '/house/list' },
  { id: 2, icon: 'video-o', text: '直播', path: '/live/list' },
  { id: 3, icon: 'calendar-o', text: '预约', path: '/reserve' },
  { id: 4, icon: 'user-o', text: '我的', path: '/profile' }
])
const hotHouses = ref([])
const liveList = ref([])
const stats = ref({ totalHouse: 0, totalLive: 0 })
const visitRate = ref(78)

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const target = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  
  const diff = Math.floor((target - today) / (1000 * 60 * 60 * 24))
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  
  if (diff === 0) return `今天 ${hours}:${minutes}`
  if (diff === 1) return `明天 ${hours}:${minutes}`
  return `${date.getMonth() + 1}/${date.getDate()} ${hours}:${minutes}`
}

const goHouseDetail = (id) => {
  router.push(`/house/detail/${id}`)
}

const goLiveDetail = (id) => {
  router.push(`/live/detail/${id}`)
}

const handleEntryClick = (path) => {
  router.push(path)
}

const handleBannerClick = (item) => {
  if (!item.linkType || !item.linkId) return
  const paths = {
    house: `/house/detail/${item.linkId}`,
    live: `/live/detail/${item.linkId}`,
    vr: `/vr/view/${item.linkId}`
  }
  const path = paths[item.linkType]
  if (path) router.push(path)
}

const fetchHomeData = async () => {
  try {
    const indexRes = await getHomeIndexApi()
    if (indexRes.data && indexRes.data.data) {
      const data = indexRes.data.data
      bannerList.value = data.banners || []
      hotHouses.value = data.hotHouses || []
      liveList.value = [...(data.livingLives || []), ...(data.upcomingLives || [])]
    }
    
    const statsRes = await getHomeStatsApi()
    if (statsRes.data && statsRes.data.data) {
      stats.value = statsRes.data.data
    }
  } catch (error) {
    console.error('获取首页数据失败:', error)
  }
}

onMounted(() => {
  fetchHomeData()
})
</script>

<style>
.home-page {
  padding-bottom: 60px;
}

.banner-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.quick-entry {
  background: #fff;
  padding: 15px 0;
}

.entry-icon {
  font-size: 32px;
  color: #1890ff;
}

.entry-text {
  font-size: 12px;
  color: #666;
}

.section-card {
  margin: 10px;
  border-radius: 12px;
}

.house-cover {
  width: 80px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
}

.house-name {
  font-size: 14px;
  font-weight: 600;
}

.house-price {
  font-size: 12px;
  color: #ff4d4f;
}

.house-tag {
  font-size: 10px;
  color: #fff;
  background: #1890ff;
  padding: 2px 6px;
  border-radius: 4px;
}

.live-cover-wrap {
  position: relative;
}

.live-cover {
  width: 80px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
}

.live-status {
  position: absolute;
  bottom: 2px;
  left: 2px;
  font-size: 10px;
  color: #fff;
  background: rgba(0, 0, 0, 0.6);
  padding: 1px 4px;
  border-radius: 4px;
}

.live-name {
  font-size: 14px;
  font-weight: 600;
}

.live-time {
  font-size: 12px;
  color: #999;
}

.play-icon {
  font-size: 20px;
  color: #1890ff;
}

.stats-row {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 20px 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
}

.stat-label {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: #eee;
}
</style>
