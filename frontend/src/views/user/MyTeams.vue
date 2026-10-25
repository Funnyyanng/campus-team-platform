<template>
  <div class="my-teams">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <el-button @click="$router.back()" :icon="ArrowLeft">返回</el-button>
        <h1 class="page-title">我的队伍</h1>
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
      <div class="teams-list">
        <div 
          v-for="team in teams" 
          :key="team.id"
          class="team-card"
        >
          <div class="card-header">
            <div class="header-left">
              <h3 class="team-title">{{ team.title }}</h3>
              <el-tag 
                v-if="team.type === 'PROJECT'" 
                type="warning" 
                size="small"
              >
                课题
              </el-tag>
              <el-tag 
                v-else
                type="primary" 
                size="small"
              >
                竞赛
              </el-tag>
              <el-tag 
                :type="getStatusType(team.status)" 
                size="small"
              >
                {{ getStatusText(team.status) }}
              </el-tag>
              <el-tag 
                v-if="!isLeader(team)" 
                type="info" 
                size="small"
              >
                队员
              </el-tag>
            </div>
            <div class="header-right">
              <el-button 
                v-if="team.type === 'COMPETITION'"
                type="primary" 
                size="small"
                @click="handleViewDetail(team.competitionId)"
              >
                查看竞赛
              </el-button>
              <!-- 队长操作菜单 -->
              <el-dropdown 
                v-if="isLeader(team)"
                @command="(cmd) => handleTeamAction(cmd, team)"
              >
                <el-button size="small" :icon="More" circle />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item 
                      command="refresh" 
                      v-if="team.status === 'RECRUITING'"
                    >
                      <el-icon><Refresh /></el-icon>
                      调整招募
                    </el-dropdown-item>
                    <el-dropdown-item 
                      command="close" 
                      v-if="team.status === 'RECRUITING'"
                    >
                      <el-icon><Lock /></el-icon>
                      关闭招募
                    </el-dropdown-item>
                    <el-dropdown-item 
                      command="delete" 
                      divided
                    >
                      <el-icon><Delete /></el-icon>
                      删除队伍
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <!-- 队员操作按钮 -->
              <el-button 
                v-if="!isLeader(team)"
                size="small" 
                type="warning"
                @click="handleQuitTeam(team)"
              >
                退出队伍
              </el-button>
            </div>
          </div>

          <div class="card-body">
            <!-- 竞赛模式 -->
            <div class="info-row" v-if="team.type === 'COMPETITION'">
              <div class="info-item">
                <el-icon class="info-icon"><Trophy /></el-icon>
                <span class="info-label">竞赛：</span>
                <span class="info-value">{{ team.competitionName }}</span>
              </div>
            </div>
            
            <!-- 课题模式 -->
            <div v-if="team.type === 'PROJECT'">
              <div class="info-row">
                <div class="info-item">
                  <el-icon class="info-icon" style="color: #e6a23c;"><Star /></el-icon>
                  <span class="info-label">课题名称：</span>
                  <span class="info-value">{{ team.projectName }}</span>
                </div>
              </div>
              <div class="info-row" v-if="team.projectType">
                <div class="info-item">
                  <el-icon class="info-icon"><Document /></el-icon>
                  <span class="info-label">课题类型：</span>
                  <el-tag type="primary" size="small">{{ team.projectType }}</el-tag>
                </div>
              </div>
              <div class="info-row" v-if="team.projectDuration">
                <div class="info-item">
                  <el-icon class="info-icon"><Timer /></el-icon>
                  <span class="info-label">预期时长：</span>
                  <span class="info-value">{{ team.projectDuration }}</span>
                </div>
              </div>
            </div>

            <div class="info-row">
              <div class="info-item">
                <el-icon class="info-icon"><UserFilled /></el-icon>
                <span class="info-label">队伍人数：</span>
                <span class="info-value">{{ team.currentSize }}/{{ team.targetSize }}人</span>
                <el-progress 
                  :percentage="Math.round((team.currentSize / team.targetSize) * 100)" 
                  :stroke-width="6"
                  :show-text="false"
                  style="width: 100px; margin-left: 12px;"
                />
              </div>
            </div>

            <div class="description" v-if="team.description">
              <p>{{ team.description }}</p>
            </div>

            <div class="skills" v-if="team.requiredSkills">
              <el-icon><Star /></el-icon>
              <span>需求技能：{{ team.requiredSkills }}</span>
            </div>
          </div>

          <div class="card-footer">
            <span class="create-time">创建于 {{ formatDate(team.createTime) }}</span>
            <div class="actions" v-if="isLeader(team)">
              <el-button size="small" @click="handleViewApplications(team)">
                查看申请 ({{ team.pendingApplicationCount || 0 }})
              </el-button>
              <el-button size="small" @click="handleManageMembers(team)">
                管理成员
              </el-button>
            </div>
            <div class="actions" v-else>
              <el-button size="small" @click="handleManageMembers(team)">
                查看成员
              </el-button>
            </div>
          </div>
        </div>

        <el-empty 
          v-if="!loading && teams.length === 0" 
          description="您还没有创建任何队伍"
        >
          <el-button type="primary" @click="$router.push('/')">
            去创建队伍
          </el-button>
        </el-empty>
      </div>
    </div>

    <!-- 调整招募对话框 -->
    <el-dialog
      v-model="refreshDialogVisible"
      title="调整招募信息"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="refreshForm" label-width="100px">
        <el-form-item label="队伍标题" required>
          <el-input 
            v-model="refreshForm.title" 
            placeholder="请输入队伍标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="队伍简介" required>
          <el-input 
            v-model="refreshForm.description" 
            type="textarea"
            :rows="4"
            placeholder="请输入队伍简介"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="目标人数" required>
          <el-input-number 
            v-model="refreshForm.targetSize" 
            :min="currentRefreshTeam?.currentSize || 2"
            :max="10"
          />
          <span class="tip" style="margin-left: 12px; color: #909399; font-size: 13px;">
            当前人数：{{ currentRefreshTeam?.currentSize }}
          </span>
        </el-form-item>
        
        <el-form-item label="需求技能">
          <el-input 
            v-model="refreshForm.requiredSkills" 
            placeholder="如：Java、Python、前端开发"
            maxlength="200"
          />
        </el-form-item>
        
        <el-form-item label="验证问题">
          <el-input 
            v-model="refreshForm.questions" 
            type="textarea"
            :rows="3"
            placeholder="可选：设置验证问题"
            maxlength="500"
          />
        </el-form-item>
        
        <el-alert
          title="调整说明"
          type="info"
          :closable="false"
          show-icon
        >
          <ul style="margin: 0; padding-left: 20px;">
            <li>调整后队伍会置顶显示</li>
            <li>每天最多调整1次</li>
            <li>创建后3天内可调整</li>
            <li>目标人数只能增加，不能减少</li>
          </ul>
        </el-alert>
      </el-form>
      
      <template #footer>
        <el-button @click="refreshDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="submitRefresh"
          :loading="submitting"
        >
          确定调整
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, ArrowDown, User, SwitchButton, HomeFilled, More, 
  Lock, Delete, Trophy, UserFilled, Star, Document, Timer, Refresh
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { 
  getMyTeams, closeRecruitment, deleteTeam, quitTeam, 
  checkCanRefresh, refreshTeam, 
  type Team, type TeamUpdateRequest 
} from '@/api/team'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const teams = ref<Team[]>([])
const refreshDialogVisible = ref(false)
const submitting = ref(false)
const currentRefreshTeam = ref<Team | null>(null)
const refreshForm = ref<TeamUpdateRequest>({
  title: '',
  description: '',
  targetSize: 2,
  requiredSkills: '',
  questions: ''
})

// 加载我的队伍
const loadMyTeams = async () => {
  loading.value = true
  try {
    const res = await getMyTeams()
    teams.value = res.data
  } catch (error) {
    console.error('加载队伍列表失败:', error)
    ElMessage.error('加载队伍列表失败')
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

const handleViewDetail = (competitionId: number) => {
  router.push(`/competition/${competitionId}`)
}

const handleTeamAction = (command: string, team: Team) => {
  switch (command) {
    case 'refresh':
      handleRefreshTeam(team)
      break
    case 'close':
      handleCloseRecruiting(team)
      break
    case 'delete':
      handleDeleteTeam(team)
      break
  }
}

// 处理调整招募
const handleRefreshTeam = async (team: Team) => {
  try {
    // 检查是否可以调整
    const checkRes = await checkCanRefresh(team.id)
    
    if (!checkRes.data.canRefresh) {
      ElMessage.warning(checkRes.data.reason)
      return
    }
    
    // 填充表单
    currentRefreshTeam.value = team
    refreshForm.value = {
      title: team.title,
      description: team.description || '',
      targetSize: team.targetSize,
      requiredSkills: team.requiredSkills || '',
      questions: team.questions || ''
    }
    
    refreshDialogVisible.value = true
  } catch (error: any) {
    console.error('检查调整权限失败:', error)
    ElMessage.error(error.response?.data?.message || '检查失败')
  }
}

// 提交调整
const submitRefresh = async () => {
  if (!currentRefreshTeam.value) return
  
  // 验证表单
  if (!refreshForm.value.title || refreshForm.value.title.trim().length < 2) {
    ElMessage.warning('队伍标题至少2个字符')
    return
  }
  
  if (!refreshForm.value.description || refreshForm.value.description.trim().length < 10) {
    ElMessage.warning('队伍简介至少10个字符')
    return
  }
  
  if (submitting.value) return
  
  submitting.value = true
  try {
    await refreshTeam(currentRefreshTeam.value.id, refreshForm.value)
    ElMessage.success('调整成功，队伍已置顶')
    refreshDialogVisible.value = false
    await loadMyTeams()
  } catch (error: any) {
    console.error('调整招募失败:', error)
    ElMessage.error(error.response?.data?.message || '调整失败')
  } finally {
    submitting.value = false
  }
}

const handleCloseRecruiting = async (team: Team) => {
  try {
    await ElMessageBox.confirm(
      '关闭招募后将不再接收新的申请，确定要关闭吗？',
      '关闭招募',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await closeRecruitment(team.id)
    ElMessage.success('已关闭招募')
    loadMyTeams()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('关闭招募失败:', error)
      ElMessage.error(error.message || '关闭招募失败')
    }
  }
}

const handleDeleteTeam = async (team: Team) => {
  try {
    await ElMessageBox.confirm(
      '删除队伍后将无法恢复，确定要删除吗？',
      '删除队伍',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    await deleteTeam(team.id)
    ElMessage.success('已删除队伍')
    loadMyTeams()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除队伍失败:', error)
      ElMessage.error(error.message || '删除队伍失败')
    }
  }
}

const handleViewApplications = (team: Team) => {
  router.push(`/team/${team.id}/applications`)
}

const handleManageMembers = (team: Team) => {
  router.push(`/team/${team.id}/members`)
}

const handleQuitTeam = async (team: Team) => {
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
    
    await quitTeam(team.id, reason)
    ElMessage.success('已退出队伍')
    loadMyTeams()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('退出队伍失败:', error)
      ElMessage.error(error.response?.data?.message || '退出队伍失败')
    }
  }
}

const isLeader = (team: Team) => {
  return team.leaderId === userStore.userInfo?.id
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'RECRUITING':
      return 'success'
    case 'FULL':
      return 'info'
    case 'CLOSED':
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'RECRUITING':
      return '招募中'
    case 'FULL':
      return '已满员'
    case 'CLOSED':
      return '已关闭'
    default:
      return status
  }
}

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

onMounted(() => {
  loadMyTeams()
})
</script>

<style scoped lang="scss">
.my-teams {
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
    display: flex;
    align-items: center;
    gap: 16px;

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

// 队伍列表
.teams-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

// 队伍卡片
.team-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e4e7ed;

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;

      .team-title {
        font-size: 20px;
        font-weight: 600;
        color: #303133;
        margin: 0;
      }
    }

    .header-right {
      display: flex;
      gap: 8px;
    }
  }

  .card-body {
    margin-bottom: 20px;

    .info-row {
      margin-bottom: 12px;

      .info-item {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 14px;

        .info-icon {
          color: #667eea;
          font-size: 16px;
        }

        .info-label {
          color: #909399;
        }

        .info-value {
          color: #303133;
          font-weight: 500;
        }
      }
    }

    .description {
      margin: 16px 0;
      padding: 12px;
      background: #f5f7fa;
      border-radius: 8px;

      p {
        margin: 0;
        font-size: 14px;
        color: #606266;
        line-height: 1.6;
      }
    }

    .skills {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;
      color: #667eea;
      padding: 8px 12px;
      background: #f0f2ff;
      border-radius: 6px;
    }
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 16px;
    border-top: 1px solid #e4e7ed;

    .create-time {
      font-size: 13px;
      color: #909399;
    }

    .actions {
      display: flex;
      gap: 8px;
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .container {
    padding: 16px;
  }

  .team-card {
    padding: 16px;

    .card-header {
      flex-direction: column;
      gap: 12px;

      .header-right {
        width: 100%;
        justify-content: flex-end;
      }
    }

    .card-footer {
      flex-direction: column;
      gap: 12px;
      align-items: flex-start;

      .actions {
        width: 100%;
        
        button {
          flex: 1;
        }
      }
    }
  }
}
</style>
