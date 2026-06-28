import request from '@/utils/request.js'

export const getHomeIndexApi = () => {
  return request.get('/api/h5/home/index')
}

export const getHomeStatsApi = () => {
  return request.get('/api/h5/home/stats')
}