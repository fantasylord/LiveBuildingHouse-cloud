<template>
  <div class="page">
    <van-nav-bar title="浏览记录" left-arrow @click-left="goBack">
      <template #right>
        <van-icon name="delete-o" size="20" @click="clearAll" />
      </template>
    </van-nav-bar>

    <van-tabs v-model:active="active" @change="loadList">
      <van-tab title="全部" />
      <van-tab title="楼盘" />
      <van-tab title="直播" />
      <van-tab title="VR" />
    </van-tabs>

    <van-empty v-if="!loading && records.length === 0" description="暂无浏览记录" />
    <van-cell
      v-for="item in records"
      :key="item.id"
      :title="item.targetTitle || '未命名内容'"
      :label="item.targetDesc || formatTime(item.updateTime || item.createTime)"
      is-link
      @click="openItem(item)"
    >
      <template #icon>
        <AppImage v-if="item.targetCover" :src="item.targetCover" alt="" class="thumb" />
        <van-icon v-else :name="typeIcon(item.targetType)" class="fallback-icon" />
      </template>
      <template #right-icon>
        <van-icon name="cross" class="delete-icon" @click.stop="remove(item.id)" />
      </template>
    </van-cell>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { clearHistoryApi, deleteHistoryApi, getHistoryListApi } from '@/api/history.js'
import AppImage from '@/components/AppImage.vue'

const router = useRouter()
const active = ref(0)
const records = ref([])
const loading = ref(false)
const types = ['', 'house', 'live', 'vr']

const goBack = () => router.back()

const loadList = async () => {
  loading.value = true
  try {
    const params = { pageNum: 1, pageSize: 50 }
    if (types[active.value]) params.targetType = types[active.value]
    const res = await getHistoryListApi(params)
    const data = res.data?.data || {}
    records.value = data.records || data.list || []
  } catch (error) {
    if (error.response?.status === 401 || error.response?.data?.code === 401) {
      router.push('/login')
    } else {
      showToast('获取浏览记录失败')
    }
  } finally {
    loading.value = false
  }
}

const remove = async (id) => {
  await deleteHistoryApi(id)
  records.value = records.value.filter((item) => item.id !== id)
}

const clearAll = async () => {
  if (records.value.length === 0) return
  try {
    await showConfirmDialog({ title: '清空记录', message: '确认清空浏览记录吗？' })
    await clearHistoryApi()
    records.value = []
  } catch {}
}

const openItem = (item) => {
  const paths = {
    house: `/house/detail/${item.targetId}`,
    live: `/live/detail/${item.targetId}`,
    vr: `/vr/view/${item.targetId}`
  }
  router.push(paths[item.targetType] || '/')
}

const typeIcon = (type) => ({ house: 'home-o', live: 'play-circle-o', vr: 'eye-o' }[type] || 'clock-o')
const formatTime = (value) => String(value || '').replace('T', ' ').slice(0, 16)

onMounted(loadList)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f6f7f9;
}

.thumb {
  width: 54px;
  height: 42px;
  object-fit: cover;
  border-radius: 5px;
  margin-right: 10px;
}

.fallback-icon {
  width: 54px;
  margin-right: 10px;
  color: #1989fa;
  font-size: 24px;
  text-align: center;
}

.delete-icon {
  color: #999;
  padding: 8px;
}
</style>
