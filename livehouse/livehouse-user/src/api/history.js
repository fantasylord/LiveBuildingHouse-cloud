import request from '@/utils/request.js'

export const recordHistoryApi = (params) => {
  return request.post('/api/h5/history', params)
}

export const getHistoryListApi = (params) => {
  return request.get('/api/h5/history/list', { params })
}

export const deleteHistoryApi = (id) => {
  return request.delete(`/api/h5/history/${id}`)
}

export const clearHistoryApi = () => {
  return request.delete('/api/h5/history/clear')
}
