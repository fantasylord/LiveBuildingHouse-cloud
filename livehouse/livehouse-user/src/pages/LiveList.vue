<template>
  <div class="live-list-page">
    <van-search v-model="searchKey" placeholder="搜索直播" @search="handleSearch" />

    <van-tabs v-model:active="activeTab">
      <van-tab title="正在直播">
        <live-card-list :items="liveList.filter(item => item.status === 1)" type="live" @detail="goDetail" />
      </van-tab>
      <van-tab title="预约直播">
        <live-card-list :items="liveList.filter(item => item.status === 0)" type="schedule" @detail="goDetail" />
      </van-tab>
      <van-tab title="直播回放">
        <live-card-list :items="liveList.filter(item => item.status === 2)" type="replay" @detail="goDetail" />
      </van-tab>
    </van-tabs>

    <van-empty v-if="!loading && liveList.length === 0" description="暂无直播" />
  </div>
</template>

<script setup>
import { defineComponent, h, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getLiveListApi } from '@/api/live.js'
import AppImage from '@/components/AppImage.vue'

const router = useRouter()
const searchKey = ref('')
const activeTab = ref(0)
const liveList = ref([])
const loading = ref(false)

const LiveCardList = defineComponent({
  props: {
    items: { type: Array, default: () => [] },
    type: { type: String, default: 'live' }
  },
  emits: ['detail'],
  setup(props, { emit }) {
    const badgeText = () => ({ live: '直播中', schedule: '预告', replay: '回放' }[props.type])
    const badgeClass = () => ['live-badge', props.type === 'schedule' ? 'schedule' : '', props.type === 'replay' ? 'replay' : '']
    return () => h('div', { class: 'live-list' }, props.items.map(item => h('div', {
      class: 'live-card',
      onClick: () => emit('detail', item.id)
    }, [
      h('div', { class: 'live-cover-wrap' }, [
        item.coverImage ? h(AppImage, { className: 'live-cover-img', src: item.coverImage, alt: '' }) : h('div', { class: 'live-cover' }, item.sessionName?.slice(0, 2) || '直播'),
        h('div', { class: badgeClass() }, badgeText()),
        h('span', { class: 'viewer-count' }, `${item.totalViewer || 0}人观看`)
      ]),
      h('div', { class: 'live-body' }, [
        h('div', { class: 'live-title' }, item.sessionName),
        h('div', { class: 'live-desc' }, [
          h('span', item.anchorName || '置业顾问'),
          h('span', formatDateTime(item.startTime))
        ])
      ])
    ])))
  }
})

onMounted(() => {
  loadLiveList()
})

const loadLiveList = async () => {
  loading.value = true
  try {
    const response = await getLiveListApi(1, 50, {
      keyword: searchKey.value || undefined
    })
    liveList.value = response.data.data?.records || []
  } catch (error) {
    showToast('获取直播列表失败')
  } finally {
    loading.value = false
  }
}

const formatDateTime = (value) => value ? String(value).replace('T', ' ').substring(5, 16) : ''

const handleSearch = () => {
  loadLiveList()
}

const goDetail = (id) => {
  router.push(`/live/detail/${id}`)
}
</script>

<style>
.live-list-page {
  padding-bottom: 60px;
}

.live-list {
  padding: 10px;
}

.live-card {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 12px;
  overflow: hidden;
}

.live-cover-wrap {
  position: relative;
}

.live-cover {
  height: 180px;
  background: linear-gradient(135deg, #29323c, #485563);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  font-weight: 700;
}

.live-cover-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.live-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: #ff4d4f;
  color: #fff;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
}

.live-badge.schedule {
  background: #1989fa;
}

.live-badge.replay {
  background: #52c41a;
}

.viewer-count {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
}

.live-body {
  padding: 12px;
}

.live-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
}

.live-desc {
  display: flex;
  justify-content: space-between;
  color: #666;
  font-size: 13px;
}
</style>
