import request from '@/utils/request'
import type { Result, UserInfo } from '@/types/api'

export interface UserSearchParams {
  major?: string
  grade?: string
  skills?: string
}

export interface UserSearchResult {
  id: number
  nickname: string
  avatar: string
  college: string
  major: string
  grade: string
  skills: string
  allowInvite: boolean
}

// 获取用户信息
export const getUserProfile = () => {
  return request<Result<UserInfo>>({
    url: '/user/profile',
    method: 'get'
  })
}

// 更新用户信息
export const updateUserProfile = (data: any) => {
  return request<Result<void>>({
    url: '/user/profile',
    method: 'put',
    data
  })
}

// 搜索用户（人才库）
export const searchUsers = (params: UserSearchParams) => {
  return request<Result<UserSearchResult[]>>({
    url: '/user/search',
    method: 'get',
    params
  })
}
