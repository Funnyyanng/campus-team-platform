<template>
  <div class="team-applications">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <el-button @click="$router.back()" :icon="ArrowLeft">返回</el-button>
        <h1 class="page-title">申请管理</h1>
        <div class="header-actions">
          <el-button 
            type="danger" 
            size="small"
            :disabled="pendingApplications.length === 0"
            @click="handleBatchReject"
          >
            一键忽略全部
          </el-button>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <div class="container" v-loading="loading">
      <div class="applications-list">
        <div 
          v-for="app in applications" 
          :key="app.id"
          class="application-card"
        >
          <div class="card-header">
            <div class="applicant-info">
              <el-avatar :src="app.applicantAvatar" :size="50" />
              <div class="applicant-details">
                <div class="applicant-name">{{ app.applicantName }}</div>
                <div class="applicant-meta">
                  <span v-if="app.applicantCollege">{{ app.applicantCollege }}</span>
                  <span v-if="app.applicantMajor">{{ app.applicantMajor }}</span>
                  <span v-if="app.applicantGrade">{{ app.applicantGrade }}</span>
                </div>
              </div>
            </div>
            <el-tag 
              :type="getStatusType(app.status)" 
              size="large"
            >
              {{ getStatusText(app.status) }}
            </el-tag>
          </div>

          <div class="card-body">
            <div class="skills" v-if="app.applicantSkills">
              <div class="label">技能标签：</div>
              <el-tag 
                v-for="skill in app.applicantSkills.split(',')" 
                :key="skill"
                size="small"
                style="margin-right: 8px"
              >
                {{ skill }}
              </el-tag>
            </div>

            <div class="answers" v-if="app.answers">
              <div class="label">问题回答：</div>
              <div class="content">{{ app.answers }}</div>
            </div>

            <div class="message" v-if="app.message">
              <div class="label">申请留言：</div>
              <div class="content">{{ app.message }}</div>
            </div>

            <div class="reject-reason" v-if="app.status === 'REJECTED' && app.rejectReason">
              <el-alert
                :title="`拒绝理由：${app.rejectReason}`"
                type="error"
                :closable="false"
              />
            </div>
          </div>

          <div class="card-footer">
            <span class="apply-time">{{ formatTime(app.createTime) }}</span>
            <div class="actions" v-if="app.status === 'PENDING'">
              <el-button 
                type="danger" 
                size="small"
                :disabled="submitting"
                @click="handleReject(app)"
              >
                拒绝
              </el-button>
              <el-button 
                type="primary" 
                size="small"
                :disabled="submitting"
                @click="handleApprove(app)"
              >
                通过
              </el-button>
            </div>
          </div>
        </div>

        <el-empty 
          v-if="applications.length === 0" 
          description="暂无申请"
        />
      </div>
    </div>

    <!-- 拒绝理由对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝申请"
      width="500px"
    >
      <el-form label-width="80px">
        <el-form-item label="拒绝理由" required>
          <el-select v-model="rejectReason" placeholder="请选择拒绝理由">
            <el-option label="技能不匹配" value="技能不匹配" />
            <el-option label="经验不足" value="经验不足" />
            <el-option label="时间冲突" value="时间冲突" />
            <el-option label="队伍已满" value="队伍已满" />
            <el-option label="其他原因" value="其他原因" />
          </el-select>
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input
            v-model="rejectNote"
            type="textarea"
            :rows="3"
            placeholder="可选：补充说明（选填）"
            maxlength="200"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { 
  getTeamApplications, 
  approveApplication, 
  rejectApplication,
  batchRejectApplications,
  type Application 
} from '@/api/application'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const applications = ref<Application[]>([])
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const rejectNote = ref('')
const submitting = ref(false)
const currentApplication = ref<Application | null>(null)
const isBatchReject = ref(false)

const pendingApplications = computed(() => {
  return applications.value.filter(app => app.status === 'PENDING')
})

// 加载申请列表
const loadApplications = async () => {
  loading.value = true
  try {
    const teamId = Number(route.params.teamId)
    const res = await getTeamApplications(teamId)
    
    // 排序：待审核的申请优先显示
    applications.value = res.data.sort((a, b) => {
      // 定义状态优先级：PENDING > APPROVED > REJECTED > REMOVED > QUIT
      const statusPriority: Record<string, number> = {
        'PENDING': 1,
        'APPROVED': 2,
        'REJECTED': 3,
        'REMOVED': 4,
        'QUIT': 5
      }
      
      const priorityA = statusPriority[a.status] || 999
      const priorityB = statusPriority[b.status] || 999
      
      // 如果状态优先级相同，按创建时间倒序（最新的在前）
      if (priorityA === priorityB) {
        return new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
      }
      
      return priorityA - priorityB
    })
  } catch (error: any) {
    console.error('加载申请列表失败:', error)
    ElMessage.error(error.response?.data?.message || '加载申请列表失败')
    router.back()
  } finally {
    loading.value = false
  }
}

const handleApprove = async (app: Application) => {
  // 防止重复提交
  if (submitting.value) {
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定通过 ${app.applicantName} 的申请吗？`,
      '通过申请',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )

    submitting.value = true
    try {
      await approveApplication(app.id)
      ElMessage.success('已通过申请')
      await loadApplications()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    } finally {
      submitting.value = false
    }
  } catch {
    // 用户取消操作
  }
}

const handleReject = (app: Application) => {
  currentApplication.value = app
  isBatchReject.value = false
  rejectReason.value = ''
  rejectNote.value = ''
  rejectDialogVisible.value = true
}

const handleBatchReject = () => {
  ElMessageBox.confirm(
    `确定要拒绝所有待审核的申请吗？共 ${pendingApplications.value.length} 个申请`,
    '一键忽略',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    currentApplication.value = null
    isBatchReject.value = true
    rejectReason.value = ''
    rejectNote.value = ''
    rejectDialogVisible.value = true
  }).catch(() => {})
}

const confirmReject = async () => {
  if (!rejectReason.value) {
    ElMessage.warning('请选择拒绝理由')
    return
  }

  // 防止重复提交
  if (submitting.value) {
    return
  }

  const reason = rejectNote.value 
    ? `${rejectReason.value}：${rejectNote.value}` 
    : rejectReason.value

  submitting.value = true
  try {
    if (isBatchReject.value) {
      const teamId = Number(route.params.teamId)
      await batchRejectApplications(teamId, reason)
      ElMessage.success('已拒绝所有申请')
    } else if (currentApplication.value) {
      await rejectApplication(currentApplication.value.id, reason)
      ElMessage.success('已拒绝申请')
    }
    rejectDialogVisible.value = false
    await loadApplications()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'PENDING':
      return 'warning'
    case 'APPROVED':
      return 'success'
    case 'REJECTED':
      return 'danger'
    case 'REMOVED':
      return 'info'
    case 'QUIT':
      return 'info'
    default:
      return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'PENDING':
      return '待审核'
    case 'APPROVED':
      return '已通过'
    case 'REJECTED':
      return '已拒绝'
    case 'REMOVED':
      return '已移除'
    case 'QUIT':
      return '已退出'
    default:
      return status
  }
}

const formatTime = (dateStr: string) => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

onMounted(() => {
  loadApplications()
})
</script>

<style scoped lang="scss">
.team-applications {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;

  .header-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 16px 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .page-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin: 0;
    flex: 1;
    text-align: center;
  }
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.applications-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.application-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e4e7ed;

    .applicant-info {
      display: flex;
      align-items: center;
      gap: 16px;

      .applicant-details {
        .applicant-name {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 4px;
        }

        .applicant-meta {
          font-size: 13px;
          color: #909399;
          
          span {
            margin-right: 12px;
          }
        }
      }
    }
  }

  .card-body {
    margin-bottom: 16px;

    .skills, .answers, .message {
      margin-bottom: 16px;

      .label {
        font-size: 13px;
        color: #909399;
        margin-bottom: 8px;
      }

      .content {
        font-size: 14px;
        color: #606266;
        line-height: 1.6;
        padding: 12px;
        background: #f5f7fa;
        border-radius: 8px;
        white-space: pre-wrap;
      }
    }

    .answers {
      .content {
        background: #fff7e6;
        border-left: 3px solid #faad14;
      }
    }

    .reject-reason {
      margin-top: 12px;
    }
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 16px;
    border-top: 1px solid #e4e7ed;

    .apply-time {
      font-size: 13px;
      color: #909399;
    }

    .actions {
      display: flex;
      gap: 8px;
    }
  }
}

@media (max-width: 768px) {
  .container {
    padding: 16px;
  }

  .application-card {
    .card-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }

    .card-footer {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;
    }
  }
}
</style>
