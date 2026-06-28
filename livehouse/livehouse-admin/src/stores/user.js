import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, getUserInfoApi } from '../api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref({})

  const isLoggedIn = computed(() => !!token.value)

  const login = async (username, password) => {
    const response = await loginApi(username, password)
    if (response.data.code === 200) {
      const data = response.data.data
      token.value = data.token
      userInfo.value = {
        userId: data.userId,
        username: data.username,
        realName: data.realName,
        phone: data.phone,
        email: data.email,
        avatar: data.avatar,
        roleId: data.roleId,
        roleName: data.roleName
      }
      localStorage.setItem('token', data.token)
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }
    return response
  }

  const getUserInfo = async () => {
    if (!token.value) return
    try {
      const response = await getUserInfoApi(token.value)
      if (response.data.code === 200) {
        userInfo.value = response.data.data
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  const initUserInfo = () => {
    const savedUserInfo = localStorage.getItem('userInfo')
    if (savedUserInfo) {
      userInfo.value = JSON.parse(savedUserInfo)
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    getUserInfo,
    logout,
    initUserInfo
  }
})