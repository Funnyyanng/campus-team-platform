<template>
  <div class="team-manage">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="关键词">
          <el-input
            v-model="queryForm.keyword"
            placeholder="队伍标题或队长昵称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="队伍类型">
          <el-select v-model="queryForm.type" placeholder="全部" clearable style="width: 120px">
            <el-option label="竞赛队伍" value="COMPETITION" />
            <el-option label="课题队伍" value="PROJECT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="招募中" value="RECRUITING" />
            <el-option label="已满员" value="FULL" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 队伍列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>队伍列表</span>
          <el-tag type="info">共 {{ filteredTeams.length }} 支队伍</el-tag>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="paginatedTeams"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column label="队伍类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'COMPETITION' ? 'primary' : 'success'" size="small">
              {{ row.type === 'COMPETITION' ? '竞赛' : '课题' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="队伍标题" min-width="200">
          <template #default="{ row }">
            <div class="team-title">
              <div class="title-text">{{ row.title }}</div>
              <div class="subtitle-text">
                {{ row.type === 'COMPETITION' ? row.competitionName : row.projectName }}
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="队长" width="150">
          <template #default="{ row }">
            <div class="leader-info">
              <el-avatar :src="row.leaderAvatar" :size="32" />
              <span style="margin-left: 8px">{{ row.leaderName }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="人数" width="100">
          <template #default="{ row }">
            <el-tag size="small">
              {{ row.currentSize }}/{{ row.targetSize }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="待审核" width="90">
          <template #default="{ row }">
            <el-badge :value="row.pendingApplicationCount" :hidden="!row.pendingApplicationCount">
              <el-tag size="small" type="warning">
                {{ row.pendingApplicationCount || 0 }}
              </el-tag>
            </el-badge>
          </template>
        </el-table-column>
        
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              查看详情
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="filteredTeams.length"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="队伍详情"
      width="700px"
    >
      <div v-if="currentTeam" class="team-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="队伍ID">
            {{ currentTeam.id }}
          </el-descriptions-item>
          <el-descriptions-item label="队伍类型">
            <el-tag :type="currentTeam.type === 'COMPETITION' ? 'primary' : 'success'">
              {{ currentTeam.type === 'COMPETITION' ? '竞赛队伍' : '课题队伍' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="队伍标题" :span="2">
            {{ currentTeam.title }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentTeam.type === 'COMPETITION'" label="关联竞赛" :span="2">
            {{ currentTeam.competitionName }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentTeam.type === 'PROJECT'" label="课题名称" :span="2">
            {{ currentTeam.projectName }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentTeam.type === 'PROJECT'" label="课题类型">
            {{ currentTeam.projectType }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentTeam.type === 'PROJECT'" label="预期时长">
            {{ currentTeam.projectDuration }}
          </el-descriptions-item>
          <el-descriptions-item label="队长">
            <div class="leader-info">
              <el-avatar :src="currentTeam.leaderAvatar" :size="32" />
              <span style="margin-left: 8px">{{ currentTeam.leaderName }}</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="队伍人数">
            {{ currentTeam.currentSize }}/{{ currentTeam.targetSize }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentTeam.status)">
              {{ getStatusText(currentTeam.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="待审核申请">
            {{ currentTeam.pendingApplicationCount || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ formatDate(currentTeam.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="2">
            {{ formatDate(currentTeam.updateTime) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentTeam.description" label="队伍简介" :span="2">
            <div class="description-text">{{ currentTeam.description }}</div>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentTeam.requiredSkills" label="技能要求" :span="2">
            {{ currentTeam.requiredSkills }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentTeam.questions" label="验证问题" :span="2">
            <div class="questions-text">{{ currentTeam.questions }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { type Team } from '@/api/team'
import { getAllTeams, deleteTeamByAdmin } from '@/api/admin'
import dayjs from 'dayjs'

const loading = ref(false)
const detailDialogVisible = ref(false)
const allTeams = ref<Team[]>([])
const currentTeam = ref<Team | null>(null)
const currentPage = ref(1)
const pageSize = ref(10)

const queryForm = reactive({
  keyword: '',
  type: '',
  status: ''
})

// 过滤后的队伍列表
const filteredTeams = computed(() => {
  let teams = allTeams.value

  // 关键词搜索
  if (queryForm.keyword) {
    const keyword = queryForm.keyword.toLowerCase()
    teams = teams.filter(team => 
      team.title.toLowerCase().includes(keyword) ||
      team.leaderName.toLowerCase().includes(keyword) ||
      (team.competitionName && team.competitionName.toLowerCase().includes(keyword)) ||
      (team.projectName && team.projectName.toLowerCase().includes(keyword))
    )
  }

  // 类型筛选
  if (queryForm.type) {
    teams = teams.filter(team => team.type === queryForm.type)
  }

  // 状态筛选
  if (queryForm.status) {
    teams = teams.filter(team => team.status === queryForm.status)
  }

  return teams
})

// 分页后的队伍列表
const paginatedTeams = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredTeams.value.slice(start, end)
})

// 加载所有队伍
const loadAllTeams = async () => {
  loading.value = true
  try {
    // 使用管理员专用接口获取所有队伍（不限状态）
    const res = await getAllTeams()
    allTeams.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
}

// 重置
const handleReset = () => {
  queryForm.keyword = ''
  queryForm.type = ''
  queryForm.status = ''
  currentPage.value = 1
}

// 查看详情
const handleViewDetail = (team: Team) => {
  currentTeam.value = team
  detailDialogVisible.value = true
}

// 删除队伍
const handleDelete = async (team: Team) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      `确定要删除队伍「${team.title}」吗？此操作不可恢复！\n\n请输入删除原因（将通过站内信通知所有队员）：`,
      '删除队伍',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入删除原因',
        inputValidator: (value) => {
          if (!value || value.trim().length === 0) {
            return '请输入删除原因'
          }
          if (value.trim().length < 5) {
            return '删除原因至少5个字符'
          }
          return true
        },
        inputType: 'textarea',
        inputAttributes: {
          rows: 4
        }
      }
    )
    
    await deleteTeamByAdmin(team.id, reason)
    ElMessage.success('删除成功，已通知所有队员')
    loadAllTeams()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 格式化日期
const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 获取状态类型
const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    'RECRUITING': 'success',
    'FULL': 'warning',
    'CLOSED': 'info'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    'RECRUITING': '招募中',
    'FULL': '已满员',
    'CLOSED': '已关闭'
  }
  return map[status] || status
}

onMounted(() => {
  loadAllTeams()
})
</script>

<style scoped lang="scss">
.team-manage {
  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .team-title {
      .title-text {
        font-weight: 500;
        color: #303133;
        margin-bottom: 4px;
      }

      .subtitle-text {
        font-size: 12px;
        color: #909399;
      }
    }

    .leader-info {
      display: flex;
      align-items: center;
    }

    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .team-detail {
    .description-text,
    .questions-text {
      white-space: pre-wrap;
      word-break: break-word;
      line-height: 1.6;
    }
  }
}
</style>
