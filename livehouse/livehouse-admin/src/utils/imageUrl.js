import { getUploadConfigApi } from '../api/upload.js'

let uploadConfig = null

export const initUploadConfig = async () => {
  try {
    const response = await getUploadConfigApi()
    if (response && response.data && response.data.code === 200) {
      uploadConfig = response.data.data
    }
  } catch (error) {
    console.error('获取上传配置失败', error)
    uploadConfig = { storageType: 'local' }
  }
}

export const getStorageType = () => {
  return uploadConfig?.storageType || 'local'
}

export const getFullImageUrl = (url) => {
  if (!url) return ''
  
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  
  const storageType = getStorageType()
  
  if (storageType === 'local') {
    const baseUrl = window.location.origin
    if (url.startsWith('/')) {
      return baseUrl + url
    }
    return baseUrl + '/' + url
  }
  
  return url
}

export const formatImageUrls = (urls) => {
  if (!urls) return []
  if (Array.isArray(urls)) {
    return urls.map(url => getFullImageUrl(url))
  }
  try {
    const urlArray = JSON.parse(urls)
    return urlArray.map(url => getFullImageUrl(url))
  } catch (e) {
    return []
  }
}

export const getFirstImageUrl = (imagesStr) => {
  const urls = formatImageUrls(imagesStr)
  return urls.length > 0 ? urls[0] : ''
}