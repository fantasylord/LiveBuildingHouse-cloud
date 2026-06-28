import request from '@/utils/request.js'

export const createReserveApi = (params) => {
  return request.post('/api/h5/reserve', params)
}

export const checkReserveApi = (buildingId, phone) => {
  return request.get(`/api/h5/reserve/check/${buildingId}/${phone}`)
}

export const getMyReserveListApi = (params) => {
  return request.get('/api/h5/reserve/my', { params })
}

export const cancelReserveApi = (id) => {
  return request.put(`/api/h5/reserve/${id}/cancel`)
}
