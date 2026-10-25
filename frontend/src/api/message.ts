import request from '@/utils/request'

export interface Message {
  id: number
  type: string
  title: string
  content: string
  senderId?: number
  senderName?: string
  senderAvatar?: string
  receiverId: number
  relatedId?: number
  isRead: boolean
  inviteStatus?: string
  createTime: string
  updateTime: string
  teamTitle?: string
  competitionName?: string
}

/**
 * 获取我的消息
 */
export function getMyMessages(type?: string) {
  return request<Message[]>({
    url: '/messages',
    method: 'get',
    params: { type }
  })
}

/**
 * 获取未读消息数量
 */
export function getUnreadCount() {
  return request<{ count: number }>({
    url: '/messages/unread-count',
    method: 'get'
  })
}

/**
 * 标记消息为已读
 */
export function markAsRead(id: number) {
  return request({
    url: `/messages/${id}/read`,
    method: 'put'
  })
}

/**
 * 标记所有消息为已读
 */
export function markAllAsRead() {
  return request({
    url: '/messages/read-all',
    method: 'put'
  })
}

/**
 * 删除消息
 */
export function deleteMessage(id: number) {
  return request({
    url: `/messages/${id}`,
    method: 'delete'
  })
}

/**
 * 接受邀请
 */
export function acceptInvite(id: number) {
  return request({
    url: `/messages/${id}/accept`,
    method: 'post'
  })
}

/**
 * 拒绝邀请
 */
export function rejectInvite(id: number, reason?: string) {
  return request({
    url: `/messages/${id}/reject`,
    method: 'post',
    data: { reason }
  })
}
