<template>
  <div class="message-center">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <el-icon class="title-icon"><Bell /></el-icon>
            消息中心
          </h1>
          <p class="page-subtitle">查看和管理你的所有消息通知</p>
        </div>
        <div class="header-right">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0">
            <el-button type="primary" @click="handleMarkAllRead" :icon="Check">
              全部已读
            </el-button>
          </el-badge>
        </div>
      </div>
    </div>

    <!-- 消息列表卡片 -->
    <el-card class="message-card" shadow="never">
      <!-- 标签页 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部消息" name="all"></el-tab-pane>
        <el-tab-pane label="系统消息" name="SYSTEM"></el-tab-pane>
        <el-tab-pane label="队伍邀请" name="INVITE"></el-tab-pane>
        <el-tab-pane label="申请通知" name="APPLICATION"></el-tab-pane>
        <el-tab-pane label="队伍通知" name="TEAM"></el-tab-pane>
      </el-tabs>

      <!-- 消息列表 -->
      <div class="message-list" v-loading="loading">
        <el-empty 
          v-if="messages.length === 0" 
          description="暂无消息" 
          :image-size="160"
        />
        
        <div
          v-for="message in messages"
          :key="message.id"
          class="message-item"
          :class="{ unread: !message.isRead }"
          @click="handleMarkRead(message)"
        >
          <!-- 消息头部 -->
          <div class="message-header">
            <div class="message-info">
              <el-avatar
                v-if="message.senderAvatar"
                :src="message.senderAvatar"
                :size="48"
              />
              <el-avatar v-else :size="48">
                <el-icon><Bell /></el-icon>
              </el-avatar>

              <div class="message-meta">
                <div class="message-title">
                  <span class="title-text">{{ message.title }}</span>
                  <el-tag v-if="!message.isRead" type="danger" size="small">
                    未读
                  </el-tag>
                </div>
                <div class="message-time">
                  <el-icon><Clock /></el-icon>
                  {{ formatTime(message.createTime) }}
                </div>
              </div>
            </div>

            <el-button
              type="danger"
              size="small"
              text
              :icon="Delete"
              @click.stop="handleDelete(message.id)"
            >
              删除
            </el-button>
          </div>

          <!-- 消息内容 -->
          <div class="message-content">
            {{ message.content }}
          </div>

          <!-- 相关信息 -->
          <div v-if="message.teamTitle" class="message-related">
            <el-tag effect="plain">
              {{ message.teamTitle }}
            </el-tag>
            <el-tag v-if="message.competitionName" type="info" effect="plain">
              {{ message.competitionName }}
            </el-tag>
          </div>

          <!-- 邀请操作按钮 -->
          <div
            v-if="message.type === 'INVITE' && message.inviteStatus === 'PENDING'"
            class="message-actions"
          >
            <el-button type="success" size="default" @click.stop="handleAccept(message.id)">
              接受邀请
            </el-button>
            <el-button size="default" @click.stop="handleReject(message.id)">
              拒绝邀请
            </el-button>
          </div>

          <!-- 邀请状态 -->
          <div
            v-if="message.type === 'INVITE' && message.inviteStatus !== 'PENDING'"
            class="message-status"
          >
            <el-tag v-if="message.inviteStatus === 'ACCEPTED'" type="success">
              已接受
            </el-tag>
            <el-tag v-if="message.inviteStatus === 'REJECTED'" type="info">
              已拒绝
            </el-tag>
            <el-tag v-if="message.inviteStatus === 'EXPIRED'" type="warning">
              已过期
            </el-tag>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bell,
  Check,
  Close,
  Delete,
  Clock,
  CircleCheck,
  CircleClose,
  List,
  UserFilled,
  Document,
  Trophy,
  Medal,
  ChatDotRound
} from '@element-plus/icons-vue'
import {
  getMyMessages,
  markAllAsRead,
  markAsRead,
  deleteMessage,
  acceptInvite,
  rejectInvite,
  type Message
} from '@/api/message'

const activeTab = ref('all')
const messages = ref<Message[]>([])
const loading = ref(false)

// 计算未读消息数量
const unreadCount = computed(() => {
  return messages.value.filter(m => !m.isRead).length
})

const loadMessages = async () => {
  loading.value = true
  try {
    const type = activeTab.value === 'all' ? undefined : activeTab.value
    const res = await getMyMessages(type)
    messages.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载消息失败')
  } finally {
    loading.value = false
  }
}

// 标记单条消息为已读
const handleMarkRead = async (message: Message) => {
  if (message.isRead) return
  
  try {
    await markAsRead(message.id)
    message.isRead = true
  } catch (error: any) {
    // 静默失败
  }
}

const handleTabChange = () => {
  loadMessages()
}

const handleMarkAllRead = async () => {
  try {
    await markAllAsRead()
    ElMessage.success('已全部标记为已读')
    loadMessages()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteMessage(id)
    ElMessage.success('删除成功')
    loadMessages()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleAccept = async (id: number) => {
  try {
    await acceptInvite(id)
    ElMessage.success('已接受邀请，欢迎加入队伍！')
    loadMessages()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleReject = async (id: number) => {
  try {
    await rejectInvite(id)
    ElMessage.success('已拒绝邀请')
    loadMessages()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  } else if (diff < 604800000) {
    return `${Math.floor(diff / 86400000)}天前`
  } else {
    return date.toLocaleDateString()
  }
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped lang="scss">
.message-center {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

// 页面标题
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 20px;
  }

  .header-left {
    flex: 1;
  }

  .page-title {
    font-size: 28px;
    font-weight: 600;
    margin: 0 0 8px 0;
    display: flex;
    align-items: center;
    gap: 12px;

    .title-icon {
      font-size: 32px;
    }
  }

  .page-subtitle {
    font-size: 14px;
    opacity: 0.9;
    margin: 0;
  }

  .header-right {
    :deep(.el-badge__content) {
      font-weight: 600;
    }
  }
}

// 消息卡片
.message-card {
  margin-bottom: 24px;
  border-radius: 12px;
  border: none;

  :deep(.el-card__body) {
    padding: 0;
  }

  :deep(.el-tabs__header) {
    margin: 0;
    padding: 20px 24px 0;
  }

  :deep(.el-tabs__item) {
    font-size: 15px;
    font-weight: 500;
  }
}

// 消息列表
.message-list {
  padding: 24px;
  min-height: 400px;
}

// 消息项
.message-item {
  padding: 20px;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  margin-bottom: 16px;
  transition: all 0.3s;
  cursor: pointer;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    border-color: #667eea;
  }

  &.unread {
    background: #f0f9ff;
    border-color: #409eff;
  }
}

// 消息头部
.message-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.message-info {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex: 1;
}

// 消息元信息
.message-meta {
  flex: 1;
  min-width: 0;
}

.message-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  flex-wrap: wrap;

  .title-text {
    font-weight: 600;
    font-size: 16px;
    color: #303133;
    flex: 1;
    min-width: 0;
  }
}

.message-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;

  .el-icon {
    font-size: 14px;
  }
}

// 消息内容
.message-content {
  margin: 16px 0;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 14px;
}

// 相关信息
.message-related {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 16px 0;

  .el-tag {
    border-radius: 4px;
  }
}

// 操作按钮
.message-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #e4e7ed;

  .el-button {
    flex: 1;
    height: 40px;
    border-radius: 8px;
    font-weight: 500;
  }
}

// 消息状态
.message-status {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #e4e7ed;
}

// 响应式
@media (max-width: 768px) {
  .message-center {
    padding: 12px;
  }

  .page-header {
    padding: 20px;

    .page-title {
      font-size: 22px;
    }

    .header-content {
      flex-direction: column;
      align-items: flex-start;
    }
  }

  .message-card {
    :deep(.el-card__body) {
      padding: 16px;
    }
  }
}
</style>
