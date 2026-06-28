import axios from '../utils/request.js'

export const getCustomerListApi = (pageNum = 1, pageSize = 10, params = {}) => {
  return axios.get('/api/customer/list', {
    params: { pageNum, pageSize, ...params }
  })
}

export const addCustomerApi = (customer) => {
  return axios.post('/api/customer', customer)
}

export const updateCustomerApi = (customer) => {
  return axios.put('/api/customer', customer)
}

export const deleteCustomerApi = (id) => {
  return axios.delete(`/api/customer/${id}`)
}

export const assignCustomerApi = (id, consultantId) => {
  return axios.put(`/api/customer/${id}/assign`, null, {
    params: { consultantId }
  })
}

export const getReserveListApi = (pageNum = 1, pageSize = 10, params = {}) => {
  return axios.get('/api/reserve/list', {
    params: { pageNum, pageSize, ...params }
  })
}

export const addReserveApi = (reserve) => {
  return axios.post('/api/reserve', reserve)
}

export const updateReserveApi = (reserve) => {
  return axios.put('/api/reserve', reserve)
}

export const changeReserveStatusApi = (id, visitStatus) => {
  return axios.put(`/api/reserve/${id}/status`, null, {
    params: { visitStatus }
  })
}

export const deleteReserveApi = (id) => {
  return axios.delete(`/api/reserve/${id}`)
}

export const getFollowListApi = (pageNum = 1, pageSize = 10, params = {}) => {
  return axios.get('/api/customer/follow/list', {
    params: { pageNum, pageSize, ...params }
  })
}

export const addFollowApi = (follow) => {
  return axios.post('/api/customer/follow', follow)
}

export const updateFollowApi = (follow) => {
  return axios.put('/api/customer/follow', follow)
}

export const deleteFollowApi = (id) => {
  return axios.delete(`/api/customer/follow/${id}`)
}
