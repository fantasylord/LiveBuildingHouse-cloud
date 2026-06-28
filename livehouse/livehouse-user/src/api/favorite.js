import request from '@/utils/request.js'

export const toggleFavoriteApi = (params) => {
  return request.post('/api/h5/favorite/toggle', params)
}

export const getFavoriteListApi = (params) => {
  return request.get('/api/h5/favorite/list', { params })
}

export const checkFavoriteApi = (params) => {
  return request.get('/api/h5/favorite/check', { params })
}

export const deleteFavoriteApi = (id) => {
  return request.delete(`/api/h5/favorite/${id}`)
}
