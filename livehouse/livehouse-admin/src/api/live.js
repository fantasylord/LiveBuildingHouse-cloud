import axios from '../utils/request.js'

// ============= 直播场次管理 =============

/**
 * 获取直播场次列表
 */
export const getSessionListApi = (pageNum = 1, pageSize = 10, params = {}) => {
  return axios.get('/api/live/session/list', {
    params: {
      pageNum,
      pageSize,
      ...params
    }
  })
}

/**
 * 获取直播场次详情
 */
export const getSessionDetailApi = (id) => {
  return axios.get(`/api/live/session/${id}`)
}

/**
 * 新增直播场次
 */
export const addSessionApi = (session) => {
  return axios.post('/api/live/session', session)
}

/**
 * 更新直播场次
 */
export const updateSessionApi = (session) => {
  return axios.put('/api/live/session', session)
}

/**
 * 删除直播场次
 */
export const deleteSessionApi = (id) => {
  return axios.delete(`/api/live/session/${id}`)
}

/**
 * 开播
 */
export const startLiveApi = (id) => {
  return axios.post(`/api/live/session/${id}/start`)
}

export const notifyLiveStartApi = (id, payload) => {
  return axios.post(`/api/live/session/${id}/notify-start`, payload)
}

/**
 * 关播
 */
export const stopLiveApi = (id) => {
  return axios.post(`/api/live/session/${id}/stop`)
}

/**
 * 修改直播状态
 */
export const changeSessionStatusApi = (id, status) => {
  return axios.put(`/api/live/session/${id}/changeStatus`, null, {
    params: { status }
  })
}

export const syncSessionStatusApi = (id) => {
  return axios.post(`/api/live/session/${id}/sync-status`)
}

export const getSessionPushInfoApi = (id) => {
  return axios.get(`/api/live/session/${id}/push-info`)
}

export const getSessionStatisticsApi = (id) => {
  return axios.get(`/api/live/session/${id}/statistics`)
}

export const getReplayListApi = (pageNum = 1, pageSize = 10, params = {}) => {
  return axios.get('/api/live/session/replay/list', {
    params: {
      pageNum,
      pageSize,
      ...params
    }
  })
}

export const updateReplayApi = (id, replay) => {
  return axios.put(`/api/live/session/${id}/replay`, replay)
}

export const deleteReplayApi = (id) => {
  return axios.delete(`/api/live/session/${id}/replay`)
}

// ============= 直播平台配置管理 =============

/**
 * 获取直播平台列表
 */
export const getPlatformListApi = (pageNum = 1, pageSize = 10, params = {}) => {
  return axios.get('/api/live/platform/list', {
    params: {
      pageNum,
      pageSize,
      ...params
    }
  })
}

/**
 * 获取直播平台选项列表（不分页）
 */
export const getPlatformOptionsApi = (status = null) => {
  return axios.get('/api/live/platform/options', {
    params: { status }
  })
}

/**
 * 获取直播平台详情
 */
export const getPlatformDetailApi = (id) => {
  return axios.get(`/api/live/platform/${id}`)
}

/**
 * 新增直播平台
 */
export const addPlatformApi = (platform) => {
  return axios.post('/api/live/platform', platform)
}

/**
 * 更新直播平台
 */
export const updatePlatformApi = (platform) => {
  return axios.put('/api/live/platform', platform)
}

/**
 * 删除直播平台
 */
export const deletePlatformApi = (id) => {
  return axios.delete(`/api/live/platform/${id}`)
}

// ============= 直播白名单管理 =============

/**
 * 获取白名单列表
 */
export const getWhitelistListApi = (pageNum = 1, pageSize = 10, params = {}) => {
  return axios.get('/api/live/whitelist/list', {
    params: {
      pageNum,
      pageSize,
      ...params
    }
  })
}

/**
 * 获取白名单详情
 */
export const getWhitelistDetailApi = (id) => {
  return axios.get(`/api/live/whitelist/${id}`)
}

/**
 * 新增白名单
 */
export const addWhitelistApi = (whitelist) => {
  return axios.post('/api/live/whitelist', whitelist)
}

/**
 * 批量添加白名单
 */
export const batchAddWhitelistApi = (payload) => {
  return axios.post('/api/live/whitelist/batch', payload)
}

export const batchAddWhitelistCustomersApi = (payload) => {
  return axios.post('/api/live/whitelist/batch/customers', payload)
}

/**
 * 删除白名单
 */
export const deleteWhitelistApi = (id) => {
  return axios.delete(`/api/live/whitelist/${id}`)
}

/**
 * 检查手机号是否在白名单中
 */
export const checkWhitelistApi = (sessionId, phone) => {
  return axios.get('/api/live/whitelist/check', {
    params: { sessionId, phone }
  })
}
