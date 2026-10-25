import type { UserInfo } from '@/types/api'

/**
 * 检查用户信息是否完整
 * @param userInfo 用户信息
 * @returns 是否完整
 */
export const isProfileComplete = (userInfo: UserInfo | null): boolean => {
  if (!userInfo) return false
  
  return !!(
    userInfo.major &&
    userInfo.grade &&
    userInfo.skills &&
    userInfo.skills.length > 0
  )
}

/**
 * 获取未完善的信息项
 * @param userInfo 用户信息
 * @returns 未完善的信息项列表
 */
export const getIncompleteFields = (userInfo: UserInfo | null): string[] => {
  if (!userInfo) return ['所有信息']
  
  const incomplete: string[] = []
  
  if (!userInfo.major) incomplete.push('专业')
  if (!userInfo.grade) incomplete.push('年级')
  if (!userInfo.skills || userInfo.skills.length === 0) incomplete.push('技能标签')
  
  return incomplete
}
