<template>
  <div class="user-manage">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="关键词">
          <el-input
            v-model="queryForm.keyword"
            placeholder="邮箱或昵称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" value="NORMAL" />
            <el-option label="已封禁" value="BANNED" />
            <el-option label="禁言" value="MUTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="queryForm.role" placeholder="全部" clearable style="width: 120px">
            <el-option label="用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="userList"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户信息" width="300">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :src="row.avatar" :size="40" />
              <div class="info-text">
                <div class="nickname">{{ row.nickname }}</div>
                <div class="email">{{ row.email }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="college" label="学院" />
        <el-table-column prop="major" label="专业" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'" size="small">
              {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'NORMAL' ? 'success' : row.status === 'BANNED' ? 'danger' : 'warning'"
              size="small"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.role !== 'ADMIN'"
              link
              type="primary"
              size="small"
              @click="handleBan(row)"
            >
              {{ row.status === 'BANNED' ? '解封' : '封禁' }}
            </el-button>
            <el-button
              v-if="row.role !== 'ADMIN' && row.status !== 'MUTED'"
              link
              type="warning"
              size="small"
              @click="handleMute(row)"
            >
              禁言
            </el-button>
            <el-button
              v-if="row.role !== 'ADMIN'"
              link
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadUserList"
          @current-change="loadUserList"
        />
      </div>
    </el-card>

    <!-- 封禁对话框 -->
    <el-dialog
      v-model="banDialogVisible"
      :title="currentUser?.status === 'BANNED' ? '解封用户' : '封禁用户'"
      width="500px"
    >
      <el-form :model="banForm" label-width="80px">
        <el-form-item label="用户">
          <span>{{ currentUser?.nickname }} ({{ currentUser?.email }})</span>
        </el-form-item>
        <el-form-item label="操作">
          <span>{{ currentUser?.status === 'BANNED' ? '解除封禁' : '封禁账号' }}</span>
        </el-form-item>
        <el-form-item label="原因">
          <el-input
            v-model="banForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入原因（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="banDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="banLoading" @click="confirmBan">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, banUser, deleteUser, type UserManage } from '@/api/admin'
import dayjs from 'dayjs'

const loading = ref(false)
const banLoading = ref(false)
const userList = ref<UserManage[]>([])
const total = ref(0)
const banDialogVisible = ref(false)
const currentUser = ref<UserManage | null>(null)

const queryForm = reactive({
  keyword: '',
  status: '',
  role: '',
  page: 1,
  pageSize: 10
})

const banForm = reactive({
  reason: ''
})

// 加载用户列表
const loadUserList = async () => {
  loading.value = true
  try {
    const res = await getUserList(queryForm)
    userList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryForm.page = 1
  loadUserList()
}

// 重置
const handleReset = () => {
  queryForm.keyword = ''
  queryForm.status = ''
  queryForm.role = ''
  queryForm.page = 1
  loadUserList()
}

// 封禁/解封
const handleBan = (user: UserManage) => {
  currentUser.value = user
  banForm.reason = ''
  banDialogVisible.value = true
}

// 确认封禁
const confirmBan = async () => {
  if (!currentUser.value) return

  banLoading.value = true
  try {
    const newStatus = currentUser.value.status === 'BANNED' ? 'NORMAL' : 'BANNED'
    await banUser(currentUser.value.id, {
      status: newStatus,
      reason: banForm.reason
    })
    ElMessage.success(newStatus === 'BANNED' ? '封禁成功' : '解封成功')
    banDialogVisible.value = false
    loadUserList()
  } catch (error) {
    console.error(error)
  } finally {
    banLoading.value = false
  }
}

// 禁言
const handleMute = async (user: UserManage) => {
  try {
    await ElMessageBox.confirm(
      `确定要禁言用户 ${user.nickname} 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await banUser(user.id, { status: 'MUTED' })
    ElMessage.success('禁言成功')
    loadUserList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

// 删除
const handleDelete = async (user: UserManage) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 ${user.nickname} 吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    await deleteUser(user.id)
    ElMessage.success('删除成功')
    loadUserList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

// 格式化日期
const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 获取状态文本
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    NORMAL: '正常',
    BANNED: '已封禁',
    MUTED: '禁言'
  }
  return map[status] || status
}

onMounted(() => {
  loadUserList()
})
</script>

<style scoped lang="scss">
.user-manage {
  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
    .user-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .info-text {
        .nickname {
          font-weight: 500;
          color: #303133;
          margin-bottom: 4px;
        }

        .email {
          font-size: 12px;
          color: #909399;
        }
      }
    }

    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
