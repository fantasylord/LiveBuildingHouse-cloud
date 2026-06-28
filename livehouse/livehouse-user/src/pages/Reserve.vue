<template>
  <div class="reserve-page">
    <van-nav-bar title="预约看房" left-arrow @click-left="goBack" />
    
    <van-form @submit="onSubmit">
      <van-cell-group>
        <van-field 
          v-model="form.name" 
          label="姓名" 
          placeholder="请输入姓名" 
          required 
        />
        <van-field 
          v-model="form.phone" 
          label="手机号" 
          placeholder="请输入手机号" 
          type="number"
          required 
        />
        <van-field 
          v-model="form.building" 
          label="预约楼盘" 
          placeholder="请选择楼盘" 
          required 
          readonly
          @click="showBuildingPicker = true"
        />
        <van-field 
          v-model="form.date" 
          label="预约日期" 
          placeholder="请选择日期" 
          required 
          readonly
          @click="showDatePicker = true"
        />
        <van-field 
          v-model="form.time" 
          label="预约时间" 
          placeholder="请选择时间" 
          required 
          readonly
          @click="showTimePicker = true"
        />
        <van-field 
          v-model="form.remark" 
          label="备注信息" 
          placeholder="请输入备注（选填）" 
          type="textarea"
        />
      </van-cell-group>
      
      <div style="margin: 16px;">
        <van-button type="primary" block native-type="submit">提交预约</van-button>
      </div>
    </van-form>
    
    <van-picker 
      v-model:show="showBuildingPicker" 
      :columns="buildingColumns" 
      @confirm="onBuildingConfirm" 
    />
    
    <van-date-picker 
      v-model:show="showDatePicker" 
      @confirm="onDateConfirm" 
    />
    
    <van-time-picker 
      v-model:show="showTimePicker" 
      @confirm="onTimeConfirm" 
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getHouseListApi } from '@/api/house.js'
import { createReserveApi } from '@/api/reserve.js'

const router = useRouter()

const form = reactive({
  name: '',
  phone: '',
  building: '',
  buildingId: '',
  date: '',
  time: '',
  remark: ''
})

const showBuildingPicker = ref(false)
const showDatePicker = ref(false)
const showTimePicker = ref(false)

const buildings = ref([])
const buildingColumns = computed(() => buildings.value.map(b => b.buildingName))

const goBack = () => {
  router.back()
}

const onBuildingConfirm = (value) => {
  form.building = value
  const building = buildings.value.find(b => b.buildingName === value)
  if (building) {
    form.buildingId = building.id
  }
  showBuildingPicker.value = false
}

const onDateConfirm = (value) => {
  form.date = value
  showDatePicker.value = false
}

const onTimeConfirm = (value) => {
  form.time = value
  showTimePicker.value = false
}

const onSubmit = async () => {
  if (!form.name || !form.phone || !form.building) {
    alert('请填写完整信息')
    return
  }
  
  try {
    await createReserveApi({
      buildingId: form.buildingId,
      customerName: form.name,
      phone: form.phone,
      visitTime: form.date ? `${form.date}T${form.time || '09:00'}:00` : null,
      remark: form.remark
    })
    alert('预约成功！我们的置业顾问将尽快与您联系。')
    setTimeout(() => {
      goBack()
    }, 1500)
  } catch (error) {
    alert('预约失败，请稍后重试')
  }
}

const fetchBuildings = async () => {
  try {
    const res = await getHouseListApi({ pageSize: 50 })
    if (res.data && res.data.data) {
      buildings.value = res.data.data.records || []
    }
  } catch (error) {
    console.error('获取楼盘列表失败:', error)
  }
}

onMounted(() => {
  fetchBuildings()
})
</script>

<style scoped>
.reserve-page {
  min-height: 100vh;
  background: #f5f5f5;
}
</style>
