<template>
  <div class="competition-detail">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <el-button @click="$router.back()" :icon="ArrowLeft">返回</el-button>
        <div class="header-actions">
          <template v-if="userStore.token">
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
                  <el-dropdown-item command="myTeams">
                    <el-icon><UserFilled /></el-icon>
                    我的队伍
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button @click="$router.push('/login')">登录</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <div class="container" v-loading="loading">
      <template v-if="competition">
        <!-- 竞赛基本信息 -->
        <div class="info-card">
          <div class="card-header">
            <div class="tags">
              <el-tag 
                :type="competition.level === '国家级' ? 'danger' : competition.level === '省级' ? 'warning' : 'primary'" 
                size="large"
              >
                {{ competition.level }}
              </el-tag>
              <el-tag v-if="competition.category" type="info" size="large">
                {{ competition.category }}
              </el-tag>
              <el-tag 
                v-if="competition.status === 'ACTIVE'" 
                type="success" 
                size="large"
              >
                进行中
              </el-tag>
            </div>
          </div>
          
          <h1 class="competition-title">{{ competition.name }}</h1>
          
          <div class="info-grid">
            <div class="info-item">
              <el-icon class="info-icon"><Calendar /></el-icon>
              <div class="info-content">
                <div class="info-label">报名时间</div>
                <div class="info-value">
                  {{ formatDate(competition.registrationStart) }} 至 {{ formatDate(competition.registrationEnd) }}
                </div>
              </div>
            </div>
            
            <div class="info-item">
              <el-icon class="info-icon"><Timer /></el-icon>
              <div class="info-content">
                <div class="info-label">比赛时间</div>
                <div class="info-value">
                  {{ formatDate(competition.competitionStart) }} 至 {{ formatDate(competition.competitionEnd) }}
                </div>
              </div>
            </div>
            
            <div class="info-item">
              <el-icon class="info-icon"><UserFilled /></el-icon>
              <div class="info-content">
                <div class="info-label">队伍人数</div>
                <div class="info-value">{{ competition.minTeamSize }}-{{ competition.maxTeamSize }}人</div>
              </div>
            </div>
            
            <div class="info-item">
              <el-icon class="info-icon"><Money /></el-icon>
              <div class="info-content">
                <div class="info-label">参赛费用</div>
                <div class="info-value">
                  {{ competition.participationFee === 0 ? '免费' : `¥${competition.participationFee}` }}
                </div>
              </div>
            </div>
            
            <div class="info-item" v-if="competition.organizer">
              <el-icon class="info-icon"><OfficeBuilding /></el-icon>
              <div class="info-content">
                <div class="info-label">主办方</div>
                <div class="info-value">{{ competition.organizer }}</div>
              </div>
            </div>
            
            <div class="info-item" v-if="competition.officialUrl">
              <el-icon class="info-icon"><Link /></el-icon>
              <div class="info-content">
                <div class="info-label">官方网站</div>
                <div class="info-value">
                  <a :href="competition.officialUrl" target="_blank" class="link">
                    访问官网 <el-icon><TopRight /></el-icon>
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 竞赛描述 -->
        <div class="description-card" v-if="competition.description">
          <h2 class="section-title">竞赛简介</h2>
          <div class="description-content">{{ competition.description }}</div>
        </div>

        <!-- 招募中的队伍 -->
        <div class="teams-section">
          <div class="section-header">
            <h2 class="section-title">招募中的队伍</h2>
            <el-button type="primary" @click="handleCreateTeam">
              <el-icon><Plus /></el-icon>
              创建队伍
            </el-button>
          </div>
          
          <div class="teams-grid" v-loading="teamsLoading">
            <div 
              v-for="team in teams" 
              :key="team.id"
              class="team-card"
            >
              <div class="team-header">
                <div class="leader-info">
                  <el-avatar :src="team.leaderAvatar" :size="40" />
                  <div class="leader-details">
                    <div class="leader-name">{{ team.leaderName }}</div>
                    <div class="team-status">队长</div>
                  </div>
                </div>
                <el-tag :type="team.currentSize >= team.targetSize ? 'info' : 'success'">
                  {{ team.currentSize }}/{{ team.targetSize }}人
                </el-tag>
              </div>
              
              <h3 class="team-title">{{ team.title }}</h3>
              
              <p class="team-description" v-if="team.description">
                {{ team.description }}
              </p>
              
              <div class="team-skills" v-if="team.requiredSkills">
                <el-icon><Star /></el-icon>
                <span>需求技能：{{ team.requiredSkills }}</span>
              </div>
              
              <div class="team-footer">
                <span class="team-time">{{ formatTime(team.createTime) }}</span>
                <el-button 
                  type="primary" 
                  size="small"
                  :disabled="team.currentSize >= team.targetSize"
                  @click.stop="handleApplyTeam(team)"
                >
                  申请加入
                </el-button>
              </div>
            </div>
            
            <el-empty 
              v-if="!teamsLoading && teams.length === 0" 
              description="暂无队伍招募中"
            />
          </div>
        </div>
      </template>
    </div>

    <!-- 创建队伍对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建队伍"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="teamForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="队伍标题" prop="title">
          <el-input
            v-model="teamForm.title"
            placeholder="请输入队伍标题"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="队伍简介" prop="description">
          <el-input
            v-model="teamForm.description"
            type="textarea"
            :rows="4"
            placeholder="请简要介绍队伍情况、目标等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="目标人数" prop="targetSize">
          <el-input-number
            v-model="teamForm.targetSize"
            :min="competition?.minTeamSize || 1"
            :max="competition?.maxTeamSize || 10"
            :step="1"
          />
          <span class="form-tip">
            （竞赛要求：{{ competition?.minTeamSize }}-{{ competition?.maxTeamSize }}人）
          </span>
        </el-form-item>
        
        <el-form-item label="需求技能" prop="requiredSkills">
          <el-input
            v-model="teamForm.requiredSkills"
            placeholder="例如：前端开发、算法、数据分析"
            maxlength="200"
          />
        </el-form-item>
        
        <el-form-item label="验证问题" prop="questions">
          <el-input
            v-model="teamForm.questions"
            type="textarea"
            :rows="3"
            placeholder="可选：设置1-3个问题，用于筛选申请者"
            maxlength="500"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitTeam" :loading="submitting">
          创建
        </el-button>
      </template>
    </el-dialog>

    <!-- 申请对话框 -->
    <el-dialog
      v-model="applyDialogVisible"
      title="申请加入队伍"
      width="500px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedTeam" class="apply-dialog">
        <div class="team-info">
          <h3>{{ selectedTeam.title }}</h3>
          <div class="team-meta">
            <span>队长：{{ selectedTeam.leaderName }}</span>
            <span>人数：{{ selectedTeam.currentSize }}/{{ selectedTeam.targetSize }}</span>
          </div>
        </div>

        <el-form label-width="80px">
          <el-form-item label="验证问题" v-if="selectedTeam.questions">
            <div class="questions-section">
              <el-alert
                type="info"
                :closable="false"
                style="margin-bottom: 12px"
              >
                <div style="white-space: pre-wrap; line-height: 1.6;">{{ selectedTeam.questions }}</div>
              </el-alert>
              <el-input
                v-model="applyForm.answers"
                type="textarea"
                :rows="4"
                placeholder="请回答队长的问题"
                maxlength="500"
                show-word-limit
              />
            </div>
          </el-form-item>
          
          <el-form-item label="申请留言">
            <el-input
              v-model="applyForm.message"
              type="textarea"
              :rows="4"
              placeholder="向队长介绍一下自己吧（选填）"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitApplication" :loading="applySubmitting">
          提交申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { 
  ArrowLeft, ArrowDown, User, SwitchButton, Calendar, Timer, 
  UserFilled, Money, OfficeBuilding, Link, TopRight, Plus, Star
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getCompetitionById, type Competition } from '@/api/competition'
import { createTeam, getTeamsByCompetition, type Team, type TeamCreateRequest } from '@/api/team'
import { applyToTeam } from '@/api/application'
import { isProfileComplete } from '@/utils/profile'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const competition = ref<Competition | null>(null)
const teams = ref<Team[]>([])
const teamsLoading = ref(false)

// 创建队伍表单
const createDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)
const teamForm = ref<TeamCreateRequest>({
  type: 'COMPETITION',
  competitionId: 0,
  title: '',
  description: '',
  targetSize: 3,
  requiredSkills: '',
  questions: ''
})

const formRules: FormRules = {
  title: [
    { required: true, message: '请输入队伍标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度在2-50个字符', trigger: 'blur' }
  ],
  targetSize: [
    { required: true, message: '请选择目标人数', trigger: 'change' }
  ]
}

// 申请对话框
const applyDialogVisible = ref(false)
const selectedTeam = ref<Team | null>(null)
const applyForm = ref({
  answers: '',
  message: ''
})
const applySubmitting = ref(false)

// 加载竞赛详情
const loadCompetition = async () => {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const res = await getCompetitionById(id)
    competition.value = res.data
    loadTeams(id)
  } catch (error) {
    console.error('加载竞赛详情失败:', error)
    ElMessage.error('加载竞赛详情失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 加载队伍列表
const loadTeams = async (competitionId: number) => {
  teamsLoading.value = true
  try {
    const res = await getTeamsByCompetition(competitionId)
    teams.value = res.data
  } catch (error) {
    console.error('加载队伍列表失败:', error)
  } finally {
    teamsLoading.value = false
  }
}

const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'myTeams':
      router.push('/my-teams')
      break
    case 'logout':
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
      break
  }
}

const handleCreateTeam = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  // 刷新用户信息
  try {
    const { getUserProfile } = await import('@/api/user')
    const res = await getUserProfile()
    userStore.setUserInfo(res.data)
  } catch (error) {
    console.error('刷新用户信息失败:', error)
  }
  
  // 检查信息完整度
  if (!isProfileComplete(userStore.userInfo)) {
    ElMessageBox.confirm(
      '为了更好地匹配队友，您需要先完善个人信息（专业、年级、技能标签）',
      '完善个人信息',
      {
        confirmButtonText: '去完善',
        cancelButtonText: '稍后再说',
        type: 'warning'
      }
    ).then(() => {
      router.push('/profile')
    }).catch(() => {})
    return
  }
  
  if (!competition.value) return
  
  // 重置表单
  teamForm.value = {
    type: 'COMPETITION',
    competitionId: competition.value.id,
    title: '',
    description: '',
    targetSize: competition.value.minTeamSize,
    requiredSkills: '',
    questions: ''
  }
  
  createDialogVisible.value = true
}

const handleSubmitTeam = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await createTeam(teamForm.value)
      ElMessage.success('创建队伍成功')
      createDialogVisible.value = false
      
      // 重新加载队伍列表
      if (competition.value) {
        loadTeams(competition.value.id)
      }
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '创建队伍失败')
    } finally {
      submitting.value = false
    }
  })
}

// 格式化日期
const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 格式化时间（相对时间）
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
  
  return formatDate(dateStr)
}

// 申请加入队伍
const handleApplyTeam = async (team: Team) => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  // 刷新用户信息
  try {
    const { getUserProfile } = await import('@/api/user')
    const res = await getUserProfile()
    userStore.setUserInfo(res.data)
  } catch (error) {
    console.error('刷新用户信息失败:', error)
  }
  
  // 检查信息完整度
  if (!isProfileComplete(userStore.userInfo)) {
    ElMessageBox.confirm(
      '为了更好地匹配队友，您需要先完善个人信息（专业、年级、技能标签）',
      '完善个人信息',
      {
        confirmButtonText: '去完善',
        cancelButtonText: '稍后再说',
        type: 'warning'
      }
    ).then(() => {
      router.push('/profile')
    }).catch(() => {})
    return
  }
  
  selectedTeam.value = team
  applyForm.value = { answers: '', message: '' }
  applyDialogVisible.value = true
}

// 提交申请
const handleSubmitApplication = async () => {
  if (!selectedTeam.value) return
  
  // 如果有验证问题，检查是否已回答
  if (selectedTeam.value.questions && !applyForm.value.answers.trim()) {
    ElMessage.warning('请回答队长的验证问题')
    return
  }
  
  applySubmitting.value = true
  try {
    await applyToTeam({
      teamId: selectedTeam.value.id,
      answers: applyForm.value.answers,
      message: applyForm.value.message
    })
    ElMessage.success('申请已提交，请等待队长审核')
    applyDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '申请失败')
  } finally {
    applySubmitting.value = false
  }
}

onMounted(() => {
  loadCompetition()
})
</script>

<style scoped lang="scss">
.competition-detail {
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

// 信息卡片
.info-card {
  background: white;
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .card-header {
    margin-bottom: 16px;

    .tags {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }

  .competition-title {
    font-size: 28px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 24px;
    line-height: 1.4;
  }

  .info-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 24px;
  }

  .info-item {
    display: flex;
    gap: 12px;

    .info-icon {
      font-size: 20px;
      color: #667eea;
      margin-top: 2px;
    }

    .info-content {
      flex: 1;

      .info-label {
        font-size: 13px;
        color: #909399;
        margin-bottom: 4px;
      }

      .info-value {
        font-size: 15px;
        color: #303133;
        font-weight: 500;

        .link {
          color: #667eea;
          text-decoration: none;
          display: inline-flex;
          align-items: center;
          gap: 4px;
          transition: color 0.3s;

          &:hover {
            color: #5568d3;
          }
        }
      }
    }
  }
}

// 描述卡片
.description-card {
  background: white;
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .section-title {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 16px;
  }

  .description-content {
    font-size: 15px;
    color: #606266;
    line-height: 1.8;
    white-space: pre-wrap;
  }
}

// 队伍区域
.teams-section {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .section-title {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }
  }

  .teams-grid {
    min-height: 200px;
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 20px;
  }
}

// 队伍卡片
.team-card {
  background: #f9fafb;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;

  &:hover {
    border-color: #667eea;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
  }

  .team-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .leader-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .leader-details {
        .leader-name {
          font-size: 14px;
          font-weight: 500;
          color: #303133;
        }

        .team-status {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }

  .team-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 12px;
    line-height: 1.5;
  }

  .team-description {
    font-size: 14px;
    color: #606266;
    line-height: 1.6;
    margin: 0 0 12px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .team-skills {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #667eea;
    margin-bottom: 16px;
  }

  .team-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 16px;
    border-top: 1px solid #e4e7ed;

    .team-time {
      font-size: 12px;
      color: #909399;
    }
  }
}

// 表单提示
.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}

// 申请对话框
.apply-dialog {
  .team-info {
    padding: 16px;
    background: #f5f7fa;
    border-radius: 8px;
    margin-bottom: 20px;

    h3 {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 12px;
    }

    .team-meta {
      display: flex;
      gap: 16px;
      font-size: 13px;
      color: #606266;
    }
  }

  .questions-section {
    :deep(.el-alert__content) {
      font-size: 14px;
      color: #606266;
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .container {
    padding: 16px;
  }

  .info-card,
  .description-card,
  .teams-section {
    padding: 20px;
  }

  .competition-title {
    font-size: 22px;
  }

  .info-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}
</style>
