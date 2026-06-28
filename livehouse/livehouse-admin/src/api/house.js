import axios from '../utils/request.js'

// ============= 楼盘管理 =============

/**
 * 获取楼盘列表
 */
export const getBuildingListApi = (pageNum = 1, pageSize = 10, params = {}) => {
  return axios.get('/api/house/building/list', {
    params: {
      pageNum,
      pageSize,
      ...params
    }
  })
}

/**
 * 获取楼盘详情
 */
export const getBuildingDetailApi = (id) => {
  return axios.get(`/api/house/building/${id}`)
}

/**
 * 新增楼盘
 */
export const addBuildingApi = (building) => {
  return axios.post('/api/house/building', building)
}

/**
 * 更新楼盘
 */
export const updateBuildingApi = (building) => {
  return axios.put('/api/house/building', building)
}

/**
 * 删除楼盘
 */
export const deleteBuildingApi = (id) => {
  return axios.delete(`/api/house/building/${id}`)
}

/**
 * 修改楼盘状态
 */
export const changeBuildingStatusApi = (id, status) => {
  return axios.put(`/api/house/building/${id}/changeStatus`, null, {
    params: { status }
  })
}

/**
 * 设置楼盘私密
 */
export const setBuildingPrivateApi = (id, isPrivate) => {
  return axios.put(`/api/house/building/${id}/setPrivate`, null, {
    params: { isPrivate }
  })
}

// ============= 户型管理 =============

/**
 * 获取户型列表
 */
export const getUnitListApi = (pageNum = 1, pageSize = 10, params = {}) => {
  return axios.get('/api/house/unit/list', {
    params: {
      pageNum,
      pageSize,
      ...params
    }
  })
}

/**
 * 获取户型详情
 */
export const getUnitDetailApi = (id) => {
  return axios.get(`/api/house/unit/${id}`)
}

/**
 * 新增户型
 */
export const addUnitApi = (unit) => {
  return axios.post('/api/house/unit', unit)
}

/**
 * 更新户型
 */
export const updateUnitApi = (unit) => {
  return axios.put('/api/house/unit', unit)
}

/**
 * 删除户型
 */
export const deleteUnitApi = (id) => {
  return axios.delete(`/api/house/unit/${id}`)
}

/**
 * 修改户型状态
 */
export const changeUnitStatusApi = (id, status) => {
  return axios.put(`/api/house/unit/${id}/changeStatus`, null, {
    params: { status }
  })
}

// ============= VR素材管理 =============

/**
 * 获取VR列表
 */
export const getVrListApi = (pageNum = 1, pageSize = 10, params = {}) => {
  return axios.get('/api/house/vr/list', {
    params: {
      pageNum,
      pageSize,
      ...params
    }
  })
}

/**
 * 获取VR详情
 */
export const getVrDetailApi = (id) => {
  return axios.get(`/api/house/vr/${id}`)
}

/**
 * 新增VR素材
 */
export const addVrApi = (vr) => {
  return axios.post('/api/house/vr', vr)
}

/**
 * 更新VR素材
 */
export const updateVrApi = (vr) => {
  return axios.put('/api/house/vr', vr)
}

/**
 * 删除VR素材
 */
export const deleteVrApi = (id) => {
  return axios.delete(`/api/house/vr/${id}`)
}

/**
 * 修改VR状态
 */
export const changeVrStatusApi = (id, status) => {
  return axios.put(`/api/house/vr/${id}/changeStatus`, null, {
    params: { status }
  })
}
