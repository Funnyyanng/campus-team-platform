// 统一API类型定义文件

// ========== 通用响应 ==========
export interface Result<T = any> {
  code: number
  message: string
  data: T
}

// ========== 认证模块 ==========
export interface SendCodeRequest {
  email: string
}

export interface RegisterRequest {
  email: string
  code: string
  password: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface UserInfo {
  id: number
  email: string
  nickname: string
  avatar: string
  college?: string
  major?: string
  grade?: string
  wechat?: string
  qq?: string
  skills?: string[]
  role?: string
  allowInvite: boolean
}

export interface LoginResponse {
  token: string
  userInfo: UserInfo
}
