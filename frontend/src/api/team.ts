import request from '@/utils/request'
import type { Result } from '@/types/api'

export interface Team {
  id: number
  type: 'COMPETITION' | 'PROJECT'
  competitionId?: number
  competitionName?: string
  projectName?: string
  projectType?: string
  projectDuration?: string
  leaderId: number
  leaderName: string
  leaderAvatar: string
  title: string
  description?: string
  targetSize: number
  currentSize: number
  requiredSkills?: string
  questions?: string
  status: string
  pendingApplicationCount?: number
  createTime: string
  updateTime: string
}

export interface TeamCreateRequest {
  type: 'COMPETITION' | 'PROJECT'
  competitionId?: number
  projectName?: string
  projectType?: string
  projectDuration?: string
  title: string
  description?: string
  targetSize: number
  requiredSkills?: string
  questions?: string
}

export interface TeamUpdateRequest {
  title: string
  description?: string
  targetSize: number
  requiredSkills?: string
  questions?: string
}

export interface RefreshCheckResponse {
  canRefresh: boolean
  reason: string
  nextRefreshTime?: string
  refreshDeadline?: string
}

// 创建队伍
export const createTeam = (data: TeamCreateRequest) => {
  return request<Result<void>>({
    url: '/teams',
    method: 'post',
    data
  })
}

// 获取所有招募中的队伍
export const getAllRecruitingTeams = () => {
  return request<Result<Team[]>>({
    url: '/teams/recruiting',
    method: 'get'
  })
}

// 获取竞赛的招募队伍
export const getTeamsByCompetition = (competitionId: number) => {
  return request<Result<Team[]>>({
    url: `/teams/competition/${competitionId}`,
    method: 'get'
  })
}

// 获取队伍详情
export const getTeamById = (id: number) => {
  return request<Result<Team>>({
    url: `/teams/${id}`,
    method: 'get'
  })
}

// 获取我的队伍
export const getMyTeams = () => {
  return request<Result<Team[]>>({
    url: '/teams/my',
    method: 'get'
  })
}

// 关闭招募
export const closeRecruitment = (teamId: number) => {
  return request<Result<void>>({
    url: `/teams/${teamId}/close`,
    method: 'post'
  })
}

// 移除成员
export const removeMember = (teamId: number, memberId: number) => {
  return request<Result<void>>({
    url: `/teams/${teamId}/members/${memberId}`,
    method: 'delete'
  })
}

// 删除队伍
export const deleteTeam = (teamId: number) => {
  return request<Result<void>>({
    url: `/teams/${teamId}`,
    method: 'delete'
  })
}

// 转让队长权限
export const transferLeadership = (teamId: number, newLeaderId: number) => {
  return request<Result<void>>({
    url: `/teams/${teamId}/transfer`,
    method: 'post',
    params: { newLeaderId }
  })
}

// 退出队伍
export const quitTeam = (teamId: number, reason: string) => {
  return request<Result<void>>({
    url: `/teams/${teamId}/quit`,
    method: 'post',
    params: { reason }
  })
}

// 获取所有课题队伍
export const getProjectTeams = () => {
  return request<Result<Team[]>>({
    url: '/teams/projects',
    method: 'get'
  })
}

// 获取招募中的课题队伍
export const getRecruitingProjectTeams = () => {
  return request<Result<Team[]>>({
    url: '/teams/projects/recruiting',
    method: 'get'
  })
}

// 检查是否可以调整招募
export const checkCanRefresh = (teamId: number) => {
  return request<Result<RefreshCheckResponse>>({
    url: `/teams/${teamId}/can-refresh`,
    method: 'get'
  })
}

// 调整招募信息
export const refreshTeam = (teamId: number, data: TeamUpdateRequest) => {
  return request<Result<void>>({
    url: `/teams/${teamId}/refresh`,
    method: 'put',
    data
  })
}

// 邀请用户加入队伍
export const inviteUser = (teamId: number, inviteeId: number, message?: string) => {
  return request<Result<void>>({
    url: `/teams/${teamId}/invite`,
    method: 'post',
    data: {
      inviteeId,
      message
    }
  })
}
