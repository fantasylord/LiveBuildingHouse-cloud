import request from '@/utils/request.js'

export const loginApi = (params) => {
  return request.post('/api/h5/user/login', params)
}

export const registerApi = (params) => {
  return request.post('/api/h5/user/register', params)
}

export const getUserInfoApi = () => {
  return request.get('/api/h5/user/info')
}

export const updateUserInfoApi = (params) => {
  return request.put('/api/h5/user/info', params)
}

export const logoutApi = () => {
  return request.post('/api/h5/user/logout')
}

export const sendCodeApi = (params) => {
  return request.post('/api/h5/user/sendCode', params)
}
