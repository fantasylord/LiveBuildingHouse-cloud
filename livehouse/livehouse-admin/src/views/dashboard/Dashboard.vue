<template>
  <div class="dashboard">
    <div class="toolbar">
      <div>
        <h2>数据统计</h2>
        <p>楼盘、直播和客户运营核心指标</p>
      </div>
      <div class="toolbar-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="loadLiveData"
        />
        <el-radio-group v-model="timeDimension" @change="loadLiveData">
          <el-radio-button label="day">日</el-radio-button>
          <el-radio-button label="week">周</el-radio-button>
          <el-radio-button label="month">月</el-radio-button>
        </el-radio-group>
        <el-button :icon="Download" type="primary" @click="handleExport('live')">导出直播报表</el-button>
      </div>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col v-for="item in statCards" :key="item.key" :xs="12" :sm="12" :md="6">
        <el-card class="stat-card" shadow="never">
          <div :class="['stat-icon', item.key]">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div>
            <div class="stat-value">{{ formatNumber(item.value) }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="14">
        <el-card class="panel" shadow="never" v-loading="loading">
          <template #header>
            <div class="panel-header">
              <span>直播数据趋势</span>
              <el-tag type="info">{{ liveRangeText }}</el-tag>
            </div>
          </template>
          <div class="metric-strip">
            <div>
              <b>{{ formatNumber(liveStats.totalViewer) }}</b>
              <span>累计观看</span>
            </div>
            <div>
              <b>{{ formatNumber(liveStats.peakOnline) }}</b>
              <span>峰值在线</span>
            </div>
            <div>
              <b>{{ formatDuration(liveStats.avgWatchTime) }}</b>
              <span>平均观看</span>
            </div>
            <div>
              <b>{{ percent(liveStats.leaveRate) }}</b>
              <span>留资转化</span>
            </div>
          </div>
          <div class="trend-chart">
            <div v-for="point in trendList" :key="point.date" class="trend-item">
              <div class="trend-bars">
                <span class="bar viewers" :style="{ height: barHeight(point.viewerCount, maxTrendViewer) }" />
                <span class="bar leads" :style="{ height: barHeight(point.leaveCount, maxTrendViewer) }" />
              </div>
              <span class="trend-label">{{ point.date }}</span>
            </div>
            <el-empty v-if="!trendList.length" description="暂无趋势数据" />
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card class="panel" shadow="never" v-loading="loading">
          <template #header>
            <div class="panel-header">
              <span>楼盘分布</span>
              <el-button text type="primary" :icon="Download" @click="handleExport('house')">导出</el-button>
            </div>
          </template>
          <div class="distribution-list">
            <div v-for="item in cityDistribution" :key="item.name" class="distribution-item">
              <div class="distribution-meta">
                <span>{{ item.name }}</span>
                <b>{{ item.value }}</b>
              </div>
              <el-progress :percentage="distributionPercent(item.value, cityDistribution)" :show-text="false" />
            </div>
            <el-empty v-if="!cityDistribution.length" description="暂无楼盘数据" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="lower-row">
      <el-col :xs="24" :lg="12">
        <el-card class="panel" shadow="never" v-loading="loading">
          <template #header>
            <div class="panel-header">
              <span>直播场次排行</span>
              <el-tag>{{ liveStats.total || 0 }} 场</el-tag>
            </div>
          </template>
          <el-table :data="liveStats.sessionList || []" height="300">
            <el-table-column prop="sessionName" label="直播场次" min-width="160" show-overflow-tooltip />
            <el-table-column prop="totalViewer" label="观看" width="90" />
            <el-table-column prop="peakOnline" label="峰值" width="90" />
            <el-table-column prop="leaveCount" label="留资" width="90" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card class="panel" shadow="never" v-loading="loading">
          <template #header>
            <div class="panel-header">
              <span>客户意向分布</span>
              <el-button text type="primary" :icon="Download" @click="handleExport('customer')">导出</el-button>
            </div>
          </template>
          <div class="distribution-list">
            <div v-for="item in intentionDistribution" :key="item.name" class="distribution-item">
              <div class="distribution-meta">
                <span>{{ item.name }}</span>
                <b>{{ item.value }}</b>
              </div>
              <el-progress :percentage="distributionPercent(item.value, intentionDistribution)" :show-text="false" />
            </div>
            <el-empty v-if="!intentionDistribution.length" description="暂无客户数据" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="panel recent-panel" shadow="never" v-loading="loading">
      <template #header>
        <div class="panel-header">
          <span>最近预约</span>
          <el-tag type="success">{{ recentReserves.length }} 条</el-tag>
        </div>
      </template>
      <el-table :data="recentReserves">
        <el-table-column prop="customerName" label="客户姓名" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="buildingName" label="预约楼盘" min-width="160" show-overflow-tooltip />
        <el-table-column prop="visitTime" label="到访时间" min-width="180" />
        <el-table-column prop="visitStatusName" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="reserveTagType(row.visitStatus)">{{ row.visitStatusName }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, House, User, VideoCamera, View } from '@element-plus/icons-vue'
import { exportStatisticsApi, getDashboardDataApi, getLiveStatisticsApi } from '@/api/dashboard'

const loading = ref(false)
const overview = ref({})
const houseStats = ref({})
const liveStats = ref({})
const customerStats = ref({})
const recentReserves = ref([])
const timeDimension = ref('day')
const dateRange = ref(defaultDateRange())

const statCards = computed(() => [
  { key: 'houses', label: '楼盘数量', value: overview.value.buildingCount, icon: House },
  { key: 'live', label: '直播场次', value: overview.value.liveCount, icon: VideoCamera },
  { key: 'customers', label: '客户数量', value: overview.value.customerCount, icon: User },
  { key: 'browse', label: '浏览记录', value: overview.value.browseCount, icon: View }
])

const trendList = computed(() => liveStats.value.trendList || [])
const cityDistribution = computed(() => houseStats.value.cityDistribution || [])
const intentionDistribution = computed(() => customerStats.value.intentionDistribution || [])
const maxTrendViewer = computed(() => {
  const max = Math.max(...trendList.value.map((item) => Number(item.viewerCount || 0)))
  return max > 0 ? max : 1
})
const liveRangeText = computed(() => {
  if (!dateRange.value || dateRange.value.length !== 2) return '本月'
  return `${dateRange.value[0]} 至 ${dateRange.value[1]}`
})

onMounted(() => {
  loadDashboard()
})

async function loadDashboard() {
  loading.value = true
  try {
    const { data } = await getDashboardDataApi()
    const payload = data.data || {}
    overview.value = payload.overview || {}
    houseStats.value = payload.houseStats || {}
    liveStats.value = payload.liveStats || {}
    customerStats.value = payload.customerStats || {}
    recentReserves.value = payload.recentReserves || []
  } finally {
    loading.value = false
  }
}

async function loadLiveData() {
  const [startDate, endDate] = dateRange.value || []
  const { data } = await getLiveStatisticsApi({
    startDate,
    endDate,
    timeDimension: timeDimension.value,
    pageNum: 1,
    pageSize: 10
  })
  liveStats.value = data.data || {}
}

async function handleExport(type) {
  const [startDate, endDate] = dateRange.value || []
  const response = await exportStatisticsApi({ type, startDate, endDate })
  const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `livehouse-${type}-report.csv`
  link.click()
  window.URL.revokeObjectURL(url)
  ElMessage.success('报表已导出')
}

function defaultDateRange() {
  const now = new Date()
  const start = new Date(now.getFullYear(), now.getMonth(), 1)
  return [formatDate(start), formatDate(now)]
}

function formatDate(date) {
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

function formatNumber(value) {
  return Number(value || 0).toLocaleString()
}

function formatDuration(seconds) {
  const value = Number(seconds || 0)
  if (value < 60) return `${value}秒`
  return `${Math.round(value / 60)}分钟`
}

function percent(value) {
  return `${Math.round(Number(value || 0) * 10000) / 100}%`
}

function barHeight(value, max) {
  const percent = Math.max(8, Math.round((Number(value || 0) / max) * 100))
  return `${percent}%`
}

function distributionPercent(value, list) {
  const max = Math.max(...list.map((item) => Number(item.value || 0)), 1)
  return Math.round((Number(value || 0) / max) * 100)
}

function reserveTagType(status) {
  if (status === 1) return 'primary'
  if (status === 2) return 'success'
  if (status === 3) return 'info'
  return 'warning'
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
  background: #f6f7fb;
  min-height: 100%;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 16px;
}

.toolbar h2 {
  margin: 0;
  font-size: 24px;
  color: #1f2937;
}

.toolbar p {
  margin: 6px 0 0;
  color: #667085;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.stats-row,
.lower-row {
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 96px;
  border: 1px solid #edf0f5;
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
}

.stat-icon.houses {
  background: #2563eb;
}

.stat-icon.live {
  background: #dc2626;
}

.stat-icon.customers {
  background: #059669;
}

.stat-icon.browse {
  background: #7c3aed;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #111827;
  line-height: 1.1;
}

.stat-label {
  color: #6b7280;
  margin-top: 6px;
}

.panel {
  border: 1px solid #edf0f5;
  margin-bottom: 16px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-weight: 600;
}

.metric-strip {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-bottom: 18px;
}

.metric-strip div {
  background: #f8fafc;
  border: 1px solid #eef2f7;
  border-radius: 8px;
  padding: 12px;
}

.metric-strip b {
  display: block;
  font-size: 20px;
  color: #111827;
}

.metric-strip span {
  display: block;
  margin-top: 4px;
  color: #667085;
  font-size: 13px;
}

.trend-chart {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  min-height: 220px;
  overflow-x: auto;
  padding: 8px 4px 0;
}

.trend-item {
  min-width: 54px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.trend-bars {
  height: 170px;
  width: 34px;
  display: flex;
  align-items: flex-end;
  gap: 4px;
}

.bar {
  width: 15px;
  border-radius: 6px 6px 0 0;
}

.bar.viewers {
  background: #2563eb;
}

.bar.leads {
  background: #10b981;
}

.trend-label {
  font-size: 12px;
  color: #667085;
  white-space: nowrap;
}

.distribution-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 258px;
}

.distribution-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #374151;
}

.distribution-meta b {
  color: #111827;
}

.recent-panel {
  margin-bottom: 0;
}

@media (max-width: 900px) {
  .toolbar {
    flex-direction: column;
  }

  .toolbar-actions {
    justify-content: flex-start;
  }

  .metric-strip {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
