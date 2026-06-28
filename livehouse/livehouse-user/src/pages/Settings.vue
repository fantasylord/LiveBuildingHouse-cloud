<template>
  <div class="page">
    <van-nav-bar title="设置" left-arrow @click-left="goBack" />

    <van-form @submit="saveProfile">
      <van-cell-group inset title="个人资料">
        <van-field v-model="profile.realName" label="昵称" placeholder="请输入昵称" />
        <van-field v-model="profile.phone" label="手机号" placeholder="请输入手机号" type="tel" />
        <van-field v-model="profile.email" label="邮箱" placeholder="请输入邮箱" />
        <van-field v-model="profile.city" label="城市" placeholder="请输入城市" />
      </van-cell-group>
      <div class="action">
        <van-button block type="primary" native-type="submit">保存资料</van-button>
      </div>
    </van-form>

    <van-cell-group inset title="通知与隐私">
      <van-cell title="站内消息通知">
        <template #right-icon>
          <van-switch v-model="settings.messageNotify" :active-value="1" :inactive-value="0" size="22" @change="saveSettings" />
        </template>
      </van-cell>
      <van-cell title="短信通知">
        <template #right-icon>
          <van-switch v-model="settings.smsNotify" :active-value="1" :inactive-value="0" size="22" @change="saveSettings" />
        </template>
      </van-cell>
      <van-cell title="保存浏览记录">
        <template #right-icon>
          <van-switch v-model="settings.browseHistory" :active-value="1" :inactive-value="0" size="22" @change="saveSettings" />
        </template>
      </van-cell>
    </van-cell-group>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getSettingsApi, updateSettingsApi } from '@/api/settings.js'
import { getUserInfoApi, updateUserInfoApi } from '@/api/user.js'

const router = useRouter()
const profile = reactive({ realName: '', phone: '', email: '', city: '' })
const settings = reactive({ messageNotify: 1, smsNotify: 1, browseHistory: 1 })

const goBack = () => router.back()

const loadData = async () => {
  try {
    const [userRes, settingRes] = await Promise.all([getUserInfoApi(), getSettingsApi()])
    Object.assign(profile, userRes.data?.data || {})
    Object.assign(settings, settingRes.data?.data || {})
  } catch (error) {
    if (error.response?.status === 401 || error.response?.data?.code === 401) {
      router.push('/login')
    } else {
      showToast('加载设置失败')
    }
  }
}

const saveProfile = async () => {
  await updateUserInfoApi(profile)
  showToast('资料已保存')
}

const saveSettings = async () => {
  await updateSettingsApi(settings)
  showToast('设置已保存')
}

onMounted(loadData)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f6f7f9;
  padding-bottom: 24px;
}

.action {
  padding: 16px;
}
</style>
