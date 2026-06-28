<template>
  <div class="page">
    <van-nav-bar title="我的预约" left-arrow @click-left="goBack" />

    <van-pull-refresh v-model="refreshing" @refresh="loadList">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="loadMore">
        <van-empty v-if="!loading && records.length === 0" description="暂无预约记录" />
        <div v-for="item in records" :key="item.id" class="reserve-card">
          <div class="card-main">
            <AppImage v-if="item.buildingCover" :src="item.buildingCover" alt="" class="cover" />
            <div class="info">
              <div class="title">{{ item.buildingName || item.liveName || '预约看房' }}</div>
              <div class="desc">预约人：{{ item.customerName || '-' }}</div>
              <div class="desc">手机：{{ maskPhone(item.phone) }}</div>
              <div class="desc">时间：{{ formatTime(item.visitTime || item.createTime) }}</div>
            </div>
            <van-tag :type="statusMeta(item.visitStatus).type">{{ statusMeta(item.visitStatus).text }}</van-tag>
          </div>
          <div class="actions">
            <van-button v-if="canCancel(item)" size="small" plain type="danger" @click="cancel(item)">取消预约</van-button>
            <van-button v-if="item.buildingId" size="small" type="primary" plain @click="goHouse(item.buildingId)">查看楼盘</van-button>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { cancelReserveApi, getMyReserveListApi } from '@/api/reserve.js'
import AppImage from '@/components/AppImage.vue'

const router = useRouter()
const records = ref([])
const pageNum = ref(1)
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)

const goBack = () => router.back()
const goHouse = (id) => router.push(`/house/detail/${id}`)

const normalizeRecords = (data) => data?.records || data?.list || []

const loadList = async () => {
  pageNum.value = 1
  finished.value = false
  await fetchPage(true)
  refreshing.value = false
}

const loadMore = async () => {
  await fetchPage(false)
}

const fetchPage = async (reset) => {
  loading.value = true
  try {
    const res = await getMyReserveListApi({ pageNum: pageNum.value, pageSize: 10 })
    const data = res.data?.data || {}
    const next = normalizeRecords(data)
    records.value = reset ? next : records.value.concat(next)
    finished.value = !data.hasNext
    pageNum.value += 1
  } catch (error) {
    if (error.response?.status === 401 || error.response?.data?.code === 401) {
      router.push('/login')
    } else {
      showToast('获取预约失败')
      finished.value = true
    }
  } finally {
    loading.value = false
  }
}

const cancel = async (item) => {
  try {
    await showConfirmDialog({ title: '取消预约', message: '确认取消这条预约吗？' })
    await cancelReserveApi(item.id)
    showToast('已取消')
    await loadList()
  } catch (error) {
    if (error) {
      showToast(error.response?.data?.message || '取消失败')
    }
  }
}

const canCancel = (item) => item.visitStatus === 0 || item.visitStatus === 1 || item.visitStatus == null

const statusMeta = (status) => {
  const map = {
    0: { text: '待确认', type: 'warning' },
    1: { text: '已确认', type: 'primary' },
    2: { text: '已到访', type: 'success' },
    3: { text: '已取消', type: 'default' }
  }
  return map[status] || map[0]
}

const maskPhone = (phone) => {
  if (!phone) return '-'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const formatTime = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

onMounted(loadList)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f6f7f9;
}

.reserve-card {
  margin: 12px;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
}

.card-main {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.cover {
  width: 82px;
  height: 66px;
  object-fit: cover;
  border-radius: 6px;
  background: #eee;
}

.info {
  flex: 1;
  min-width: 0;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #222;
  margin-bottom: 6px;
}

.desc {
  color: #666;
  font-size: 13px;
  line-height: 1.7;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 10px;
}
</style>
