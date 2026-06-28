<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="avatar">
        <van-icon name="user-o" size="44" color="#fff" />
      </div>
      <div class="user-info">
        <div class="user-name">{{ userInfo.realName || userInfo.username || '未登录用户' }}</div>
        <div class="user-phone">{{ formatPhone(userInfo.phone) || '登录后查看完整服务' }}</div>
      </div>
      <van-icon name="setting-o" size="22" color="#fff" @click="navigateTo('/settings')" />
    </div>

    <div class="summary">
      <div class="summary-item" @click="navigateTo('/my-reserve')">
        <strong>{{ reserveCount }}</strong>
        <span>预约</span>
      </div>
      <div class="summary-item" @click="navigateTo('/favorite')">
        <strong>{{ favoriteCount }}</strong>
        <span>收藏</span>
      </div>
      <div class="summary-item" @click="navigateTo('/history')">
        <strong>{{ historyCount }}</strong>
        <span>浏览</span>
      </div>
      <div class="summary-item" @click="navigateTo('/message')">
        <strong>{{ messageCount }}<span v-if="unreadCount > 0" class="unread-dot"></span></strong>
        <span>消息</span>
      </div>
    </div>

    <van-cell-group inset>
      <van-cell icon="calendar-o" title="我的预约" is-link @click="navigateTo('/my-reserve')" />
      <van-cell icon="clock-o" title="浏览记录" is-link @click="navigateTo('/history')" />
      <van-cell icon="star-o" title="我的收藏" is-link @click="navigateTo('/favorite')" />
      <van-cell icon="message-o" title="消息通知" is-link @click="navigateTo('/message')">
        <template #right-icon>
          <van-badge :content="unreadCount" :show-zero="false" max="99">
            <van-icon name="arrow" />
          </van-badge>
        </template>
      </van-cell>
      <van-cell icon="setting-o" title="设置" is-link @click="navigateTo('/settings')" />
    </van-cell-group>

    <div class="logout-section">
      <van-button type="default" block @click="logout">退出登录</van-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getUserInfoApi, logoutApi } from '@/api/user.js'
import { getMessageListApi, getUnreadCountApi } from '@/api/message.js'
import { getMyReserveListApi } from '@/api/reserve.js'
import { getFavoriteListApi } from '@/api/favorite.js'
import { getHistoryListApi } from '@/api/history.js'

const router = useRouter()
const userInfo = ref({})
const unreadCount = ref(0)
const messageCount = ref(0)
const reserveCount = ref(0)
const favoriteCount = ref(0)
const historyCount = ref(0)

const formatPhone = (phone) => {
  if (!phone || phone.length < 11) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const navigateTo = (path) => {
  router.push(path)
}

const logout = async () => {
  try {
    await logoutApi()
  } finally {
    localStorage.removeItem('token')
    router.push('/login')
  }
}

const requireLogin = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return false
  }
  return true
}

const fetchUserInfo = async () => {
  try {
    const res = await getUserInfoApi()
    userInfo.value = res.data?.data || {}
  } catch (error) {
    localStorage.removeItem('token')
    router.push('/login')
  }
}

const fetchStats = async () => {
  try {
    const [messageRes, unreadRes, reserveRes, favoriteRes, historyRes] = await Promise.all([
      getMessageListApi(1, 1),
      getUnreadCountApi(),
      getMyReserveListApi({ pageNum: 1, pageSize: 1 }),
      getFavoriteListApi({ pageNum: 1, pageSize: 1 }),
      getHistoryListApi({ pageNum: 1, pageSize: 1 })
    ])
    messageCount.value = messageRes.data?.data?.total || 0
    unreadCount.value = unreadRes.data?.data || 0
    reserveCount.value = reserveRes.data?.data?.total || 0
    favoriteCount.value = favoriteRes.data?.data?.total || 0
    historyCount.value = historyRes.data?.data?.total || 0
  } catch (error) {
    showToast('个人中心数据加载失败')
  }
}

onMounted(async () => {
  if (!requireLogin()) return
  await fetchUserInfo()
  await fetchStats()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f6f7f9;
  padding-bottom: 24px;
}

.profile-header {
  background: linear-gradient(135deg, #1f7ae0 0%, #16a06b 100%);
  padding: 30px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar {
  width: 68px;
  height: 68px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  min-width: 0;
  color: #fff;
}

.user-name {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 6px;
}

.user-phone {
  font-size: 14px;
  opacity: 0.85;
}

.summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  background: #fff;
  margin: -12px 12px 12px;
  border-radius: 8px;
  padding: 14px 0;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.06);
}

.summary-item {
  text-align: center;
}

.summary-item strong {
  display: block;
  color: #222;
  font-size: 18px;
  margin-bottom: 4px;
}

.summary-item span {
  color: #666;
  font-size: 12px;
}

.unread-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ee0a24;
  margin-left: 2px;
  vertical-align: middle;
}

.logout-section {
  padding: 20px;
}
</style>
