import request from '@/utils/request.js'

export const getVrDetailApi = (id) => {
  return request.get(`/api/h5/vr/detail/${id}`)
}

export const getVrListApi = (params = {}) => {
  return request.get('/api/h5/vr/list', { params })
}