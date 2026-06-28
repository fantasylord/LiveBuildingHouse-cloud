<template>
  <div class="message-page">
    <van-nav-bar title="消息通知" left-arrow @click-left="router.back()" />

    <van-tabs v-model:active="activeTab" sticky @change="reloadMessages">
      <van-tab title="全部" />
      <van-tab title="未读" />
      <van-tab title="已读" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="reloadMessages">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadMessages"
      >
        <van-cell-group v-if="messages.length">
          <van-cell
            v-for="item in messages"
            :key="item.id"
            :title="item.title"
            :label="item.content"
            is-link
            @click="openMessage(item)"
          >
            <template #icon>
              <span class="unread-dot" :class="{ hidden: item.readStatus === 1 }"></span>
            </template>
            <template #value>
              <span class="message-time">{{ formatDateTime(item.sendTime || item.createTime) }}</span>
            </template>
          </van-cell>
        </van-cell-group>
        <van-empty v-else-if="!loading" description="暂无消息" />
      </van-list>
    </van-pull-refresh>

    <van-popup v-model:show="showDetail" position="bottom" round closeable class="detail-popup">
      <div class="detail-content" v-if="currentMessage">
        <div class="detail-title">{{ currentMessage.title }}</div>
        <div class="detail-time">{{ formatDateTime(currentMessage.sendTime || currentMessage.createTime) }}</div>
        <div class="detail-body">{{ currentMessage.content }}</div>
        <van-button
          v-if="currentMessage.linkUrl"
          type="primary"
          block
          @click="goLink(currentMessage.linkUrl)"
        >
          查看详情
        </van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getMessageListApi, markMessageReadApi } from '@/api/message.js'

const TAB_ALL = 0
const TAB_UNREAD = 1
const TAB_READ = 2

const router = useRouter()
const activeTab = ref(TAB_ALL)
const messages = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const showDetail = ref(false)
const currentMessage = ref(null)

const getReadStatus = () => {
  if (activeTab.value === TAB_UNREAD) return 0
  if (activeTab.value === TAB_READ) return 1
  return undefined
}

const loadMessages = async () => {
  try {
    const res = await getMessageListApi(pageNum.value, pageSize.value, {
      readStatus: getReadStatus()
    })
    const data = res.data?.data || {}
    const records = data.records || []
    if (refreshing.value || pageNum.value === 1) {
      messages.value = []
    }
    messages.value.push(...records)
    pageNum.value += 1
    finished.value = messages.value.length >= (data.total || 0)
  } catch (error) {
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const reloadMessages = () => {
  messages.value = []
  pageNum.value = 1
  finished.value = false
  loading.value = true
  loadMessages()
}

const openMessage = async (item) => {
  currentMessage.value = item
  showDetail.value = true
  if (item.readStatus === 1) {
    return
  }
  try {
    await markMessageReadApi(item.id)
    item.readStatus = 1
    if (activeTab.value === TAB_UNREAD) {
      messages.value = messages.value.filter(message => message.id !== item.id)
    }
  } catch (error) {
    // keep unread state when update fails
  }
}

const goLink = (link) => {
  if (link.startsWith('/')) {
    router.push(link)
    return
  }
  window.location.href = link
}

const formatDateTime = (value) => value ? String(value).replace('T', ' ').substring(0, 16) : ''
</script>

<style scoped>
.message-page {
  min-height: 100vh;
  background: #f7f8fa;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ee0a24;
  margin: 7px 8px 0 0;
  flex: 0 0 auto;
}

.unread-dot.hidden {
  visibility: hidden;
}

.message-time {
  color: #969799;
  font-size: 12px;
}

.detail-popup {
  min-height: 260px;
}

.detail-content {
  padding: 24px 18px 28px;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #323233;
  padding-right: 28px;
}

.detail-time {
  color: #969799;
  font-size: 12px;
  margin-top: 8px;
}

.detail-body {
  color: #323233;
  font-size: 15px;
  line-height: 1.7;
  white-space: pre-wrap;
  margin: 18px 0 24px;
}
</style>
