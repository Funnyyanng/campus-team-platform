<template>
  <div class="team-members">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <el-button @click="$router.back()" :icon="ArrowLeft">返回</el-button>
        <h1 class="page-title">队伍成员</h1>
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
      <div class="team-info-card">
        <h2>{{ teamInfo?.title }}</h2>
        <p class="team-desc">{{ teamInfo?.description }}</p>
        <div class="team-stats">
          <el-tag v-if="teamInfo?.type === 'PROJECT'" type="warning">课题</el-tag>
          <el-tag v-else type="primary">竞赛</el-tag>
          <el-tag v-if="teamInfo?.type === 'COMPETITION'" type="success">{{ teamInfo?.competitionName }}</el-tag>
          <el-tag v-if="teamInfo?.type === 'PROJECT'" type="success">{{ teamInfo?.projectName }}</el-tag>
          <el-tag v-if="teamInfo?.projectType" type="info" size="small">{{ teamInfo?.projectType }}</el-tag>
          <span class="member-count">
            当前成员：{{ teamInfo?.currentSize }}/{{ teamInfo?.targetSize }}人
          </span>
        </div>
      </div>

      <div class="members-list">
        <div class="list-header">
          <h3>队伍成员</h3>
          <el-text type="info" v-if="isLeader">提示：移除成员后无法恢复</el-text>
        </div>

        <div class="member-card leader-card">
          <el-avatar :src="teamInfo?.leaderAvatar" :size="60" />
          <div class="member-info">
            <div class="member-name">
              {{ teamInfo?.leaderName }}
              <el-tag type="warning" size="small">队长</el-tag>
            </div>
            <div class="member-details">
              <span>创建于 {{ formatDate(teamInfo?.createTime) }}</span>
            </div>
            <div class="member-contact" v-if="leaderContact">
              <div class="contact-item" v-if="leaderContact.wechat">
                <el-icon><ChatDotRound /></el-icon>
                <span>微信：{{ leaderContact.wechat }}</span>
              </div>
              <div class="contact-item" v-if="leaderContact.qq">
                <el-icon><Message /></el-icon>
                <span>QQ：{{ leaderContact.qq }}</span>
              </div>
            </div>
          </div>
        </div>

        <el-empty 
          v-if="!loading && members.length === 0" 
          description="暂无其他成员"
        />

        <div 
          v-for="member in members" 
          :key="member.id"
          class="member-card"
        >
          <el-avatar :src="member.avatar" :size="60" />
          <div class="member-info">
            <div class="member-name">{{ member.nickname }}</div>
            <div class="member-details">
              <span>{{ member.college }} · {{ member.major }}</span>
              <span>{{ member.grade }}</span>
            </div>
            <div class="member-skills" v-if="member.skills">
              <el-tag 
                v-for="skill in member.skills.split(',')" 
                :key="skill"
                size="small"
              >
                {{ skill }}
              </el-tag>
            </div>
            <div class="member-contact">
              <div class="contact-item" v-if="member.wechat">
                <el-icon><ChatDotRound /></el-icon>
                <span>微信：{{ member.wechat }}</span>
              </div>
              <div class="contact-item" v-if="member.qq">
                <el-icon><Message /></el-icon>
                <span>QQ：{{ member.qq }}</span>
              </div>
            </div>
          </div>
          <div class="member-actions" v-if="isLeader">
            <el-button 
              type="primary" 
              size="small"
              @click="handleTransferLeadership(member)"
            >
              转让队长
            </el-button>
            <el-button 
              type="danger" 
              size="small"
              @click="handleRemoveMember(member)"
            >
              移除
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, ArrowDown, User, SwitchButton, HomeFilled, ChatDotRound, Message
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getTeamById, removeMember, transferLeadership, type Team } from '@/api/team'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const teamInfo = ref<Team | null>(null)
const members = ref<any[]>([])
const leaderContact = ref<any>(null)

const teamId = Number(route.params.teamId)

// 判断当前用户是否是队长
const isLeader = computed(() => {
  return teamInfo.value?.leaderId === userStore.userInfo?.id
})

// 加载队伍信息
const loadTeamInfo = async () => {
  loading.value = true
  try {
    const res = await getTeamById(teamId)
    teamInfo.value = res.data
    
    // 加载队长联系方式
    await loadLeaderContact()
    
    // 加载队伍成员列表（已通过申请的成员）
    await loadMembers()
  } catch (error) {
    console.error('加载队伍信息失败:', error)
    ElMessage.error('加载队伍信息失败')
  } finally {
    loading.value = false
  }
}

// 加载队长联系方式
const loadLeaderContact = async () => {
  if (!teamInfo.value) return
  try {
    const { getUserProfile } = await import('@/api/user')
    const res = await getUserProfile(teamInfo.value.leaderId)
    leaderContact.value = {
      wechat: res.data.wechat,
      qq: res.data.qq
    }
  } catch (error) {
    console.error('加载队长联系方式失败:', error)
  }
}

// 加载成员列表
const loadMembers = async () => {
  try {
    const { getTeamMembers } = await import('@/api/application')
    const res = await getTeamMembers(teamId)
    // 将申请响应转换为成员信息
    members.value = res.data.map((app: any) => ({
      id: app.applicantId,
      nickname: app.applicantName,
      avatar: app.applicantAvatar,
      college: app.applicantCollege,
      major: app.applicantMajor,
      grade: app.applicantGrade,
      skills: app.applicantSkills,
      wechat: app.applicantWechat,
      qq: app.applicantQq
    }))
  } catch (error) {
    console.error('加载成员列表失败:', error)
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

const handleTransferLeadership = async (member: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要将队长权限转让给 ${member.nickname} 吗？转让后您将成为普通成员。`,
      '转让队长权限',
      {
        confirmButtonText: '确定转让',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await transferLeadership(teamId, member.id)
    ElMessage.success('队长权限已转让')
    // 转让后返回我的队伍页面
    router.push('/my-teams')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('转让队长权限失败:', error)
      ElMessage.error(error.message || '转让队长权限失败')
    }
  }
}

const handleRemoveMember = async (member: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要移除成员 ${member.nickname} 吗？移除后无法恢复。`,
      '移除成员',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await removeMember(teamId, member.id)
    ElMessage.success('已移除成员')
    loadTeamInfo()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('移除成员失败:', error)
      ElMessage.error(error.message || '移除成员失败')
    }
  }
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

onMounted(() => {
  loadTeamInfo()
})
</script>

<style scoped lang="scss">
.team-members {
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

// 队伍信息卡片
.team-info-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  h2 {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 12px 0;
  }

  .team-desc {
    color: #606266;
    margin: 0 0 16px 0;
    line-height: 1.6;
  }

  .team-stats {
    display: flex;
    align-items: center;
    gap: 16px;

    .member-count {
      color: #909399;
      font-size: 14px;
    }
  }
}

// 成员列表
.members-list {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e4e7ed;

    h3 {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }
  }
}

// 成员卡片
.member-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 12px;
  transition: background 0.3s;

  &:hover {
    background: #f5f7fa;
  }

  &.leader-card {
    background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
    margin-bottom: 24px;

    &:hover {
      background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
    }
  }

  .member-info {
    flex: 1;

    .member-name {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 8px;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .member-details {
      display: flex;
      gap: 12px;
      font-size: 14px;
      color: #606266;
      margin-bottom: 8px;
    }

    .member-skills {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
      margin-bottom: 8px;
    }

    .member-contact {
      display: flex;
      gap: 16px;
      flex-wrap: wrap;
      margin-top: 8px;

      .contact-item {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: #667eea;
        background: #f0f2ff;
        padding: 4px 12px;
        border-radius: 4px;

        .el-icon {
          font-size: 14px;
        }
      }
    }
  }

  .member-actions {
    display: flex;
    gap: 8px;
  }
}

// 响应式
@media (max-width: 768px) {
  .container {
    padding: 16px;
  }

  .member-card {
    flex-direction: column;
    align-items: flex-start;

    .member-actions {
      width: 100%;
      
      button {
        flex: 1;
      }
    }
  }
}
</style>
