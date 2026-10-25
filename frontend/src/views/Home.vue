<template>
  <div class="home">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="logo">
          <svg class="logo-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span class="logo-text">UniTeam</span>
          <span class="logo-subtitle">校园竞赛与课题组队平台</span>
        </div>
        <div class="header-actions">
          <template v-if="userStore.token">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="message-badge">
              <el-button :icon="Bell" circle @click="$router.push('/messages')" />
            </el-badge>
            <el-button :icon="Search" circle @click="$router.push('/talent-pool')" title="人才库" />
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
                  <el-dropdown-item command="myApplications">
                    <el-icon><Document /></el-icon>
                    我的申请
                  </el-dropdown-item>
                  <el-dropdown-item 
                    v-if="userStore.userInfo?.role === 'ADMIN'"
                    command="admin"
                    divided
                  >
                    <el-icon><Setting /></el-icon>
                    管理后台
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button @click="loginDialogVisible = true">登录</el-button>
            <el-button type="primary" @click="registerDialogVisible = true">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 主横幅 -->
    <div class="hero-banner">
      <div class="hero-content">
        <h1 class="hero-title">寻找你的理想队友</h1>
        <p class="hero-subtitle">连接全校竞赛与科研力量，让组队更高效</p>
      </div>
    </div>

    <!-- 标签页 -->
    <div class="container">
      <div class="tabs-header">
        <el-tabs v-model="activeTab" class="tabs">
          <el-tab-pane label="竞赛信息" name="competitions"></el-tab-pane>
          <el-tab-pane label="科研课题" name="projects"></el-tab-pane>
        </el-tabs>
        <el-button 
          v-if="activeTab === 'projects' && userStore.token" 
          type="primary" 
          :icon="Plus"
          @click="handleCreateProject"
        >
          发布课题
        </el-button>
      </div>

      <!-- 筛选和搜索栏 -->
      <div class="filter-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索竞赛或队伍（支持：数模、电赛、挑战杯等）"
          :prefix-icon="Search"
          clearable
          style="max-width: 400px"
        />
        
        <div class="filter-actions">
          <el-dropdown v-if="activeTab === 'competitions'" trigger="click">
            <el-button :icon="Filter">
              筛选
              <span v-if="activeCompetitionFiltersCount > 0" class="filter-count">
                ({{ activeCompetitionFiltersCount }})
              </span>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu class="filter-dropdown">
                <div class="filter-section">
                  <div class="filter-label">竞赛级别</div>
                  <el-radio-group v-model="competitionFilters.level" size="small">
                    <el-radio-button label="">全部</el-radio-button>
                    <el-radio-button label="国家级">国家级</el-radio-button>
                    <el-radio-button label="省级">省级</el-radio-button>
                    <el-radio-button label="校级">校级</el-radio-button>
                  </el-radio-group>
                </div>
                <div class="filter-section">
                  <div class="filter-label">竞赛类别</div>
                  <el-radio-group v-model="competitionFilters.category" size="small">
                    <el-radio-button label="">全部</el-radio-button>
                    <el-radio-button label="学科竞赛">学科竞赛</el-radio-button>
                    <el-radio-button label="科技创新">科技创新</el-radio-button>
                    <el-radio-button label="创业大赛">创业大赛</el-radio-button>
                  </el-radio-group>
                </div>
                <div class="filter-footer">
                  <el-button size="small" @click="resetCompetitionFilters">重置</el-button>
                </div>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-dropdown v-if="activeTab === 'projects'" trigger="click" @command="handleSortChange">
            <el-button :icon="Sort">
              排序: {{ getSortLabel(projectSort) }}
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="default">默认排序</el-dropdown-item>
                <el-dropdown-item 
                  command="skill-match" 
                  :disabled="!userStore.token"
                >
                  技能匹配度
                  <span v-if="!userStore.token" style="color: #909399; font-size: 12px;">
                    (需登录)
                  </span>
                </el-dropdown-item>
                <el-dropdown-item command="latest">最新发布</el-dropdown-item>
                <el-dropdown-item command="almost-full">即将满员</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-dropdown v-if="activeTab === 'projects'" trigger="click">
            <el-button :icon="Filter">
              筛选
              <span v-if="activeProjectFiltersCount > 0" class="filter-count">
                ({{ activeProjectFiltersCount }})
              </span>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu class="filter-dropdown">
                <div class="filter-section">
                  <div class="filter-label">课题类型</div>
                  <el-radio-group v-model="projectFilters.type" size="small">
                    <el-radio-button label="">全部</el-radio-button>
                    <el-radio-button label="科研项目">科研项目</el-radio-button>
                    <el-radio-button label="毕业设计">毕业设计</el-radio-button>
                    <el-radio-button label="创业项目">创业项目</el-radio-button>
                    <el-radio-button label="技术学习">技术学习</el-radio-button>
                  </el-radio-group>
                </div>
                <div class="filter-footer">
                  <el-button size="small" @click="resetProjectFilters">重置</el-button>
                </div>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 竞赛信息列表 -->
      <div v-if="activeTab === 'competitions'" v-loading="loading" class="competition-grid">
        <div 
          v-for="item in filteredCompetitions" 
          :key="item.id"
          class="competition-card"
          @click="handleViewCompetition(item.id)"
        >
          <div class="card-header">
            <el-tag 
              :type="item.level === '国家级' ? 'danger' : item.level === '省级' ? 'warning' : 'primary'" 
              size="small"
            >
              {{ item.level }}
            </el-tag>
            <el-tag v-if="item.category" type="info" size="small" style="margin-left: 8px">
              {{ item.category }}
            </el-tag>
          </div>
          <h3 class="card-title">{{ item.name }}</h3>
          <div class="card-info">
            <p class="card-meta">
              <span>队伍人数：{{ item.minTeamSize }}-{{ item.maxTeamSize }}人</span>
            </p>
            <p class="card-meta" v-if="item.registrationEnd">
              <span>报名截止：{{ formatDate(item.registrationEnd) }}</span>
            </p>
            <p class="card-meta" v-if="item.participationFee !== undefined">
              <span>{{ item.participationFee === 0 ? '免费参赛' : `参赛费：¥${item.participationFee}` }}</span>
            </p>
          </div>
          <p class="card-status">还有{{ item.recruitingTeamCount || 0 }}支队伍正在招募中</p>
          <el-icon class="card-arrow"><ArrowRight /></el-icon>
        </div>
        
        <el-empty 
          v-if="!loading && filteredCompetitions.length === 0" 
          :description="searchKeyword || activeCompetitionFiltersCount > 0 ? '没有找到符合条件的竞赛' : '暂无进行中的竞赛'"
          style="grid-column: 1 / -1"
        >
          <el-button 
            v-if="searchKeyword || activeCompetitionFiltersCount > 0" 
            @click="searchKeyword = ''; resetCompetitionFilters()"
          >
            清空筛选
          </el-button>
        </el-empty>
      </div>

      <!-- 科研课题列表 -->
      <div v-if="activeTab === 'projects'" v-loading="projectTeamsLoading" class="teams-grid">
        <div 
          v-for="team in sortedProjectTeams" 
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
            <div class="team-tags">
              <el-tag type="warning" size="small">课题</el-tag>
              <el-tag :type="team.currentSize >= team.targetSize ? 'info' : 'success'" size="small">
                {{ team.currentSize }}/{{ team.targetSize }}人
              </el-tag>
              <el-tag 
                v-if="projectSort === 'skill-match' && calculateSkillMatch(team) > 0" 
                type="danger" 
                size="small"
              >
                匹配 {{ Math.round(calculateSkillMatch(team)) }}%
              </el-tag>
            </div>
          </div>
          
          <h3 class="team-title">{{ team.title }}</h3>
          
          <div class="team-project">
            <el-icon><Star /></el-icon>
            <span>{{ team.projectName }}</span>
            <el-tag 
              v-if="team.projectType" 
              type="primary" 
              size="small" 
              style="margin-left: 8px"
            >
              {{ team.projectType }}
            </el-tag>
          </div>
          
          <p class="team-description" v-if="team.description">
            {{ team.description }}
          </p>
          
          <div class="team-skills" v-if="team.requiredSkills">
            <span class="skills-label">需求技能：</span>
            <span class="skills-value">{{ team.requiredSkills }}</span>
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
          v-if="!projectTeamsLoading && sortedProjectTeams.length === 0" 
          :description="searchKeyword || activeProjectFiltersCount > 0 ? '没有找到符合条件的队伍' : '暂无课题队伍招募中'"
          style="grid-column: 1 / -1"
        >
          <el-button 
            v-if="searchKeyword || activeProjectFiltersCount > 0" 
            @click="searchKeyword = ''; resetProjectFilters()"
          >
            清空筛选
          </el-button>
          <el-button 
            v-else-if="userStore.token"
            type="primary" 
            @click="handleCreateProject"
          >
            发布课题
          </el-button>
        </el-empty>
      </div>

    </div>

    <!-- 页脚 -->
    <footer class="footer">
      <p>© 2023 UniTeam. 连接校园，共创未来。</p>
    </footer>

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
          <p class="competition-name" v-if="selectedTeam.type === 'COMPETITION'">{{ selectedTeam.competitionName }}</p>
          <p class="competition-name" v-if="selectedTeam.type === 'PROJECT'" style="color: #e6a23c;">{{ selectedTeam.projectName }}</p>
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

    <!-- 登录对话框 -->
    <el-dialog
      v-model="loginDialogVisible"
      title="用户登录"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
      >
        <el-form-item prop="email">
          <el-input
            v-model="loginForm.email"
            placeholder="请输入邮箱"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="handleLoginSubmit"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button
            type="primary"
            size="large"
            :loading="loginLoading"
            style="width: 100%"
            @click="handleLoginSubmit"
          >
            登录
          </el-button>
          <el-button
            text
            type="primary"
            style="width: 100%; margin-top: 12px"
            @click="switchToRegister"
          >
            还没有账号？去注册
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 注册对话框 -->
    <el-dialog
      v-model="registerDialogVisible"
      title="用户注册"
      width="480px"
      :close-on-click-modal="false"
    >
      <div style="text-align: center; margin-bottom: 20px; color: #909399; font-size: 14px;">
        仅限高校邮箱注册（@*.edu.cn）
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
      >
        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入高校邮箱（@*.edu.cn）"
            size="large"
            :disabled="registerCodeSent"
          />
        </el-form-item>

        <el-form-item prop="code">
          <div class="code-input">
            <el-input
              v-model="registerForm.code"
              placeholder="请输入6位验证码"
              size="large"
              maxlength="6"
            />
            <el-button
              size="large"
              :disabled="registerCountdown > 0 || registerCodeSent"
              :loading="sendingRegisterCode"
              @click="handleSendRegisterCode"
            >
              {{ registerCountdown > 0 ? `${registerCountdown}秒` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（6-20位）"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            size="large"
            show-password
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button
            type="primary"
            size="large"
            :loading="registerLoading"
            style="width: 100%"
            @click="handleRegisterSubmit"
          >
            注册
          </el-button>
          <el-button
            text
            type="primary"
            style="width: 100%; margin-top: 12px"
            @click="switchToLogin"
          >
            已有账号？去登录
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 创建课题对话框 -->
    <el-dialog
      v-model="createProjectDialogVisible"
      title="发布课题"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="projectFormRef"
        :model="projectForm"
        :rules="projectFormRules"
        label-width="100px"
      >
        <el-form-item label="课题名称" prop="projectName">
          <el-input
            v-model="projectForm.projectName"
            placeholder="请输入课题名称"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="课题类型" prop="projectType">
          <el-select
            v-model="projectForm.projectType"
            placeholder="请选择课题类型"
            style="width: 100%"
          >
            <el-option
              v-for="item in projectTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="预期时长">
          <el-input
            v-model="projectForm.projectDuration"
            placeholder="例如：3个月、半年、1年（选填）"
            maxlength="100"
          />
        </el-form-item>
        
        <el-form-item label="队伍标题" prop="title">
          <el-input
            v-model="projectForm.title"
            placeholder="请输入队伍标题"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="队伍简介" prop="description">
          <el-input
            v-model="projectForm.description"
            type="textarea"
            :rows="4"
            placeholder="请简要介绍队伍情况、目标等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="目标人数" prop="targetSize">
          <el-input-number
            v-model="projectForm.targetSize"
            :min="2"
            :max="10"
            :step="1"
          />
          <span class="form-tip">（课题队伍：2-10人）</span>
        </el-form-item>
        
        <el-form-item label="需求技能" prop="requiredSkills">
          <el-input
            v-model="projectForm.requiredSkills"
            placeholder="例如：前端开发、算法、数据分析"
            maxlength="200"
          />
        </el-form-item>
        
        <el-form-item label="验证问题" prop="questions">
          <el-input
            v-model="projectForm.questions"
            type="textarea"
            :rows="3"
            placeholder="可选：设置1-3个问题，用于筛选申请者"
            maxlength="500"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="createProjectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitProject" :loading="projectSubmitting">
          创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { ArrowRight, ArrowDown, User, UserFilled, Document, SwitchButton, Setting, Star, Plus, Search, Filter, Sort, Bell } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getActiveCompetitions, type Competition } from '@/api/competition'
import { getRecruitingProjectTeams, createTeam, type Team, type TeamCreateRequest } from '@/api/team'
import { applyToTeam } from '@/api/application'
import { isProfileComplete } from '@/utils/profile'
import { getUnreadCount } from '@/api/message'
import { login, register, sendCode } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('competitions')
const competitions = ref<Competition[]>([])
const projectTeams = ref<Team[]>([])
const loading = ref(false)
const projectTeamsLoading = ref(false)
const unreadCount = ref(0)

// 登录对话框
const loginDialogVisible = ref(false)
const loginFormRef = ref<FormInstance>()
const loginLoading = ref(false)
const loginForm = ref({
  email: '',
  password: ''
})

const loginRules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 注册对话框
const registerDialogVisible = ref(false)
const registerFormRef = ref<FormInstance>()
const registerLoading = ref(false)
const sendingRegisterCode = ref(false)
const registerCodeSent = ref(false)
const registerCountdown = ref(0)
const registerForm = ref({
  email: '',
  code: '',
  password: '',
  confirmPassword: ''
})

const validateRegisterEmail = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入邮箱'))
  } else if (!value.endsWith('.edu.cn')) {
    callback(new Error('仅支持高校邮箱（@*.edu.cn）'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.value.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules: FormRules = {
  email: [{ validator: validateRegisterEmail, trigger: 'blur' }],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

// 搜索和筛选
const searchKeyword = ref('')
const competitionFilters = ref({
  level: '',
  category: ''
})
const projectFilters = ref({
  type: ''
})
const projectSort = ref('default')

// 创建课题队伍表单
const createProjectDialogVisible = ref(false)
const projectFormRef = ref<FormInstance>()
const projectSubmitting = ref(false)
const projectForm = ref<TeamCreateRequest>({
  type: 'PROJECT',
  projectName: '',
  projectType: '',
  projectDuration: '',
  title: '',
  description: '',
  targetSize: 3,
  requiredSkills: '',
  questions: ''
})

const projectFormRules: FormRules = {
  projectName: [
    { required: true, message: '请输入课题名称', trigger: 'blur' },
    { min: 2, max: 200, message: '课题名称长度在2-200个字符', trigger: 'blur' }
  ],
  projectType: [
    { required: true, message: '请选择课题类型', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入队伍标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度在2-50个字符', trigger: 'blur' }
  ],
  targetSize: [
    { required: true, message: '请选择目标人数', trigger: 'change' }
  ]
}

const projectTypes = [
  { label: '科研项目', value: '科研项目' },
  { label: '毕业设计', value: '毕业设计' },
  { label: '创业项目', value: '创业项目' },
  { label: '技术学习', value: '技术学习' },
  { label: '其他', value: '其他' }
]

// 竞赛别名映射
const competitionAliases: Record<string, string[]> = {
  '数学建模': ['数模', 'MCM', 'ICM', 'mcm', 'icm'],
  '电子设计': ['电赛'],
  '挑战杯': ['挑战杯'],
  '互联网+': ['互联网加', '互联网plus', 'plus'],
  'ACM': ['acm', 'ICPC', 'icpc'],
  '蓝桥杯': ['蓝桥']
}

// 搜索匹配（包含别名）
const searchMatch = (text: string, keyword: string): boolean => {
  if (!keyword) return true
  
  const lowerText = text.toLowerCase()
  const lowerKeyword = keyword.toLowerCase()
  
  // 直接匹配
  if (lowerText.includes(lowerKeyword)) return true
  
  // 别名匹配
  for (const [fullName, aliases] of Object.entries(competitionAliases)) {
    if (lowerText.includes(fullName.toLowerCase())) {
      if (aliases.some(alias => alias.toLowerCase() === lowerKeyword)) {
        return true
      }
    }
  }
  
  return false
}

// 筛选后的竞赛列表
const filteredCompetitions = computed(() => {
  let result = competitions.value
  
  // 级别筛选
  if (competitionFilters.value.level) {
    result = result.filter(c => c.level === competitionFilters.value.level)
  }
  
  // 类别筛选
  if (competitionFilters.value.category) {
    result = result.filter(c => c.category === competitionFilters.value.category)
  }
  
  // 搜索
  if (searchKeyword.value) {
    result = result.filter(c => searchMatch(c.name, searchKeyword.value))
  }
  
  return result
})

// 筛选后的课题队伍列表
const filteredProjectTeams = computed(() => {
  let result = projectTeams.value
  
  // 课题类型筛选
  if (projectFilters.value.type) {
    result = result.filter(t => t.projectType === projectFilters.value.type)
  }
  
  // 搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(t => {
      return (
        t.title.toLowerCase().includes(keyword) ||
        (t.projectName && t.projectName.toLowerCase().includes(keyword)) ||
        (t.description && t.description.toLowerCase().includes(keyword)) ||
        (t.requiredSkills && t.requiredSkills.toLowerCase().includes(keyword))
      )
    })
  }
  
  return result
})

// 计算技能匹配度
const calculateSkillMatch = (team: Team): number => {
  if (!team.requiredSkills || !userStore.userInfo) return 0
  
  // 获取用户技能（从 userInfo 中获取，需要先加载）
  const userSkills = userStore.userInfo.skills || []
  if (userSkills.length === 0) return 0
  
  // 解析队伍需求技能
  const teamSkills = team.requiredSkills.split(/[,，、]/).map(s => s.trim().toLowerCase())
  
  // 计算匹配数量
  const matchCount = userSkills.filter(us => 
    teamSkills.some(ts => ts.includes(us.toLowerCase()) || us.toLowerCase().includes(ts))
  ).length
  
  return teamSkills.length > 0 ? (matchCount / teamSkills.length) * 100 : 0
}

// 排序后的课题队伍列表
const sortedProjectTeams = computed(() => {
  let result = [...filteredProjectTeams.value]
  
  switch (projectSort.value) {
    case 'skill-match':
      result.sort((a, b) => calculateSkillMatch(b) - calculateSkillMatch(a))
      break
    case 'latest':
      result.sort((a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime())
      break
    case 'almost-full':
      result.sort((a, b) => {
        const remainA = a.targetSize - a.currentSize
        const remainB = b.targetSize - b.currentSize
        return remainA - remainB
      })
      break
    default:
      // 默认按更新时间倒序
      result.sort((a, b) => new Date(b.updateTime || b.createTime).getTime() - new Date(a.updateTime || a.createTime).getTime())
  }
  
  return result
})

// 活跃的筛选条件数量
const activeCompetitionFiltersCount = computed(() => {
  let count = 0
  if (competitionFilters.value.level) count++
  if (competitionFilters.value.category) count++
  return count
})

const activeProjectFiltersCount = computed(() => {
  let count = 0
  if (projectFilters.value.type) count++
  return count
})

// 重置筛选
const resetCompetitionFilters = () => {
  competitionFilters.value = {
    level: '',
    category: ''
  }
}

const resetProjectFilters = () => {
  projectFilters.value = {
    type: ''
  }
}

// 排序标签
const getSortLabel = (sort: string): string => {
  switch (sort) {
    case 'skill-match': return '技能匹配'
    case 'latest': return '最新发布'
    case 'almost-full': return '即将满员'
    default: return '默认'
  }
}

// 处理排序变化
const handleSortChange = (command: string) => {
  projectSort.value = command
}

// 加载竞赛数据
const loadCompetitions = async () => {
  loading.value = true
  try {
    const res = await getActiveCompetitions()
    competitions.value = res.data
  } catch (error) {
    console.error('加载竞赛数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载招募中的课题队伍
const loadProjectTeams = async () => {
  projectTeamsLoading.value = true
  try {
    const res = await getRecruitingProjectTeams()
    projectTeams.value = res.data
  } catch (error) {
    console.error('加载课题队伍数据失败:', error)
  } finally {
    projectTeamsLoading.value = false
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
    case 'myApplications':
      router.push('/my-applications')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  // 清空未读消息数
  unreadCount.value = 0
}

// 查看竞赛详情
const handleViewCompetition = (id: number) => {
  router.push(`/competition/${id}`)
}

// 申请对话框
const applyDialogVisible = ref(false)
const selectedTeam = ref<Team | null>(null)
const applyForm = ref({
  answers: '',
  message: ''
})
const applySubmitting = ref(false)

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

// 打开创建课题对话框
const handleCreateProject = async () => {
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
  
  // 重置表单
  projectForm.value = {
    type: 'PROJECT',
    projectName: '',
    projectType: '',
    projectDuration: '',
    title: '',
    description: '',
    targetSize: 3,
    requiredSkills: '',
    questions: ''
  }
  
  createProjectDialogVisible.value = true
}

// 提交创建课题
const handleSubmitProject = async () => {
  if (!projectFormRef.value) return
  
  await projectFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    projectSubmitting.value = true
    try {
      await createTeam(projectForm.value)
      ElMessage.success('创建课题队伍成功')
      createProjectDialogVisible.value = false
      
      // 重新加载课题队伍列表
      loadProjectTeams()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '创建课题队伍失败')
    } finally {
      projectSubmitting.value = false
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

const loadUnreadCount = async () => {
  if (userStore.token) {
    try {
      const res = await getUnreadCount()
      unreadCount.value = res.data?.count || 0
    } catch (error) {
      // 静默失败
    }
  }
}

// 登录提交
const handleLoginSubmit = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loginLoading.value = true
    try {
      const res = await login(loginForm.value)
      userStore.setToken(res.data.token)
      userStore.setUserInfo(res.data.userInfo)
      ElMessage.success('登录成功')
      loginDialogVisible.value = false
      
      // 重新加载数据
      loadUnreadCount()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '登录失败')
    } finally {
      loginLoading.value = false
    }
  })
}

// 发送注册验证码
const handleSendRegisterCode = async () => {
  if (!registerForm.value.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }

  if (!registerForm.value.email.endsWith('.edu.cn')) {
    ElMessage.warning('仅支持高校邮箱（@*.edu.cn）')
    return
  }

  sendingRegisterCode.value = true
  try {
    await sendCode({ email: registerForm.value.email })
    ElMessage.success('验证码已发送，请查收邮件')
    registerCodeSent.value = true
    registerCountdown.value = 60

    const timer = setInterval(() => {
      registerCountdown.value--
      if (registerCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '发送验证码失败')
  } finally {
    sendingRegisterCode.value = false
  }
}

// 注册提交
const handleRegisterSubmit = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return

    registerLoading.value = true
    try {
      await register({
        email: registerForm.value.email,
        code: registerForm.value.code,
        password: registerForm.value.password
      })
      ElMessage.success('注册成功，请登录')
      registerDialogVisible.value = false
      
      // 自动填充登录表单
      loginForm.value.email = registerForm.value.email
      loginForm.value.password = registerForm.value.password
      loginDialogVisible.value = true
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '注册失败')
    } finally {
      registerLoading.value = false
    }
  })
}

// 切换到注册
const switchToRegister = () => {
  loginDialogVisible.value = false
  registerDialogVisible.value = true
}

// 切换到登录
const switchToLogin = () => {
  registerDialogVisible.value = false
  loginDialogVisible.value = true
}

onMounted(() => {
  loadCompetitions()
  loadProjectTeams()
  loadUnreadCount()
  
  // 每30秒刷新一次未读数量
  setInterval(loadUnreadCount, 30000)
})
</script>

<style scoped lang="scss">
.home {
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

  .logo {
    display: flex;
    align-items: center;
    gap: 12px;

    .logo-icon {
      width: 32px;
      height: 32px;
      color: #667eea;
    }

    .logo-text {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }

    .logo-subtitle {
      font-size: 12px;
      color: #909399;
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 16px;

    .message-badge {
      :deep(.el-badge__content) {
        border: 2px solid white;
      }
    }

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

// 主横幅
.hero-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px 24px;
  margin: 24px auto;
  max-width: 1200px;
  border-radius: 16px;

  .hero-content {
    text-align: center;
    color: white;

    .hero-title {
      font-size: 36px;
      font-weight: 600;
      margin: 0 0 16px;
    }

    .hero-subtitle {
      font-size: 16px;
      opacity: 0.9;
      margin: 0;
    }
  }
}

// 容器
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 60px;
}

// 标签页头部
.tabs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  .tabs {
    flex: 1;
    margin-bottom: 0;

    :deep(.el-tabs__nav-wrap::after) {
      display: none;
    }
  }
}

// 筛选栏
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .filter-actions {
    display: flex;
    gap: 8px;

    .filter-count {
      color: #667eea;
      font-weight: 600;
    }
  }
}

// 筛选下拉菜单
.filter-dropdown {
  padding: 12px;
  min-width: 300px;

  .filter-section {
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }

    .filter-label {
      font-size: 13px;
      color: #606266;
      margin-bottom: 8px;
      font-weight: 500;
    }

    :deep(.el-radio-group) {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    :deep(.el-radio-button) {
      margin: 0;
    }
  }

  .filter-footer {
    padding-top: 12px;
    border-top: 1px solid #e4e7ed;
    display: flex;
    justify-content: flex-end;
  }
}

// 竞赛卡片网格
.competition-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

// 队伍网格
.teams-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.competition-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  border: 1px solid #e4e7ed;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    border-color: #667eea;

    .card-arrow {
      transform: translateX(4px);
    }
  }

  .card-header {
    margin-bottom: 12px;
  }

  .card-title {
    font-size: 16px;
    font-weight: 500;
    color: #303133;
    margin: 0 0 12px;
    line-height: 1.5;
  }

  .card-info {
    margin-bottom: 12px;

    .card-meta {
      font-size: 13px;
      color: #606266;
      margin: 4px 0;
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .card-status {
    font-size: 13px;
    color: #909399;
    margin: 0;
  }

  .card-arrow {
    position: absolute;
    right: 24px;
    top: 50%;
    transform: translateY(-50%);
    color: #c0c4cc;
    transition: all 0.3s ease;
  }
}

// 页脚
.footer {
  background: white;
  border-top: 1px solid #e4e7ed;
  padding: 32px 24px;
  text-align: center;

  p {
    margin: 0;
    color: #909399;
    font-size: 14px;
  }
}

// 队伍卡片
.team-card {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    border-color: #667eea;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
    transform: translateY(-2px);
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

    .team-tags {
      display: flex;
      gap: 8px;
    }
  }

  .team-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 12px;
    line-height: 1.5;
  }

  .team-competition {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #667eea;
    margin-bottom: 12px;
  }

  .team-project {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #e6a23c;
    margin-bottom: 12px;
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
    font-size: 13px;
    color: #606266;
    margin-bottom: 16px;
    padding: 8px 12px;
    background: #f5f7fa;
    border-radius: 6px;

    .skills-label {
      color: #909399;
    }

    .skills-value {
      color: #303133;
    }
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
      margin: 0 0 8px;
    }

    .competition-name {
      font-size: 14px;
      color: #667eea;
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

// 表单提示
.form-tip {
  margin-left: 12px;
  font-size: 13px;
  color: #909399;
}

// 对话框底部
.dialog-footer {
  width: 100%;
  padding: 0;
}

// 验证码输入框
.code-input {
  display: flex;
  gap: 10px;
  width: 100%;

  .el-input {
    flex: 1;
  }

  .el-button {
    white-space: nowrap;
    min-width: 110px;
  }
}

// 响应式
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 16px;
  }

  .hero-banner {
    margin: 16px;
    padding: 40px 24px;

    .hero-title {
      font-size: 28px;
    }
  }

  .competition-grid,
  .teams-grid {
    grid-template-columns: 1fr;
  }
}
</style>
