import request from '@/utils/request.js'

export const getMessageListApi = (pageNum = 1, pageSize = 10, params = {}) => {
  const requestParams = { pageNum, pageSize }
  if (params.readStatus !== undefined && params.readStatus !== null) {
    requestParams.readStatus = params.readStatus
  }
  return request.get('/api/h5/message/list', {
    params: requestParams
  })
}

export const getUnreadCountApi = () => {
  return request.get('/api/h5/message/unread/count')
}

export const markMessageReadApi = (messageId) => {
  return request.put(`/api/h5/message/${messageId}/read`)
}
