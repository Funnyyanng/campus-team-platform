<template>
  <div class="talent-pool">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <el-icon class="title-icon"><UserFilled /></el-icon>
            人才库
          </h1>
          <p class="page-subtitle">发现优秀的队友，组建完美团队</p>
        </div>
        <div class="header-right">
          <el-statistic title="可邀请人才" :value="users.length" />
        </div>
      </div>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" label-position="top">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="专业">
              <el-input 
                v-model="searchForm.major" 
                placeholder="如：计算机科学与技术" 
                clearable
                :prefix-icon="Search"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="年级">
              <el-select 
                v-model="searchForm.grade" 
                placeholder="请选择年级" 
                clearable
                style="width: 100%"
              >
                <el-option label="大一" value="大一" />
                <el-option label="大二" value="大二" />
                <el-option label="大三" value="大三" />
                <el-option label="大四" value="大四" />
                <el-option label="研一" value="研一" />
                <el-option label="研二" value="研二" />
                <el-option label="研三" value="研三" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="技能标签">
              <el-input 
                v-model="searchForm.skills" 
                placeholder="如：Java, Python（不区分大小写）" 
                clearable
                :prefix-icon="Star"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24" class="search-actions">
            <el-button type="primary" :icon="Search" @click="handleSearch">
              搜索人才
            </el-button>
            <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 结果列表 -->
    <div class="user-list">
      <el-empty 
        v-if="users.length === 0 && !loading" 
        description="暂无符合条件的人才，试试调整搜索条件"
        :image-size="200"
      />
      
      <el-row v-else :gutter="20" v-loading="loading">
        <el-col
          v-for="user in users"
          :key="user.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card class="user-card" shadow="hover">
            <div class="user-header">
              <el-avatar :src="user.avatar" :size="64">
                <el-icon><User /></el-icon>
              </el-avatar>
              <el-tag 
                v-if="user.allowInvite" 
                type="success" 
                size="small" 
                class="invite-tag"
              >
                可邀请
              </el-tag>
            </div>
            
            <div class="user-info">
              <div class="user-name">{{ user.nickname }}</div>
              
              <div class="user-meta">
                <div class="meta-item">
                  <el-icon><School /></el-icon>
                  <span>{{ user.college }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><Reading /></el-icon>
                  <span>{{ user.major }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ user.grade }}</span>
                </div>
              </div>
              
              <div v-if="user.skills" class="user-skills">
                <el-tag
                  v-for="(skill, index) in user.skills.split(',').slice(0, 5)"
                  :key="index"
                  size="small"
                  type="info"
                  effect="plain"
                >
                  {{ skill }}
                </el-tag>
              </div>
              
              <el-button
                type="primary"
                size="default"
                class="invite-btn"
                :icon="Message"
                @click="handleInvite(user)"
              >
                发送邀请
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 邀请对话框 -->
    <el-dialog
      v-model="inviteDialogVisible"
      :title="`邀请 ${inviteForm.userName} 加入队伍`"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="inviteForm" label-width="100px" label-position="top">
        <el-alert
          title="温馨提示"
          type="info"
          :closable="false"
          style="margin-bottom: 20px"
        >
          <template #default>
            <div>• 每个队伍对同一用户只能发送一次邀请</div>
            <div>• 用户接受邀请后将自动加入队伍</div>
            <div>• 请选择合适的队伍并填写诚恳的邀请留言</div>
          </template>
        </el-alert>

        <el-form-item label="选择队伍" required>
          <el-select 
            v-model="inviteForm.teamId" 
            placeholder="请选择要邀请加入的队伍"
            style="width: 100%"
          >
            <el-option
              v-for="team in myTeams"
              :key="team.id"
              :label="team.title"
              :value="team.id"
              :disabled="team.status !== 'RECRUITING'"
            >
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span>{{ team.title }}</span>
                <el-tag 
                  :type="team.status === 'RECRUITING' ? 'success' : 'info'" 
                  size="small"
                >
                  {{ team.currentSize }}/{{ team.targetSize }}人
                </el-tag>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="邀请留言" required>
          <el-input
            v-model="inviteForm.message"
            type="textarea"
            :rows="6"
            placeholder="请输入邀请留言，介绍队伍情况和为什么邀请TA..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="inviteDialogVisible = false" size="large">
          取消
        </el-button>
        <el-button 
          type="primary" 
          @click="handleSendInvite"
          size="large"
          :icon="Message"
        >
          发送邀请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Search, 
  User, 
  UserFilled, 
  Star, 
  RefreshLeft, 
  Message,
  School,
  Reading,
  Calendar
} from '@element-plus/icons-vue'
import { searchUsers, type UserSearchResult } from '@/api/user'
import { getMyTeams, inviteUser, type Team } from '@/api/team'
import { useUserStore } from '@/stores/user'

const searchForm = ref({
  major: '',
  grade: '',
  skills: ''
})

const users = ref<UserSearchResult[]>([])
const myTeams = ref<Team[]>([])
const loading = ref(false)
const inviteDialogVisible = ref(false)
const inviteForm = ref({
  userId: 0,
  userName: '',
  teamId: undefined as number | undefined,
  message: ''
})

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await searchUsers(searchForm.value)
    users.value = res.data || []
    if (users.value.length === 0) {
      ElMessage.info('未找到符合条件的用户')
    } else {
      ElMessage.success(`找到 ${users.value.length} 位符合条件的人才`)
    }
  } catch (error: any) {
    ElMessage.error(error.message || '搜索失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.value = {
    major: '',
    grade: '',
    skills: ''
  }
  users.value = []
}

const loadMyTeams = async () => {
  try {
    const res = await getMyTeams()
    // 只显示招募中且自己是队长的队伍
    const userStore = useUserStore()
    const currentUserId = userStore.userInfo?.id
    
    myTeams.value = (res.data || []).filter(
      (team: Team) => team.status === 'RECRUITING' && team.leaderId === currentUserId
    )
  } catch (error: any) {
    ElMessage.error(error.message || '加载队伍失败')
  }
}

const handleInvite = (user: UserSearchResult) => {
  if (myTeams.value.length === 0) {
    ElMessage.warning('您没有正在招募的队伍，请先创建队伍')
    return
  }
  
  inviteForm.value = {
    userId: user.id,
    userName: user.nickname,
    teamId: undefined,
    message: `你好 ${user.nickname}！\n\n我们队伍正在寻找优秀的成员，看到你的技能标签很匹配，诚邀你加入我们！期待与你一起合作，共同进步！`
  }
  inviteDialogVisible.value = true
}

const handleSendInvite = async () => {
  if (!inviteForm.value.teamId) {
    ElMessage.warning('请选择队伍')
    return
  }
  
  try {
    await inviteUser(
      inviteForm.value.teamId,
      inviteForm.value.userId,
      inviteForm.value.message
    )
    ElMessage.success('邀请已发送')
    inviteDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '发送邀请失败')
  }
}

onMounted(() => {
  loadMyTeams()
})
</script>

<style scoped lang="scss">
.talent-pool {
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
    :deep(.el-statistic__head) {
      color: rgba(255, 255, 255, 0.8);
      font-size: 14px;
    }

    :deep(.el-statistic__content) {
      color: white;
      font-size: 32px;
      font-weight: 600;
    }
  }
}

// 搜索卡片
.search-card {
  margin-bottom: 24px;
  border-radius: 12px;
  border: none;

  :deep(.el-card__body) {
    padding: 24px;
  }

  .search-actions {
    display: flex;
    justify-content: center;
    gap: 12px;
    margin-top: 8px;
  }
}

// 用户列表
.user-list {
  .user-card {
    margin-bottom: 20px;
    border-radius: 12px;
    border: 1px solid #e4e7ed;
    transition: all 0.3s;
    overflow: hidden;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      border-color: #667eea;
    }

    :deep(.el-card__body) {
      padding: 0;
    }
  }

  .user-header {
    background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
    padding: 24px;
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;

    .invite-tag {
      position: absolute;
      top: 12px;
      right: 12px;
    }
  }

  .user-info {
    padding: 20px;
  }

  .user-name {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    text-align: center;
    margin-bottom: 16px;
  }

  .user-meta {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 16px;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      color: #606266;

      .el-icon {
        color: #909399;
        font-size: 16px;
      }

      span {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }

  .user-skills {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 16px;
    min-height: 28px;

    .el-tag {
      border-radius: 4px;
    }
  }

  .invite-btn {
    width: 100%;
    border-radius: 8px;
    height: 40px;
    font-size: 14px;
    font-weight: 500;
  }
}

// 响应式
@media (max-width: 768px) {
  .talent-pool {
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

  .search-card {
    :deep(.el-card__body) {
      padding: 16px;
    }
  }
}
</style>
