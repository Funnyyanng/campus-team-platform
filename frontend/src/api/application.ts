import request from '@/utils/request'
import type { Result } from '@/types/api'

export interface Application {
  id: number
  teamId: number
  teamTitle: string
  competitionName: string
  applicantId: number
  applicantName: string
  applicantAvatar: string
  applicantCollege?: string
  applicantMajor?: string
  applicantGrade?: string
  applicantSkills?: string
  answers?: string
  message?: string
  status: string
  rejectReason?: string
  createTime: string
  updateTime: string
}

export interface ApplicationRequest {
  teamId: number
  answers?: string
  message?: string
}

// 申请加入队伍
export const applyToTeam = (data: ApplicationRequest) => {
  return request<Result<void>>({
    url: '/applications',
    method: 'post',
    data
  })
}

// 获取我的申请
export const getMyApplications = () => {
  return request<Result<Application[]>>({
    url: '/applications/my',
    method: 'get'
  })
}

// 获取队伍的申请列表
export const getTeamApplications = (teamId: number) => {
  return request<Result<Application[]>>({
    url: `/applications/team/${teamId}`,
    method: 'get'
  })
}

// 撤销申请
export const cancelApplication = (applicationId: number) => {
  return request<Result<void>>({
    url: `/applications/${applicationId}`,
    method: 'delete'
  })
}

// 通过申请
export const approveApplication = (applicationId: number) => {
  return request<Result<void>>({
    url: `/applications/${applicationId}/approve`,
    method: 'post'
  })
}

// 拒绝申请
export const rejectApplication = (applicationId: number, reason: string) => {
  return request<Result<void>>({
    url: `/applications/${applicationId}/reject`,
    method: 'post',
    params: { reason }
  })
}

// 批量拒绝申请（一键忽略）
export const batchRejectApplications = (teamId: number, reason: string) => {
  return request<Result<void>>({
    url: `/applications/team/${teamId}/batch-reject`,
    method: 'post',
    params: { reason }
  })
}

// 获取队伍成员列表
export const getTeamMembers = (teamId: number) => {
  return request<Result<Application[]>>({
    url: `/applications/team/${teamId}/members`,
    method: 'get'
  })
}
