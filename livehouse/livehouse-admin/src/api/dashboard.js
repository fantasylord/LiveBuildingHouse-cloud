import axios from '../utils/request.js'

export const getDashboardDataApi = () => {
  return axios.get('/api/statistics/dashboard')
}

export const getLiveStatisticsApi = (params = {}) => {
  return axios.get('/api/statistics/live/data', { params })
}

export const exportStatisticsApi = (params = {}) => {
  return axios.get('/api/statistics/export', {
    params,
    responseType: 'blob'
  })
}
