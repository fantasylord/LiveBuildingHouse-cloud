import request from '@/utils/request.js'

export const getLiveListApi = (pageNum = 1, pageSize = 50, params = {}) => {
  return request.get('/api/h5/live/list', {
    params: { pageNum, pageSize, ...params }
  })
}

export const getLiveDetailApi = (id) => {
  return request.get(`/api/h5/live/${id}`)
}

export const checkLiveAccessApi = (id, payload) => {
  return request.post(`/api/h5/live/${id}/access`, payload)
}

export const recordLiveViewApi = (id) => {
  return request.post(`/api/h5/live/${id}/view`)
}

export const reserveLiveApi = (id, payload) => {
  return request.post(`/api/h5/live/${id}/reserve`, payload)
}

export const getLiveInteractionApi = (id, params = {}) => {
  return request.get(`/api/h5/live/${id}/interaction`, { params })
}

export const toggleLiveLikeApi = (id, payload) => {
  return request.post(`/api/h5/live/${id}/like`, payload)
}

export const sendLiveDanmuApi = (id, payload) => {
  return request.post(`/api/h5/live/${id}/danmu`, payload)
}
