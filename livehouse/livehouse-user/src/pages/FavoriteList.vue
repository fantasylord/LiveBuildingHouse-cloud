<template>
  <div class="page">
    <van-nav-bar title="我的收藏" left-arrow @click-left="goBack" />
    <van-tabs v-model:active="active" @change="loadList">
      <van-tab title="全部" />
      <van-tab title="楼盘" />
      <van-tab title="直播" />
      <van-tab title="VR" />
    </van-tabs>

    <van-empty v-if="!loading && records.length === 0" description="暂无收藏" />
    <van-swipe-cell v-for="item in records" :key="item.id">
      <van-cell
        :title="item.targetTitle || '未命名内容'"
        :label="item.targetDesc || formatTime(item.updateTime || item.createTime)"
        is-link
        @click="openItem(item)"
      >
        <template #icon>
          <AppImage v-if="item.targetCover" :src="item.targetCover" className="thumb" />
          <van-icon v-else :name="typeIcon(item.targetType)" class="fallback-icon" />
        </template>
      </van-cell>
      <template #right>
        <van-button square type="danger" text="取消收藏" class="swipe-button" @click="remove(item.id)" />
      </template>
    </van-swipe-cell>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { deleteFavoriteApi, getFavoriteListApi } from '@/api/favorite.js'
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
    const res = await getFavoriteListApi(params)
    const data = res.data?.data || {}
    records.value = data.records || data.list || []
  } catch (error) {
    if (error.response?.status === 401 || error.response?.data?.code === 401) {
      router.push('/login')
    } else {
      showToast('获取收藏失败')
    }
  } finally {
    loading.value = false
  }
}

const remove = async (id) => {
  await deleteFavoriteApi(id)
  records.value = records.value.filter((item) => item.id !== id)
}

const openItem = (item) => {
  const paths = {
    house: `/house/detail/${item.targetId}`,
    live: `/live/detail/${item.targetId}`,
    vr: `/vr/view/${item.targetId}`
  }
  router.push(paths[item.targetType] || '/')
}

const typeIcon = (type) => ({ house: 'home-o', live: 'play-circle-o', vr: 'eye-o' }[type] || 'star-o')
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
  color: #ee0a24;
  font-size: 24px;
  text-align: center;
}

.swipe-button {
  height: 100%;
}
</style>
