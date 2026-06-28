import request from '@/utils/request.js'

export const getHouseListApi = (params = {}) => {
  return request.get('/api/h5/house/list', { params })
}

export const getHouseDetailApi = (id) => {
  return request.get(`/api/h5/house/detail/${id}`)
}

export const getHotHouseApi = () => {
  return request.get('/api/h5/house/hot')
}

export const getHouseCitiesApi = () => {
  return request.get('/api/h5/house/cities')
}