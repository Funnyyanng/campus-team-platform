import request from '@/utils/request'
import type { Result } from '@/types/api'
import type { PageResult } from './admin'

export interface Competition {
  id: number
  name: string
  level: string
  category?: string
  organizer?: string
  officialUrl?: string
  description?: string
  keywords?: string[]
  minTeamSize: number
  maxTeamSize: number
  registrationStart?: string
  registrationEnd?: string
  competitionStart?: string
  competitionEnd?: string
  deadline?: string
  prize?: string
  participationFee?: number
  requirements?: string
  contactInfo?: string
  status: string
  recruitingTeamCount?: number
  createTime: string
  updateTime: string
}

export interface CompetitionQueryParams {
  keyword?: string
  level?: string
  status?: string
  page: number
  pageSize: number
}

// 获取竞赛列表
export const getCompetitionList = (params: CompetitionQueryParams) => {
  return request<Result<PageResult<Competition>>>({
    url: '/competitions',
    method: 'get',
    params
  })
}

// 获取进行中的竞赛
export const getActiveCompetitions = () => {
  return request<Result<Competition[]>>({
    url: '/competitions/active',
    method: 'get'
  })
}

// 获取竞赛详情
export const getCompetitionById = (id: number) => {
  return request<Result<Competition>>({
    url: `/competition/${id}`,
    method: 'get'
  })
}

// 创建竞赛
export const createCompetition = (data: any) => {
  return request<Result<void>>({
    url: '/admin/competition',
    method: 'post',
    data
  })
}

// 更新竞赛
export const updateCompetition = (id: number, data: any) => {
  return request<Result<void>>({
    url: `/admin/competition/${id}`,
    method: 'put',
    data
  })
}

// 删除竞赛
export const deleteCompetition = (id: number) => {
  return request<Result<void>>({
    url: `/admin/competition/${id}`,
    method: 'delete'
  })
}
