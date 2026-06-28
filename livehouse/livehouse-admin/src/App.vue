<!-- <template>
  <el-container class="app-container">
    <el-aside v-if="isLoggedIn" width="200px" class="sidebar">
      <el-menu :default-active="activeMenu" class="sidebar-menu">
        <el-menu-item index="/dashboard">
          <el-icon><component :is="House" /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-sub-menu index="/system">
          <template #title>
            <el-icon><component :is="Setting" /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/role">角色管理</el-menu-item>
          <el-menu-item index="/system/menu">菜单管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/house">
          <template #title>
            <el-icon><component :is="OfficeBuilding" /></el-icon>
            <span>房源管理</span>
          </template>
          <el-menu-item index="/house/building">楼盘管理</el-menu-item>
          <el-menu-item index="/house/unit">户型管理</el-menu-item>
          <el-menu-item index="/house/vr">VR管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/live">
          <template #title>
            <el-icon><component :is="Video" /></el-icon>
            <span>直播管理</span>
          </template>
          <el-menu-item index="/live/session">直播场次</el-menu-item>
          <el-menu-item index="/live/whitelist">白名单管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/customer">
          <template #title>
            <el-icon><component :is="User" /></el-icon>
            <span>客户管理</span>
          </template>
          <el-menu-item index="/customer/list">客户列表</el-menu-item>
          <el-menu-item index="/customer/follow">跟进记录</el-menu-item>
          <el-menu-item index="/customer/reserve">预约管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container v-if="isLoggedIn">
      <el-header class="header">
        <div class="header-left">
          <el-button icon="Menu" @click="toggleSidebar" class="menu-btn"></el-button>
          <span class="logo">LiveHouse</span>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-icon><component :is="User" /></el-icon>
              {{ userInfo.realName || '管理员' }}
            </span>
            <template #dropdown>
              <el-dropdown-item @click="goProfile">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
    <router-view v-else />
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { House, Setting, OfficeBuilding, VideoCamera, User } from '@element-plus/icons-vue'
import { useUserStore } from './stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)
const userInfo = computed(() => userStore.userInfo)
const activeMenu = computed(() => route.path)

const toggleSidebar = () => {}

const goProfile = () => {}

const logout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style>
.app-container {
  height: 100vh;
}

.sidebar {
  background-color: #2f4050;
}

.sidebar-menu {
  height: 100%;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background-color: #fff;
  border-bottom: 1px solid #eee;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #1890ff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
}

.main-content {
  padding: 20px;
  overflow-y: auto;
}

.menu-btn {
  border: none;
  background: none;
}
</style> -->
<template>
  <router-view />
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  width: 100%;
}
</style>