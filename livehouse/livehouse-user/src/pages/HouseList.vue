<template>
  <div class="house-list-page">
    <van-search v-model="searchKey" placeholder="搜索楼盘" @search="handleSearch" />
    
    <van-tabs v-model="activeTab" @change="handleTabChange">
      <van-tab title="全部">
        <div class="house-grid">
          <van-card v-for="house in houseList" :key="house.id" clickable @click="goDetail(house.id)" class="house-card">
            <template #cover>
              <AppImage :src="house.coverImage" alt="" className="house-card-cover" />
            </template>
            <template #title>
              <span class="house-card-name">{{ house.buildingName }}</span>
              <van-tag v-if="house.tag" type="primary" size="small">{{ house.tag }}</van-tag>
            </template>
            <template #desc>
              <span class="house-card-address">{{ house.address }}</span>
            </template>
            <template #footer>
              <span class="house-card-price">{{ house.avgPrice }}</span>
              <span class="house-card-unit">元/㎡</span>
            </template>
          </van-card>
        </div>
        <van-load-more v-if="total > houseList.length" :loading="loading" @loadmore="loadMore" />
      </van-tab>
      <van-tab title="热销">
        <div class="house-grid">
          <van-card v-for="house in hotList" :key="house.id" clickable @click="goDetail(house.id)" class="house-card">
            <template #cover>
              <AppImage :src="house.coverImage" alt="" className="house-card-cover" />
            </template>
            <template #title>
              <span class="house-card-name">{{ house.buildingName }}</span>
              <van-tag type="danger" size="small">热销</van-tag>
            </template>
            <template #desc>
              <span class="house-card-address">{{ house.address }}</span>
            </template>
            <template #footer>
              <span class="house-card-price">{{ house.avgPrice }}</span>
              <span class="house-card-unit">元/㎡</span>
            </template>
          </van-card>
        </div>
      </van-tab>
      <van-tab title="新品">
        <div class="house-grid">
          <van-card v-for="house in newList" :key="house.id" clickable @click="goDetail(house.id)" class="house-card">
            <template #cover>
              <AppImage :src="house.coverImage" alt="" className="house-card-cover" />
            </template>
            <template #title>
              <span class="house-card-name">{{ house.buildingName }}</span>
              <van-tag type="success" size="small">新品</van-tag>
            </template>
            <template #desc>
              <span class="house-card-address">{{ house.address }}</span>
            </template>
            <template #footer>
              <span class="house-card-price">{{ house.avgPrice }}</span>
              <span class="house-card-unit">元/㎡</span>
            </template>
          </van-card>
        </div>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHouseListApi, getHotHouseApi } from '@/api/house.js'
import AppImage from '@/components/AppImage.vue'

const router = useRouter()

const searchKey = ref('')
const activeTab = ref(0)
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const houseList = ref([])
const hotList = ref([])
const newList = ref([])

const handleSearch = () => {
  pageNum.value = 1
  houseList.value = []
  fetchHouseList()
}

const handleTabChange = () => {
  if (activeTab.value === 0) {
    fetchHouseList()
  } else if (activeTab.value === 1) {
    fetchHotList()
  } else if (activeTab.value === 2) {
    fetchNewList()
  }
}

const loadMore = () => {
  if (loading.value || houseList.value.length >= total.value) return
  loading.value = true
  pageNum.value++
  fetchHouseList(true)
}

const fetchHouseList = async (append = false) => {
  try {
    const res = await getHouseListApi({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKey.value
    })
    if (res.data && res.data.data) {
      const data = res.data.data
      total.value = data.total || 0
      if (append) {
        houseList.value = [...houseList.value, ...data.records]
      } else {
        houseList.value = data.records || []
      }
    }
  } catch (error) {
    console.error('获取楼盘列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchHotList = async () => {
  try {
    const res = await getHotHouseApi()
    if (res.data && res.data.data) {
      hotList.value = res.data.data.slice(0, 3)
    }
  } catch (error) {
    console.error('获取热门楼盘失败:', error)
  }
}

const fetchNewList = async () => {
  try {
    const res = await getHouseListApi({ sortField: 'createTime' })
    if (res.data && res.data.data) {
      newList.value = (res.data.data.records || []).slice(0, 3)
    }
  } catch (error) {
    console.error('获取新盘列表失败:', error)
  }
}

const goDetail = (id) => {
  router.push(`/house/detail/${id}`)
}

onMounted(() => {
  fetchHouseList()
})
</script>

<style>
.house-list-page {
  padding-bottom: 60px;
}

.house-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  padding: 10px;
}

.house-card {
  margin: 0;
}

.house-card-cover {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.house-card-name {
  font-size: 14px;
  font-weight: 600;
}

.house-card-address {
  font-size: 12px;
  color: #999;
}

.house-card-price {
  font-size: 18px;
  font-weight: bold;
  color: #ff4d4f;
}

.house-card-unit {
  font-size: 12px;
  color: #999;
  margin-left: 4px;
}
</style>