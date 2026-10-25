<template>
  <div class="announcement-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公告管理</span>
          <el-button type="primary" :icon="Plus" @click="showPublishDialog">
            发布公告
          </el-button>
        </div>
      </template>

      <el-alert
        title="功能说明"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <template #default>
          <div>• 发布的公告将通过站内信发送给所有正常状态的用户</div>
          <div>• 用户可以在消息中心查看公告</div>
          <div>• 公告标题最多100字，内容最多2000字</div>
        </template>
      </el-alert>

      <!-- 公告历史记录 -->
      <div v-loading="loading">
        <el-empty v-if="announcements.length === 0" description="暂无公告记录" />
        
        <el-timeline v-else>
          <el-timeline-item
            v-for="announcement in announcements"
            :key="announcement.id"
            :timestamp="formatTime(announcement.createTime)"
            placement="top"
          >
            <el-card shadow="hover">
              <div class="announcement-item">
                <div class="announcement-header">
                  <h3>{{ announcement.title }}</h3>
                  <el-tag type="info" size="small">
                    发送给 {{ announcement.recipientCount }} 人
                  </el-tag>
                </div>
                <div class="announcement-content">{{ announcement.content }}</div>
                <div class="announcement-footer">
                  <span class="publisher">发布者：{{ announcement.publisherName }}</span>
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>

    <!-- 发布公告对话框 -->
    <el-dialog
      v-model="publishDialogVisible"
      title="发布全局公告"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="announcementForm"
        :rules="formRules"
        label-width="100px"
        label-position="top"
      >
        <el-alert
          title="重要提示"
          type="warning"
          :closable="false"
          style="margin-bottom: 20px"
        >
          <template #default>
            <div>此操作将向所有用户发送站内信，请谨慎操作！</div>
            <div>发布后无法撤回，请确认公告内容准确无误。</div>
          </template>
        </el-alert>

        <el-form-item label="公告标题" prop="title" required>
          <el-input
            v-model="announcementForm.title"
            placeholder="请输入公告标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="公告内容" prop="content" required>
          <el-input
            v-model="announcementForm.content"
            type="textarea"
            :rows="12"
            placeholder="请输入公告内容，支持换行"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="预览效果">
          <el-card shadow="never" class="preview-card">
            <div class="preview-title">【系统公告】{{ announcementForm.title || '公告标题' }}</div>
            <div class="preview-content">{{ announcementForm.content || '公告内容' }}</div>
          </el-card>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="publishDialogVisible = false" size="large">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="handlePublish"
          :loading="publishing"
          size="large"
        >
          确认发布
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { publishAnnouncement, getAnnouncementHistory, type AnnouncementHistory } from '@/api/admin'

const publishDialogVisible = ref(false)
const publishing = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()
const announcements = ref<AnnouncementHistory[]>([])

const announcementForm = reactive({
  title: '',
  content: ''
})

const formRules: FormRules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
    { min: 1, max: 2000, message: '内容长度在 1 到 2000 个字符', trigger: 'blur' }
  ]
}

const loadAnnouncementHistory = async () => {
  loading.value = true
  try {
    const res = await getAnnouncementHistory()
    announcements.value = res.data || []
  } catch (error: any) {
    ElMessage.error(error.message || '加载公告历史失败')
  } finally {
    loading.value = false
  }
}

const showPublishDialog = () => {
  announcementForm.title = ''
  announcementForm.content = ''
  publishDialogVisible.value = true
}

const formatTime = (time: string) => {
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handlePublish = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    await ElMessageBox.confirm(
      '确定要发布此公告吗？公告将发送给所有用户，发布后无法撤回。',
      '确认发布',
      {
        confirmButtonText: '确定发布',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    publishing.value = true

    await publishAnnouncement({
      title: announcementForm.title,
      content: announcementForm.content
    })

    ElMessage.success('公告发布成功！已发送给所有用户')
    publishDialogVisible.value = false

    // 重置表单
    announcementForm.title = ''
    announcementForm.content = ''
    
    // 刷新公告列表
    loadAnnouncementHistory()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '发布失败')
    }
  } finally {
    publishing.value = false
  }
}

onMounted(() => {
  loadAnnouncementHistory()
})
</script>

<style scoped lang="scss">
.announcement-manage {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .preview-card {
    width: 100%;
    background: #f5f7fa;
    border: 1px solid #e4e7ed;

    :deep(.el-card__body) {
      padding: 16px;
    }

    .preview-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 12px;
    }

    .preview-content {
      font-size: 14px;
      color: #606266;
      line-height: 1.6;
      white-space: pre-wrap;
      word-break: break-word;
    }
  }

  .announcement-item {
    .announcement-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      h3 {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }
    }

    .announcement-content {
      font-size: 14px;
      color: #606266;
      line-height: 1.8;
      white-space: pre-wrap;
      word-break: break-word;
      margin-bottom: 12px;
      padding: 12px;
      background: #f5f7fa;
      border-radius: 4px;
    }

    .announcement-footer {
      display: flex;
      justify-content: flex-end;
      font-size: 13px;
      color: #909399;

      .publisher {
        font-style: italic;
      }
    }
  }

  :deep(.el-timeline-item__timestamp) {
    font-size: 14px;
    color: #909399;
  }
}
</style>
