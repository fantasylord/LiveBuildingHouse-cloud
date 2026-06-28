<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="layout-sidebar">
      <div class="logo">
        <el-icon class="logo-icon">
          <component :is="resolveMenuIcon('Home')" />
        </el-icon>
        <span class="logo-text">LiveHouse</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        mode="vertical"
        class="sidebar-menu"
        @select="handleMenuSelect"
      >
        <menu-tree v-if="menuTree.length > 0" :menus="menuTree" />
        <template v-else>
          <el-menu-item index="/dashboard">
            <el-icon><component :is="resolveMenuIcon('LayoutDashboard')" /></el-icon>
            <span>数据看板</span>
          </el-menu-item>

          <el-sub-menu index="/system">
            <template #title>
              <el-icon><component :is="resolveMenuIcon('User')" /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/user">用户管理</el-menu-item>
            <el-menu-item index="/system/role">角色管理</el-menu-item>
            <el-menu-item index="/system/menu">菜单管理</el-menu-item>
            <el-menu-item index="/system/home-config">首页配置</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/house">
            <template #title>
              <el-icon><component :is="resolveMenuIcon('Home')" /></el-icon>
              <span>房源管理</span>
            </template>
            <el-menu-item index="/house/building">楼盘管理</el-menu-item>
            <el-menu-item index="/house/unit">户型管理</el-menu-item>
            <el-menu-item index="/house/vr">VR管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/live">
            <template #title>
              <el-icon><component :is="resolveMenuIcon('Video')" /></el-icon>
              <span>直播管理</span>
            </template>
            <el-menu-item index="/live/session">直播场次</el-menu-item>
            <el-menu-item index="/live/platform">直播平台</el-menu-item>
            <el-menu-item index="/live/whitelist">白名单管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/customer">
            <template #title>
              <el-icon><component :is="resolveMenuIcon('Users')" /></el-icon>
              <span>客户管理</span>
            </template>
            <el-menu-item index="/customer/list">客户列表</el-menu-item>
            <el-menu-item index="/customer/follow">跟进记录</el-menu-item>
            <el-menu-item index="/customer/reserve">预约管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/message">
            <template #title>
              <el-icon><component :is="resolveMenuIcon('Message')" /></el-icon>
              <span>消息中心</span>
            </template>
            <el-menu-item index="/message/list">消息管理</el-menu-item>
            <el-menu-item index="/message/sms">短信记录</el-menu-item>
            <el-menu-item index="/message/template">模板配置</el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-icon><component :is="resolveMenuIcon('User')" /></el-icon>
              <span>{{ userRealName || '管理员' }}</span>
              <el-icon><component :is="resolveMenuIcon('ArrowDown')" /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人中心</el-dropdown-item>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, provide } from 'vue'
import { useRouter } from 'vue-router'
import { getMenuByUserIdApi } from '../api/user'
import MenuTree from './MenuTree.vue'
import { resolveMenuIcon } from '../utils/icon'

const router = useRouter()
const menuTree = ref([])
const userRealName = ref('')

const activeMenu = computed(() => router.currentRoute.value.path)

const loadUserMenus = async () => {
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (!userInfoStr) return

    const userInfo = JSON.parse(userInfoStr)
    userRealName.value = userInfo.realName || userInfo.username || ''
    const userId = userInfo.userId || userInfo.id
    if (!userId) {
      console.warn('未找到当前用户ID')
      return
    }

    const response = await getMenuByUserIdApi(userId)
    if (response.data?.code === 200) {
      const menus = response.data.data || []
      if (menus.length > 0) {
        menuTree.value = menus
      }
    }
  } catch (error) {
    console.error('加载用户菜单失败:', error)
  }
}

provide('refreshMenus', loadUserMenus)

const handleMenuSelect = (key) => {
  router.push(key)
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}

onMounted(() => {
  loadUserMenus()
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-sidebar {
  background-color: #fff;
  color: #303133;
  border-right: 1px solid #e4e7ed;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  border-bottom: 1px solid #ebeef5;
}

.logo-icon {
  margin-right: 8px;
  font-size: 24px;
}

.sidebar-menu {
  border-right: none;
  background-color: transparent;
}

.layout-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.08);
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 6px;
}

.layout-main {
  background-color: #f3f4f6;
  padding: 20px;
  overflow-y: auto;
}
</style>
