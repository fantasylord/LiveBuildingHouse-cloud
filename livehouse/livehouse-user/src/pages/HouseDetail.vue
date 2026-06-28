<template>
  <div class="house-detail-page">
    <van-nav-bar title="楼盘详情" left-arrow @click-left="goBack">
      <template #right>
        <van-icon :name="favorited ? 'star' : 'star-o'" size="22" :color="favorited ? '#ee0a24' : '#333'" @click="toggleFavorite" />
      </template>
    </van-nav-bar>

    <van-swipe v-if="images.length" :autoplay="3000" indicator-color="white">
      <van-swipe-item v-for="(img, index) in images" :key="index">
        <AppImage :src="img" alt="" className="detail-image" />
      </van-swipe-item>
    </van-swipe>
    <div v-else class="image-placeholder">
      <van-icon name="photo-o" size="48" color="#bbb" />
    </div>

    <div class="house-info">
      <h1 class="house-title">{{ building.buildingName || '楼盘名称' }}</h1>
      <div class="house-price-row">
        <span class="price-value">{{ building.avgPrice || '-' }}</span>
        <span class="price-unit">元/㎡</span>
      </div>
      <div class="house-meta">
        <span>{{ building.city || '-' }}</span>
        <span>{{ building.district || '-' }}</span>
        <span>{{ building.buildingType || '-' }}</span>
      </div>
      <div class="address">{{ building.address || '暂无地址' }}</div>
    </div>

    <div class="action-bar" v-if="vrList.length > 0">
      <van-button type="primary" icon="eye-o" block @click="goVrView">VR全景看房</van-button>
    </div>

    <van-tabs v-model:active="activeTab" sticky>
      <van-tab title="楼盘介绍">
        <div class="tab-content">
          <van-cell-group inset>
            <van-cell title="开发商" :value="building.developer || '-'" />
            <van-cell title="物业公司" :value="building.propertyCompany || '-'" />
            <van-cell title="绿化率" :value="building.greenRate ? `${building.greenRate}%` : '-'" />
            <van-cell title="容积率" :value="building.plotRatio || '-'" />
            <van-cell title="装修情况" :value="building.decorationType || '-'" />
            <van-cell title="总套数" :value="building.totalUnits ? `${building.totalUnits}套` : '-'" />
          </van-cell-group>
          <div class="description">{{ building.description || '暂无楼盘介绍' }}</div>
        </div>
      </van-tab>

      <van-tab title="户型图">
        <div class="tab-content">
          <van-empty v-if="units.length === 0" description="暂无户型" />
          <van-card
            v-for="unit in units"
            v-else
            :key="unit.id"
            :title="unit.unitName"
            :desc="`${unit.rooms || 0}室${unit.halls || 0}厅${unit.bathrooms || 0}卫`"
            :price="unit.price || '-'"
            :thumb="unit.imageUrl || building.coverImage"
            currency="约"
          >
            <template #tags>
              <van-tag plain type="primary">{{ unit.area || '-' }}㎡</van-tag>
            </template>
          </van-card>
        </div>
      </van-tab>

      <van-tab title="预约看房">
        <div class="tab-content">
          <van-form @submit="handleSubmit">
            <van-cell-group inset>
              <van-field v-model="form.name" label="姓名" placeholder="请输入姓名" required />
              <van-field v-model="form.phone" label="手机号" placeholder="请输入手机号" required type="tel" />
              <van-field v-model="form.date" label="预约日期" placeholder="如：2026-06-18" />
              <van-field v-model="form.time" label="预约时间" placeholder="如：14:00" />
              <van-field v-model="form.message" label="备注" placeholder="请输入备注（选填）" type="textarea" />
            </van-cell-group>
            <div class="submit-btn-wrap">
              <van-button type="primary" native-type="submit" block>立即预约</van-button>
            </div>
          </van-form>
        </div>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getHouseDetailApi } from '@/api/house.js'
import { createReserveApi } from '@/api/reserve.js'
import { checkFavoriteApi, toggleFavoriteApi } from '@/api/favorite.js'
import { recordHistoryApi } from '@/api/history.js'
import { getUserInfoApi } from '@/api/user.js'
import AppImage from '@/components/AppImage.vue'

const router = useRouter()
const route = useRoute()
const activeTab = ref(0)
const building = ref({})
const units = ref([])
const vrList = ref([])
const favorited = ref(false)

const form = reactive({
  name: '',
  phone: '',
  date: '',
  time: '',
  message: ''
})

const images = computed(() => {
  if (building.value.images) {
    try {
      const parsed = JSON.parse(building.value.images)
      return Array.isArray(parsed) ? parsed : [building.value.coverImage].filter(Boolean)
    } catch {
      return [building.value.coverImage].filter(Boolean)
    }
  }
  return [building.value.coverImage].filter(Boolean)
})

const goBack = () => router.back()

const goVrView = () => {
  if (vrList.value.length > 0) {
    router.push(`/vr/view/${vrList.value[0].id}`)
  }
}

const handleSubmit = async () => {
  if (!form.name || !form.phone) {
    showToast('请填写姓名和手机号')
    return
  }

  try {
    await createReserveApi({
      buildingId: route.params.id,
      customerName: form.name,
      phone: form.phone,
      visitTime: buildVisitTime(),
      remark: form.message
    })
    showToast('预约成功')
    form.date = ''
    form.time = ''
    form.message = ''
  } catch (error) {
    showToast('预约失败，请稍后重试')
  }
}

const buildVisitTime = () => {
  if (!form.date) return null
  return `${form.date}T${form.time || '09:00'}:00`
}

const toggleFavorite = async () => {
  if (!localStorage.getItem('token')) {
    router.push('/login')
    return
  }
  const res = await toggleFavoriteApi(buildFavoritePayload())
  favorited.value = !!res.data?.data?.favorited
  showToast(favorited.value ? '已收藏' : '已取消收藏')
}

const buildFavoritePayload = () => ({
  targetType: 'house',
  targetId: Number(route.params.id),
  targetTitle: building.value.buildingName,
  targetCover: building.value.coverImage,
  targetDesc: building.value.address || building.value.description
})

const recordHistory = async () => {
  if (!localStorage.getItem('token')) return
  try {
    await recordHistoryApi(buildFavoritePayload())
  } catch {}
}

const fetchFavorite = async () => {
  if (!localStorage.getItem('token')) return
  try {
    const res = await checkFavoriteApi({ targetType: 'house', targetId: route.params.id })
    favorited.value = !!res.data?.data?.favorited
  } catch {}
}

const prefillUser = async () => {
  if (!localStorage.getItem('token')) return
  try {
    const res = await getUserInfoApi()
    const user = res.data?.data || {}
    form.name = user.realName || ''
    form.phone = user.phone || ''
  } catch {}
}

const fetchHouseDetail = async () => {
  try {
    const res = await getHouseDetailApi(route.params.id)
    const data = res.data?.data
    building.value = data?.building || {}
    units.value = data?.units || []
    vrList.value = data?.vrList || []
    await Promise.all([recordHistory(), fetchFavorite(), prefillUser()])
  } catch (error) {
    showToast('获取楼盘详情失败')
  }
}

onMounted(fetchHouseDetail)
</script>

<style scoped>
.house-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.detail-image,
.image-placeholder {
  width: 100%;
  height: 250px;
  object-fit: cover;
  background: #e8e8e8;
}

.image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
}

.house-info {
  background: #fff;
  padding: 18px 16px;
}

.house-title {
  font-size: 22px;
  margin: 0 0 10px;
  color: #323233;
}

.house-price-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 10px;
}

.price-value {
  color: #ee0a24;
  font-size: 24px;
  font-weight: 700;
}

.price-unit,
.house-meta,
.address {
  color: #646566;
  font-size: 13px;
}

.house-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.action-bar {
  padding: 12px 16px;
  background: #fff;
  margin-top: 8px;
}

.tab-content {
  padding: 12px 0 24px;
}

.description {
  margin: 12px 16px 0;
  padding: 14px;
  border-radius: 8px;
  background: #fff;
  color: #323233;
  line-height: 1.7;
  white-space: pre-wrap;
}

.submit-btn-wrap {
  padding: 18px 16px;
}
</style>
