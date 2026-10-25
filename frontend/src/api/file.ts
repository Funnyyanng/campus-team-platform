import request from '@/utils/request'
import type { Result } from '@/types/api'

export interface UploadResponse {
  url: string
  filename: string
}

// 上传头像
export const uploadAvatar = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request<Result<UploadResponse>>({
    url: '/file/upload/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
