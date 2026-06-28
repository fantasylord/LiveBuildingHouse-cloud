import axios from '../utils/request.js'

// 创建用于文件上传的axios实例（设置更长超时时间）
const uploadRequest = axios.create({
  baseURL: '',
  timeout: 120000 // 2分钟
})

// 上传请求不需要鉴权
uploadRequest.interceptors.request.use(
  (config) => {
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

uploadRequest.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 通用文件上传
 * @param {File} file 文件对象
 * @param {string} bizType 业务类型（image/video/vr/cover）
 * @returns Promise
 */
export const uploadFileApi = (file, bizType = 'common') => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('bizType', bizType)
  return uploadRequest.post('/api/common/upload/file', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 图片上传
 * @param {File} file 文件对象
 * @param {string} bizType 业务类型（vr/cover/avatar等）
 * @returns Promise
 */
export const uploadImageApi = (file, bizType = 'image') => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('bizType', bizType)
  return uploadRequest.post('/api/common/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 视频上传
 * @param {File} file 文件对象
 * @param {string} bizType 业务类型
 * @returns Promise
 */
export const uploadVideoApi = (file, bizType = 'video') => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('bizType', bizType)
  return uploadRequest.post('/api/common/upload/video', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除文件
 * @param {string} url 文件URL
 * @returns Promise
 */
export const deleteFileApi = (url) => {
  return axios.delete('/api/common/upload/file', {
    params: { url }
  })
}

/**
 * 获取上传配置信息
 */
export const getUploadConfigApi = () => {
  return axios.get('/api/common/upload/config')
}
