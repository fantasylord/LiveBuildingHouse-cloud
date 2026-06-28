import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/pages/Home.vue')
  },
  {
    path: '/house/list',
    name: 'HouseList',
    component: () => import('@/pages/HouseList.vue')
  },
  {
    path: '/house/detail/:id',
    name: 'HouseDetail',
    component: () => import('@/pages/HouseDetail.vue')
  },
  {
    path: '/vr/view/:id',
    name: 'VrView',
    component: () => import('@/pages/vr/View.vue')
  },
  {
    path: '/live/list',
    name: 'LiveList',
    component: () => import('@/pages/LiveList.vue')
  },
  {
    path: '/live/detail/:id',
    name: 'LiveDetail',
    component: () => import('@/pages/LiveDetail.vue')
  },
  {
    path: '/reserve',
    name: 'Reserve',
    component: () => import('@/pages/Reserve.vue')
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/pages/Profile.vue')
  },
  {
    path: '/message',
    name: 'MessageList',
    component: () => import('@/pages/MessageList.vue')
  },
  {
    path: '/my-reserve',
    name: 'MyReserve',
    component: () => import('@/pages/MyReserve.vue')
  },
  {
    path: '/history',
    name: 'BrowseHistory',
    component: () => import('@/pages/BrowseHistory.vue')
  },
  {
    path: '/favorite',
    name: 'FavoriteList',
    component: () => import('@/pages/FavoriteList.vue')
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/pages/Settings.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/pages/Register.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/Login.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
