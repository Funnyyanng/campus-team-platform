import request from '@/utils/request'
import type { 
  SendCodeRequest, 
  RegisterRequest, 
  LoginRequest, 
  LoginResponse,
  Result 
} from '@/types/api'

// 发送验证码
export const sendCode = (data: SendCodeRequest) => {
  return request<Result<void>>({
    url: '/auth/send-code',
    method: 'post',
    data
  })
}

// 用户注册
export const register = (data: RegisterRequest) => {
  return request<Result<void>>({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 用户登录
export const login = (data: LoginRequest) => {
  return request<Result<LoginResponse>>({
    url: '/auth/login',
    method: 'post',
    data
  })
}
