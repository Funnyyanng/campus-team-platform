import request from '@/utils/request'
import type { Result } from '@/types/api'

export interface UserManage {
  id: number
  email: string
  nickname: string
  avatar: string
  college?: string
  major?: string
  grade?: string
  role: string
  status: string
  createTime: string
  updateTime: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

export interface UserQueryParams {
  keyword?: string
  status?: string
  role?: string
  page: number
  pageSize: number
}

// 获取用户列表
export const getUserList = (params: UserQueryParams) => {
  return request<Result<PageResult<UserManage>>>({
    url: '/admin/users',
    method: 'get',
    params
  })
}

// 封禁/解封用户
export const banUser = (userId: number, data: { status: string; reason?: string }) => {
  return request<Result<void>>({
    url: `/admin/user/${userId}/ban`,
    method: 'put',
    data
  })
}

// 删除用户
export const deleteUser = (userId: number) => {
  return request<Result<void>>({
    url: `/admin/user/${userId}`,
    method: 'delete'
  })
}

// 发布公告
export const publishAnnouncement = (data: { title: string; content: string }) => {
  return request<Result<void>>({
    url: '/admin/announcement',
    method: 'post',
    data
  })
}

export interface AnnouncementHistory {
  id: number
  title: string
  content: string
  publisherId: number
  publisherName: string
  recipientCount: number
  createTime: string
}

// 获取公告历史记录
export const getAnnouncementHistory = () => {
  return request<Result<AnnouncementHistory[]>>({
    url: '/admin/announcements',
    method: 'get'
  })
}

export interface DashboardStats {
  totalUsers: number
  activeTeams: number
  activeCompetitions: number
}

// 获取数据概览统计
export const getDashboardStats = () => {
  return request<Result<DashboardStats>>({
    url: '/admin/dashboard/stats',
    method: 'get'
  })
}

export interface DashboardCharts {
  teamStatus: {
    recruiting: number
    full: number
    closed: number
  }
  competitionLevel: {
    national: number
    provincial: number
    school: number
  }
  skillTag: {
    items: Array<{
      name: string
      count: number
    }>
  }
  teamType: {
    competition: number
    project: number
  }
}

// 获取图表数据
export const getDashboardCharts = () => {
  return request<Result<DashboardCharts>>({
    url: '/admin/dashboard/charts',
    method: 'get'
  })
}

// 获取所有队伍（管理后台）
export const getAllTeams = () => {
  return request<Result<any[]>>({
    url: '/admin/teams',
    method: 'get'
  })
}

// 删除队伍（管理后台）
export const deleteTeamByAdmin = (teamId: number, reason: string) => {
  return request<Result<void>>({
    url: `/admin/team/${teamId}`,
    method: 'delete',
    data: { reason }
  })
}
