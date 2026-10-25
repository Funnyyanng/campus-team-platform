<template>
  <div class="competition-manage">
    <!-- 操作栏 -->
    <el-card class="action-card">
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建竞赛
      </el-button>
    </el-card>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="关键词">
          <el-input
            v-model="queryForm.keyword"
            placeholder="竞赛名称或主办方"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="级别">
          <el-select v-model="queryForm.level" placeholder="全部" clearable style="width: 120px">
            <el-option label="国家级" value="国家级" />
            <el-option label="省级" value="省级" />
            <el-option label="校级" value="校级" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="进行中" value="ACTIVE" />
            <el-option label="已结束" value="ENDED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 竞赛列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="competitionList"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="竞赛名称" min-width="180" />
        <el-table-column label="级别" width="90">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)" size="small">
              {{ row.level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="类别" width="100" />
        <el-table-column label="队伍人数" width="100">
          <template #default="{ row }">
            {{ row.minTeamSize }}-{{ row.maxTeamSize }}人
          </template>
        </el-table-column>
        <el-table-column prop="organizer" label="主办方" width="140" />
        <el-table-column label="搜索别名" min-width="180">
          <template #default="{ row }">
            <div v-if="row.keywords && row.keywords.length > 0" class="keywords-display">
              <el-tag
                v-for="(keyword, index) in row.keywords.slice(0, 3)"
                :key="index"
                size="small"
                style="margin: 2px"
              >
                {{ keyword }}
              </el-tag>
              <el-tag v-if="row.keywords.length > 3" size="small" type="info" style="margin: 2px">
                +{{ row.keywords.length - 3 }}
              </el-tag>
            </div>
            <span v-else style="color: #909399">未设置</span>
          </template>
        </el-table-column>
        <el-table-column label="报名截止" width="160">
          <template #default="{ row }">
            {{ row.registrationEnd ? formatDate(row.registrationEnd) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ row.status === 'ACTIVE' ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
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
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadCompetitionList"
          @current-change="loadCompetitionList"
        />
      </div>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="竞赛名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入竞赛名称" />
        </el-form-item>
        
        <el-form-item label="竞赛级别" prop="level">
          <el-select v-model="form.level" placeholder="请选择级别" style="width: 100%">
            <el-option label="国家级" value="国家级" />
            <el-option label="省级" value="省级" />
            <el-option label="校级" value="校级" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="竞赛类别">
          <el-select v-model="form.category" placeholder="请选择类别" style="width: 100%">
            <el-option label="学科竞赛" value="学科竞赛" />
            <el-option label="创新创业" value="创新创业" />
            <el-option label="文体竞赛" value="文体竞赛" />
            <el-option label="技能竞赛" value="技能竞赛" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="主办方" prop="organizer">
          <el-input v-model="form.organizer" placeholder="请输入主办方" />
        </el-form-item>
        
        <el-form-item label="队伍人数" required>
          <el-col :span="11">
            <el-form-item prop="minTeamSize">
              <el-input-number 
                v-model="form.minTeamSize" 
                :min="1" 
                :max="20"
                placeholder="最小人数"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="2" style="text-align: center">-</el-col>
          <el-col :span="11">
            <el-form-item prop="maxTeamSize">
              <el-input-number 
                v-model="form.maxTeamSize" 
                :min="1" 
                :max="20"
                placeholder="最大人数"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-form-item>
        
        <el-form-item label="报名时间">
          <el-date-picker
            v-model="form.registrationTime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="竞赛时间">
          <el-date-picker
            v-model="form.competitionTime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="提交截止">
          <el-date-picker
            v-model="form.deadline"
            type="datetime"
            placeholder="选择截止时间"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="参赛费用">
          <el-input-number 
            v-model="form.participationFee" 
            :min="0"
            placeholder="0表示免费"
            style="width: 100%"
          >
            <template #append>元</template>
          </el-input-number>
        </el-form-item>
        
        <el-form-item label="奖项设置">
          <el-input v-model="form.prize" placeholder="如：一等奖、二等奖、三等奖" />
        </el-form-item>
        
        <el-form-item label="官网链接">
          <el-input v-model="form.officialUrl" placeholder="请输入官网链接" />
        </el-form-item>
        
        <el-form-item label="联系方式">
          <el-input v-model="form.contactInfo" placeholder="请输入联系方式" />
        </el-form-item>
        
        <el-form-item label="搜索别名">
          <div class="keywords-section">
            <el-select
              v-model="form.keywords"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="输入别名后回车添加，支持用户通过别名搜索竞赛"
              style="width: 100%; margin-bottom: 12px"
            >
              <el-option
                v-for="keyword in suggestedKeywords"
                :key="keyword"
                :label="keyword"
                :value="keyword"
              />
            </el-select>
            <el-alert
              title="别名说明"
              type="info"
              :closable="false"
              show-icon
            >
              <template #default>
                <div style="font-size: 13px; line-height: 1.6">
                  • 别名用于帮助用户更容易搜索到竞赛，如"数模"可搜到"数学建模"<br>
                  • 可以添加竞赛简称、常用叫法、相关领域关键词等<br>
                  • 建议添加3-8个别名，每个别名不超过10个字<br>
                  • 下拉列表提供常用别名建议，也可以自定义输入
                </div>
              </template>
            </el-alert>
          </div>
        </el-form-item>
        
        <el-form-item label="竞赛描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入竞赛描述"
          />
        </el-form-item>
        
        <el-form-item label="参赛要求">
          <el-input
            v-model="form.requirements"
            type="textarea"
            :rows="3"
            placeholder="请输入参赛要求"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getCompetitionList, 
  createCompetition, 
  updateCompetition, 
  deleteCompetition,
  type Competition 
} from '@/api/competition'
import dayjs from 'dayjs'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新建竞赛')
const competitionList = ref<Competition[]>([])
const total = ref(0)
const formRef = ref<FormInstance>()
const currentId = ref<number | null>(null)

const queryForm = reactive({
  keyword: '',
  level: '',
  status: '',
  page: 1,
  pageSize: 10
})

const form = reactive({
  name: '',
  level: '',
  category: '',
  organizer: '',
  officialUrl: '',
  description: '',
  keywords: [] as string[],
  minTeamSize: 1,
  maxTeamSize: 5,
  registrationTime: null as [Date, Date] | null,
  competitionTime: null as [Date, Date] | null,
  deadline: null as Date | null,
  prize: '',
  participationFee: 0,
  requirements: '',
  contactInfo: ''
})

// 常用别名建议
const suggestedKeywords = [
  // 数学类
  '数模', '数学建模', '美赛', 'MCM', 'ICM', '国赛', '数学竞赛',
  // 计算机类
  '蓝桥杯', '算法竞赛', 'ACM', 'ICPC', '编程', '软件开发', '人工智能', 'AI',
  '大数据', '云计算', '物联网', 'IoT', '网络安全', '信息安全',
  // 电子类
  '电赛', '电子设计', '嵌入式', '单片机', '硬件开发', '电路设计', '智能硬件',
  // 创新创业类
  '挑战杯', '互联网+', '创青春', '创新创业', '商业计划', '创业大赛',
  // 机械类
  '机器人', '机械设计', '工程训练', '3D打印', '智能制造',
  // 外语类
  '英语竞赛', '口语', '写作', '翻译', '演讲',
  // 设计类
  '广告设计', '平面设计', '视频制作', 'UI设计', '交互设计', '工业设计',
  // 学科竞赛
  '物理实验', '化学实验', '生物实验', '结构设计', '力学竞赛',
  // 其他
  '团队协作', '创新思维', '实践能力', '综合素质'
]

const rules: FormRules = {
  name: [{ required: true, message: '请输入竞赛名称', trigger: 'blur' }],
  level: [{ required: true, message: '请选择竞赛级别', trigger: 'change' }],
  organizer: [{ required: true, message: '请输入主办方', trigger: 'blur' }],
  minTeamSize: [{ required: true, message: '请输入最小队伍人数', trigger: 'blur' }],
  maxTeamSize: [{ required: true, message: '请输入最大队伍人数', trigger: 'blur' }]
}

// 加载竞赛列表
const loadCompetitionList = async () => {
  loading.value = true
  try {
    const res = await getCompetitionList(queryForm)
    competitionList.value = res.data.records
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
  loadCompetitionList()
}

// 重置
const handleReset = () => {
  queryForm.keyword = ''
  queryForm.level = ''
  queryForm.status = ''
  queryForm.page = 1
  loadCompetitionList()
}

// 新建
const handleCreate = () => {
  currentId.value = null
  dialogTitle.value = '新建竞赛'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: Competition) => {
  currentId.value = row.id
  dialogTitle.value = '编辑竞赛'
  Object.assign(form, {
    name: row.name,
    level: row.level,
    category: row.category,
    organizer: row.organizer,
    officialUrl: row.officialUrl,
    description: row.description,
    keywords: row.keywords || [],
    minTeamSize: row.minTeamSize || 1,
    maxTeamSize: row.maxTeamSize || 5,
    registrationTime: row.registrationStart && row.registrationEnd 
      ? [new Date(row.registrationStart), new Date(row.registrationEnd)]
      : null,
    competitionTime: row.competitionStart && row.competitionEnd
      ? [new Date(row.competitionStart), new Date(row.competitionEnd)]
      : null,
    deadline: row.deadline ? new Date(row.deadline) : null,
    prize: row.prize || '',
    participationFee: row.participationFee || 0,
    requirements: row.requirements || '',
    contactInfo: row.contactInfo || ''
  })
  dialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    // 验证队伍人数
    if (form.maxTeamSize < form.minTeamSize) {
      ElMessage.error('最大队伍人数不能小于最小队伍人数')
      return
    }

    submitLoading.value = true
    try {
      const data = {
        ...form,
        registrationStart: form.registrationTime?.[0] || null,
        registrationEnd: form.registrationTime?.[1] || null,
        competitionStart: form.competitionTime?.[0] || null,
        competitionEnd: form.competitionTime?.[1] || null
      }
      
      if (currentId.value) {
        await updateCompetition(currentId.value, data)
        ElMessage.success('更新成功')
      } else {
        await createCompetition(data)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadCompetitionList()
    } catch (error) {
      console.error(error)
    } finally {
      submitLoading.value = false
    }
  })
}

// 删除
const handleDelete = async (row: Competition) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除竞赛 ${row.name} 吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    await deleteCompetition(row.id)
    ElMessage.success('删除成功')
    loadCompetitionList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

// 重置表单
const resetForm = () => {
  form.name = ''
  form.level = ''
  form.category = ''
  form.organizer = ''
  form.officialUrl = ''
  form.description = ''
  form.keywords = []
  form.minTeamSize = 1
  form.maxTeamSize = 5
  form.registrationTime = null
  form.competitionTime = null
  form.deadline = null
  form.prize = ''
  form.participationFee = 0
  form.requirements = ''
  form.contactInfo = ''
  formRef.value?.clearValidate()
}

// 格式化日期
const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 获取级别标签类型
const getLevelType = (level: string) => {
  const map: Record<string, any> = {
    '国家级': 'danger',
    '省级': 'warning',
    '校级': 'primary'
  }
  return map[level] || 'info'
}

onMounted(() => {
  loadCompetitionList()
})
</script>

<style scoped lang="scss">
.competition-manage {
  .action-card {
    margin-bottom: 20px;
  }

  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
    .keywords-display {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;
    }

    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .keywords-section {
    width: 100%;

    :deep(.el-alert) {
      margin-top: 8px;
    }

    :deep(.el-alert__content) {
      padding: 0;
    }
  }
}
</style>
