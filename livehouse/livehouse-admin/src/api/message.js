import axios from '../utils/request.js'

export const getMessageListApi = (pageNum = 1, pageSize = 10, params = {}) => {
  return axios.get('/api/message/list', {
    params: { pageNum, pageSize, ...params }
  })
}

export const getMessageDetailApi = (id) => {
  return axios.get(`/api/message/${id}`)
}

export const addMessageApi = (message) => {
  return axios.post('/api/message', message)
}

export const updateMessageApi = (message) => {
  return axios.put('/api/message', message)
}

export const sendUnifiedMessageApi = (payload) => {
  return axios.post('/api/message/send', payload)
}

export const sendMessageApi = (id) => {
  return axios.put(`/api/message/${id}/send`)
}

export const batchSendMessageApi = (ids) => {
  return axios.put('/api/message/batch/send', ids)
}

export const broadcastByRoleApi = (roleId, message) => {
  return axios.post(`/api/message/broadcast/role/${roleId}`, message)
}

export const broadcastToUsersApi = (userIds, message) => {
  return axios.post('/api/message/broadcast/users', { userIds, message })
}

export const broadcastToAllApi = (message) => {
  return axios.post('/api/message/broadcast/all', message)
}

export const getReceivedMessagesApi = (receiverId, readStatus, pageNum = 1, pageSize = 10) => {
  return axios.get('/api/message/received', {
    params: { receiverId, readStatus, pageNum, pageSize }
  })
}

export const markAsReadApi = (messageId, receiverId) => {
  return axios.put(`/api/message/${messageId}/read/${receiverId}`)
}

export const getUnreadCountApi = (receiverId) => {
  return axios.get('/api/message/unread/count', {
    params: { receiverId }
  })
}

export const deleteMessageApi = (id) => {
  return axios.delete(`/api/message/${id}`)
}

export const getSmsRecordListApi = (pageNum = 1, pageSize = 10, params = {}) => {
  return axios.get('/api/sms/list', {
    params: { pageNum, pageSize, ...params }
  })
}

export const getSmsRecordDetailApi = (id) => {
  return axios.get(`/api/sms/${id}`)
}

export const sendSmsApi = (phone, templateCode, content) => {
  return axios.post('/api/sms/send', null, {
    params: { phone, templateCode, content }
  })
}

export const batchSendSmsApi = (phones, templateCode, content) => {
  return axios.post('/api/sms/batch/send', phones, {
    params: { templateCode, content }
  })
}

export const deleteSmsRecordApi = (id) => {
  return axios.delete(`/api/sms/${id}`)
}

export const getMessageTemplateListApi = (pageNum = 1, pageSize = 10, params = {}) => {
  return axios.get('/api/message/template/list', {
    params: { pageNum, pageSize, ...params }
  })
}

export const addMessageTemplateApi = (template) => {
  return axios.post('/api/message/template', template)
}

export const updateMessageTemplateApi = (template) => {
  return axios.put('/api/message/template', template)
}

export const changeMessageTemplateStatusApi = (id, status) => {
  return axios.put(`/api/message/template/${id}/status`, null, {
    params: { status }
  })
}

export const deleteMessageTemplateApi = (id) => {
  return axios.delete(`/api/message/template/${id}`)
}
