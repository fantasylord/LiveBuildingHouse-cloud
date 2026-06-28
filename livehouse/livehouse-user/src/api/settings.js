import request from '@/utils/request.js'

export const getSettingsApi = () => {
  return request.get('/api/h5/settings')
}

export const updateSettingsApi = (params) => {
  return request.put('/api/h5/settings', params)
}
