<template>
  <div class="admin-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <svg class="logo-icon" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2"/>
            <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2"/>
            <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2"/>
          </svg>
          <span class="logo-text">UniTeam 管理后台</span>
        </div>
        <el-button @click="$router.push('/')">返回首页</el-button>
      </div>
    </header>

    <div class="admin-container">
      <!-- 侧边栏 -->
      <aside class="sidebar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据概览</span>
          </el-menu-item>
          <el-menu-item index="users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="competitions">
            <el-icon><Trophy /></el-icon>
            <span>竞赛管理</span>
          </el-menu-item>
          <el-menu-item index="teams">
            <el-icon><UserFilled /></el-icon>
            <span>队伍管理</span>
          </el-menu-item>
          <el-menu-item index="announcements">
            <el-icon><Bell /></el-icon>
            <span>公告管理</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <!-- 主内容区 -->
      <main class="main-content">
        <div class="content-header">
          <h2>{{ pageTitle }}</h2>
        </div>

        <!-- 数据概览 -->
        <div v-if="activeMenu === 'dashboard'" class="dashboard" v-loading="loading">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card class="stat-card">
                <div class="stat-icon user-icon">
                  <el-icon><User /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.totalUsers.toLocaleString() }}</div>
                  <div class="stat-label">注册用户</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="stat-card">
                <div class="stat-icon team-icon">
                  <el-icon><UserFilled /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.activeTeams.toLocaleString() }}</div>
                  <div class="stat-label">活跃队伍</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="stat-card">
                <div class="stat-icon competition-icon">
                  <el-icon><Trophy /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.activeCompetitions.toLocaleString() }}</div>
                  <div class="stat-label">进行中竞赛</div>
                </div>
              </el-card>
            </el-col>

          </el-row>

          <!-- 图表区域 -->
          <el-row :gutter="20" style="margin-top: 20px">
            <el-col :span="12">
              <el-card class="chart-card">
                <template #header>
                  <span>队伍状态分布</span>
                </template>
                <div ref="teamStatusChartRef" style="height: 300px"></div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card class="chart-card">
                <template #header>
                  <span>竞赛级别分布</span>
                </template>
                <div ref="competitionLevelChartRef" style="height: 300px"></div>
              </el-card>
            </el-col>
          </el-row>

          <el-row :gutter="20" style="margin-top: 20px">
            <el-col :span="12">
              <el-card class="chart-card">
                <template #header>
                  <span>热门技能标签 TOP 10</span>
                </template>
                <div ref="skillTagChartRef" style="height: 300px"></div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card class="chart-card">
                <template #header>
                  <span>队伍类型分布</span>
                </template>
                <div ref="teamTypeChartRef" style="height: 300px"></div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 用户管理 -->
        <UserManage v-else-if="activeMenu === 'users'" />

        <!-- 竞赛管理 -->
        <CompetitionManage v-else-if="activeMenu === 'competitions'" />

        <!-- 队伍管理 -->
        <TeamManage v-else-if="activeMenu === 'teams'" />

        <!-- 公告管理 -->
        <AnnouncementManage v-else-if="activeMenu === 'announcements'" />

        <!-- 其他页面占位 -->
        <div v-else class="placeholder">
          <el-empty description="功能开发中，敬请期待" />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { 
  DataAnalysis, 
  User, 
  Trophy, 
  UserFilled, 
  Bell 
} from '@element-plus/icons-vue'
import { getDashboardStats, getDashboardCharts, type DashboardStats, type DashboardCharts } from '@/api/admin'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import UserManage from './UserManage.vue'
import CompetitionManage from './CompetitionManage.vue'
import TeamManage from './TeamManage.vue'
import AnnouncementManage from './AnnouncementManage.vue'

const router = useRouter()
const activeMenu = ref('dashboard')
const stats = ref<DashboardStats>({
  totalUsers: 0,
  activeTeams: 0,
  activeCompetitions: 0
})
const loading = ref(false)

// 图表引用
const teamStatusChartRef = ref<HTMLDivElement>()
const competitionLevelChartRef = ref<HTMLDivElement>()
const skillTagChartRef = ref<HTMLDivElement>()
const teamTypeChartRef = ref<HTMLDivElement>()

// 图表实例
let teamStatusChart: ECharts | null = null
let competitionLevelChart: ECharts | null = null
let skillTagChart: ECharts | null = null
let teamTypeChart: ECharts | null = null

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    dashboard: '数据概览',
    users: '用户管理',
    competitions: '竞赛管理',
    teams: '队伍管理',
    announcements: '公告管理'
  }
  return titles[activeMenu.value] || '管理后台'
})

const handleMenuSelect = (index: string) => {
  activeMenu.value = index
}

// 监听菜单切换，当切换到数据概览时重新加载数据
watch(activeMenu, (newValue) => {
  if (newValue === 'dashboard') {
    loadStats()
    loadCharts()
  }
})

// 加载统计数据
const loadStats = async () => {
  loading.value = true
  try {
    const res = await getDashboardStats()
    stats.value = res.data
  } catch (error: any) {
    ElMessage.error(error.message || '加载统计数据失败')
  } finally {
    loading.value = false
  }
}

// 加载图表数据
const loadCharts = async () => {
  try {
    const res = await getDashboardCharts()
    const data = res.data
    
    await nextTick()
    
    // 初始化图表
    initTeamStatusChart(data.teamStatus)
    initCompetitionLevelChart(data.competitionLevel)
    initSkillTagChart(data.skillTag)
    initTeamTypeChart(data.teamType)
  } catch (error: any) {
    ElMessage.error(error.message || '加载图表数据失败')
  }
}

// 队伍状态分布图
const initTeamStatusChart = (data: any) => {
  if (!teamStatusChartRef.value) return
  
  teamStatusChart = echarts.init(teamStatusChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: '5%',
      left: 'center'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: [
          { value: data.recruiting || 0, name: '招募中', itemStyle: { color: '#67C23A' } },
          { value: data.full || 0, name: '已满员', itemStyle: { color: '#E6A23C' } },
          { value: data.closed || 0, name: '已关闭', itemStyle: { color: '#909399' } }
        ]
      }
    ]
  }
  
  teamStatusChart.setOption(option)
}

// 竞赛级别分布图
const initCompetitionLevelChart = (data: any) => {
  if (!competitionLevelChartRef.value) return
  
  competitionLevelChart = echarts.init(competitionLevelChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['国家级', '省级', '校级'],
      axisLabel: {
        interval: 0
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        type: 'bar',
        data: [
          { value: data.national || 0, itemStyle: { color: '#F56C6C' } },
          { value: data.provincial || 0, itemStyle: { color: '#E6A23C' } },
          { value: data.school || 0, itemStyle: { color: '#409EFF' } }
        ],
        barWidth: '50%',
        label: {
          show: true,
          position: 'top'
        }
      }
    ]
  }
  
  competitionLevelChart.setOption(option)
}

// 热门技能标签图
const initSkillTagChart = (data: any) => {
  if (!skillTagChartRef.value) return
  
  skillTagChart = echarts.init(skillTagChartRef.value)
  
  const items = data.items || []
  const skillNames = items.map((item: any) => item.name)
  const skillCounts = items.map((item: any) => item.count)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: '{b}: {c} 人'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: skillNames,
      axisLabel: {
        interval: 0
      }
    },
    series: [
      {
        type: 'bar',
        data: skillCounts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ])
        },
        label: {
          show: true,
          position: 'right',
          formatter: '{c} 人'
        }
      }
    ]
  }
  
  skillTagChart.setOption(option)
}

// 队伍类型分布图
const initTeamTypeChart = (data: any) => {
  if (!teamTypeChartRef.value) return
  
  teamTypeChart = echarts.init(teamTypeChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: '5%',
      left: 'center'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: data.competition || 0, name: '竞赛队伍', itemStyle: { color: '#409EFF' } },
          { value: data.project || 0, name: '课题队伍', itemStyle: { color: '#67C23A' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  teamTypeChart.setOption(option)
}

// 窗口大小改变时重新调整图表
const handleResize = () => {
  teamStatusChart?.resize()
  competitionLevelChart?.resize()
  skillTagChart?.resize()
  teamTypeChart?.resize()
}

onMounted(() => {
  loadStats()
  loadCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  teamStatusChart?.dispose()
  competitionLevelChart?.dispose()
  skillTagChart?.dispose()
  teamTypeChart?.dispose()
})
</script>

<style scoped lang="scss">
.admin-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

  .header-content {
    padding: 16px 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .logo {
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;

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
  }
}

.admin-container {
  display: flex;
  min-height: calc(100vh - 64px);
}

.sidebar {
  width: 200px;
  min-width: 200px;
  max-width: 200px;
  background: white;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  flex-shrink: 0;

  .sidebar-menu {
    border-right: none;
    width: 200px;
  }
}

.main-content {
  flex: 1;
  padding: 24px;

  .content-header {
    margin-bottom: 24px;

    h2 {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }
  }
}

.dashboard {
  .stat-card {
    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 20px;
    }

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;
      color: white;

      &.user-icon {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.team-icon {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }

      &.competition-icon {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }
    }

    .stat-info {
      flex: 1;

      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 4px;
      }

      .stat-label {
        font-size: 14px;
        color: #909399;
      }
    }
  }

  .chart-card {
    margin-top: 20px;
  }
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}
</style>
