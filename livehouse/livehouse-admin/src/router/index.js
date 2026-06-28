import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue')
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/Layout.vue'),
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue')
      },
      {
        path: '/system/user',
        name: 'UserManagement',
        component: () => import('@/views/system/UserList.vue')
      },
      {
        path: '/system/role',
        name: 'RoleManagement',
        component: () => import('@/views/system/RoleList.vue')
      },
      {
        path: '/system/menu',
        name: 'MenuManagement',
        component: () => import('@/views/system/MenuList.vue')
      },
      {
        path: '/system/home-config',
        name: 'HomeConfigManagement',
        component: () => import('@/views/system/HomeConfigManage.vue')
      },
      {
        path: '/house/building',
        name: 'BuildingManagement',
        component: () => import('@/views/house/BuildingList.vue')
      },
      {
        path: '/house/unit',
        name: 'UnitManagement',
        component: () => import('@/views/house/UnitList.vue')
      },
      {
        path: '/house/vr',
        name: 'VrManagement',
        component: () => import('@/views/house/VrList.vue')
      },
      {
        path: '/live/session',
        name: 'SessionManagement',
        component: () => import('@/views/live/SessionList.vue')
      },
      {
        path: '/live/platform',
        name: 'PlatformManagement',
        component: () => import('@/views/live/PlatformList.vue')
      },
      {
        path: '/live/whitelist',
        name: 'WhitelistManagement',
        component: () => import('@/views/live/WhitelistList.vue')
      },
      {
        path: '/customer/list',
        name: 'CustomerList',
        component: () => import('@/views/customer/CustomerList.vue')
      },
      {
        path: '/customer/follow',
        name: 'FollowManagement',
        component: () => import('@/views/customer/FollowList.vue')
      },
      {
        path: '/customer/reserve',
        name: 'ReserveManagement',
        component: () => import('@/views/customer/ReserveList.vue')
      },
      {
        path: '/message/list',
        name: 'MessageManagement',
        component: () => import('@/views/message/MessageList.vue')
      },
      {
        path: '/message/sms',
        name: 'SmsManagement',
        component: () => import('@/views/message/SmsRecordList.vue')
      },
      {
        path: '/message/template',
        name: 'MessageTemplateManagement',
        component: () => import('@/views/message/MessageTemplateList.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.path !== '/login' && !userStore.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && userStore.isLoggedIn) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
