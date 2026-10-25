<template>
  <div class="my-applications">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <el-button @click="$router.back()" :icon="ArrowLeft">返回</el-button>
        <h1 class="page-title">我的申请</h1>
        <div class="header-actions">
          <el-dropdown @command="handleCommand">
            <div class="user-dropdown">
              <el-avatar :src="userStore.userInfo?.avatar" :size="32" />
              <span class="user-name">{{ userStore.userInfo?.nickname }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item command="home">
                  <el-icon><HomeFilled /></el-icon>
                  返回首页
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <div class="container" v-loading="loading">
      <!-- 标签页 -->
      <el-tabs v-model="activeTab" class="tabs">
        <el-tab-pane label="待审核" name="pending">
          <template #label>
            <span>待审核 <el-badge :value="pendingCount" v-if="pendingCount > 0" /></span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="已通过" name="approved"></el-tab-pane>
        <el-tab-pane label="已拒绝" name="rejected"></el-tab-pane>
        <el-tab-pane label="已退出" name="quit"></el-tab-pane>
      </el-tabs>

      <div class="applications-list">
        <div 
          v-for="app in filteredApplications" 
          :key="app.id"
          class="application-card"
        >
          <div class="card-header">
            <div class="header-left">
              <h3 class="team-title">{{ app.teamTitle }}</h3>
              <el-tag 
                :type="getStatusType(app.status)" 
                size="small"
              >
                {{ getStatusText(app.status) }}
              </el-tag>
            </div>
            <div class="header-right">
              <el-button 
                v-if="app.status === 'PENDING'"
                size="small" 
                type="danger"
                @click="handleCancel(app)"
              >
                撤销申请
              </el-button>
              <el-button 
                v-if="app.status === 'APPROVED'"
                size="small" 
                type="warning"
                @click="handleQuit(app)"
              >
                退出队伍
              </el-button>
            </div>
          </div>

          <div class="card-body">
            <div class="info-row">
              <el-icon class="info-icon"><Trophy /></el-icon>
              <span class="info-label">竞赛：</span>
              <span class="info-value">{{ app.competitionName }}</span>
            </div>

            <div class="message" v-if="app.message">
              <div class="message-label">申请留言：</div>
              <div class="message-content">{{ app.message }}</div>
            </div>

            <div class="reject-reason" v-if="app.status === 'REJECTED' && app.rejectReason">
              <el-alert
                :title="`拒绝理由：${app.rejectReason}`"
                type="error"
                :closable="false"
              />
            </div>

            <div class="quit-reason" v-if="app.status === 'QUIT' && app.rejectReason">
              <el-alert
                :title="`退出理由：${app.rejectReason}`"
                type="info"
                :closable="false"
              />
            </div>
          </div>

          <div class="card-footer">
            <span class="apply-time">申请时间：{{ formatDate(app.createTime) }}</span>
            <el-button 
              size="small" 
              @click="handleViewTeam(app.teamId)"
            >
              查看队伍
            </el-button>
          </div>
        </div>

        <el-empty 
          v-if="filteredApplications.length === 0" 
          :description="getEmptyText()"
        >
          <el-button type="primary" @click="$router.push('/')">
            去申请队伍
          </el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, ArrowDown, User, SwitchButton, HomeFilled, Trophy
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getMyApplications, cancelApplication, type Application } from '@/api/application'
import { quitTeam } from '@/api/team'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const applications = ref<Application[]>([])
const activeTab = ref('pending')

const pendingCount = computed(() => {
  return applications.value.filter(app => app.status === 'PENDING').length
})

const filteredApplications = computed(() => {
  const statusMap: Record<string, string> = {
    pending: 'PENDING',
    approved: 'APPROVED',
    rejected: 'REJECTED',
    quit: 'QUIT'
  }
  const status = statusMap[activeTab.value]
  return applications.value.filter(app => app.status === status)
})

// 加载我的申请
const loadApplications = async () => {
  loading.value = true
  try {
    const res = await getMyApplications()
    applications.value = res.data
  } catch (error) {
    console.error('加载申请列表失败:', error)
    ElMessage.error('加载申请列表失败')
  } finally {
    loading.value = false
  }
}

const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'home':
      router.push('/')
      break
    case 'logout':
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
      break
  }
}

const handleCancel = (app: Application) => {
  ElMessageBox.confirm(
    '确定要撤销这个申请吗？',
    '撤销申请',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await cancelApplication(app.id)
      ElMessage.success('已撤销申请')
      loadApplications()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '撤销失败')
    }
  }).catch(() => {})
}

const handleQuit = async (app: Application) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请填写退出理由（必填）',
      '退出队伍',
      {
        confirmButtonText: '确定退出',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请说明退出原因...',
        inputValidator: (value) => {
          if (!value || value.trim().length === 0) {
            return '请填写退出理由'
          }
          if (value.trim().length < 5) {
            return '退出理由至少5个字符'
          }
          return true
        }
      }
    )
    
    await quitTeam(app.teamId, reason)
    ElMessage.success('已退出队伍')
    loadApplications()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('退出队伍失败:', error)
      ElMessage.error(error.response?.data?.message || '退出队伍失败')
    }
  }
}

const handleViewTeam = (teamId: number) => {
  // 这里需要通过teamId找到competitionId，暂时跳转到首页
  router.push('/')
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'PENDING':
      return 'warning'
    case 'APPROVED':
      return 'success'
    case 'REJECTED':
      return 'danger'
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
    case 'QUIT':
      return '已退出'
    default:
      return status
  }
}

const getEmptyText = () => {
  switch (activeTab.value) {
    case 'pending':
      return '暂无待审核的申请'
    case 'approved':
      return '暂无通过的申请'
    case 'rejected':
      return '暂无被拒绝的申请'
    case 'quit':
      return '暂无退出记录'
    default:
      return '暂无申请'
  }
}

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

onMounted(() => {
  loadApplications()
})
</script>

<style scoped lang="scss">
.my-applications {
  min-height: 100vh;
  background: #f5f7fa;
}

// 顶部导航栏
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

  .header-actions {
    .user-dropdown {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 4px 12px;
      border-radius: 8px;
      transition: background 0.3s;

      &:hover {
        background: #f5f7fa;
      }

      .user-name {
        color: #606266;
        font-size: 14px;
      }
    }
  }
}

// 容器
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

// 标签页
.tabs {
  margin-bottom: 24px;
}

// 申请列表
.applications-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

// 申请卡片
.application-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #e4e7ed;

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;

      .team-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin: 0;
      }
    }
  }

  .card-body {
    margin-bottom: 16px;

    .info-row {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;
      font-size: 14px;

      .info-icon {
        color: #667eea;
      }

      .info-label {
        color: #909399;
      }

      .info-value {
        color: #303133;
        font-weight: 500;
      }
    }

    .message {
      margin: 12px 0;
      padding: 12px;
      background: #f5f7fa;
      border-radius: 8px;

      .message-label {
        font-size: 13px;
        color: #909399;
        margin-bottom: 8px;
      }

      .message-content {
        font-size: 14px;
        color: #606266;
        line-height: 1.6;
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
    padding-top: 12px;
    border-top: 1px solid #e4e7ed;

    .apply-time {
      font-size: 13px;
      color: #909399;
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .container {
    padding: 16px;
  }

  .application-card {
    .card-header {
      flex-direction: column;
      gap: 12px;
    }

    .card-footer {
      flex-direction: column;
      gap: 12px;
      align-items: flex-start;
    }
  }
}
</style>
