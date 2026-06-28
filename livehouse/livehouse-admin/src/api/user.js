import axios from '../utils/request.js'

// 用户管理
export const loginApi = (username, password) => {
  return axios.post('/admin/system/user/login', {
    username,
    password
  })
}

export const getUserInfoApi = (token) => {
  return axios.get('/admin/system/user/info', {
    headers: {
      Authorization: `Bearer ${token}`
    }
  })
}

export const getUserListApi = () => {
  return axios.get('/admin/system/user/list')
}

export const getUserByIdApi = (id) => {
  return axios.get(`/admin/system/user/${id}`)
}

export const getUserPageApi = (params) => {
  return axios.post('/admin/system/user/page', params)
}

export const addUserApi = (user) => {
  return axios.post('/admin/system/user', user)
}

export const updateUserApi = (user) => {
  return axios.put('/admin/system/user', user)
}

export const deleteUserApi = (id) => {
  return axios.delete(`/admin/system/user/${id}`)
}

export const resetPasswordApi = (id) => {
  return axios.put(`/admin/system/user/${id}/resetPassword`)
}

// 角色管理
export const getRoleListApi = (keyword) => {
  return axios.get('/admin/system/role/list', {
    params: { keyword }
  })
}

export const getRoleByIdApi = (id) => {
  return axios.get(`/admin/system/role/${id}`)
}

export const addRoleApi = (role) => {
  return axios.post('/admin/system/role', role)
}

export const updateRoleApi = (role) => {
  return axios.put('/admin/system/role', role)
}

export const deleteRoleApi = (id) => {
  return axios.delete(`/admin/system/role/${id}`)
}

export const getRoleMenusApi = (roleId) => {
  return axios.get(`/admin/system/role/${roleId}/menus`)
}

export const assignMenuApi = (roleId, menuIds) => {
  return axios.put('/admin/system/role/authMenu', {
    roleId,
    menuIds
  })
}

export const getRoleMenuTreeApi = (roleId) => {
  return axios.get(`/admin/system/role/${roleId}/menuTree`)
}

// 菜单管理
export const getMenuListApi = (keyword) => {
  return axios.get('/admin/system/menu/list', {
    params: { keyword }
  })
}

export const getMenuTreeApi = () => {
  return axios.get('/admin/system/menu/tree')
}

export const getMenuByUserIdApi = (userId) => {
  return axios.get(`/admin/system/menu/user/${userId}`)
}

export const getMenuByIdApi = (id) => {
  return axios.get(`/admin/system/menu/${id}`)
}

export const addMenuApi = (menu) => {
  return axios.post('/admin/system/menu', menu)
}

export const updateMenuApi = (menu) => {
  return axios.put('/admin/system/menu', menu)
}

export const deleteMenuApi = (id) => {
  return axios.delete(`/admin/system/menu/${id}`)
}

// 首页配置管理
export const getHomeConfigListApi = () => {
  return axios.get('/api/system/home-config/list')
}

export const getHomeConfigByIdApi = (id) => {
  return axios.get(`/api/system/home-config/${id}`)
}

export const addHomeConfigApi = (config) => {
  return axios.post('/api/system/home-config', config)
}

export const updateHomeConfigApi = (id, config) => {
  return axios.put(`/api/system/home-config/${id}`, config)
}

export const deleteHomeConfigApi = (id) => {
  return axios.delete(`/api/system/home-config/${id}`)
}

export const updateHomeConfigStatusApi = (id, status) => {
  return axios.put(`/api/system/home-config/${id}/status`, { status })
}

// 获取楼盘选项列表（用于配置联动）
export const getBuildingOptionsApi = () => {
  return axios.get('/api/house/building/options')
}

// 获取直播选项列表（用于配置联动）
export const getLiveOptionsApi = () => {
  return axios.get('/api/live/session/options')
}

// 获取VR选项列表（用于配置联动）
export const getVrOptionsApi = () => {
  return axios.get('/api/house/vr/options')
}