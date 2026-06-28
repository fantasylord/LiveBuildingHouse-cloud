<template>
  <div class="app-container">
    <router-view />
    <van-tabbar v-if="showTabBar" route>
      <van-tabbar-item icon="home-o" to="/">首页</van-tabbar-item>
      <van-tabbar-item icon="hotel-o" to="/house/list">楼盘</van-tabbar-item>
      <van-tabbar-item icon="video-o" to="/live/list">直播</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const showTabBar = computed(() => {
  const hideTabs = ['/house/detail', '/live/detail', '/reserve', '/register', '/login']
  return !hideTabs.some(path => route.path.startsWith(path))
})
</script>

<style>
.app-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}
</style>