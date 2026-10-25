<template>
  <div class="profile-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <svg class="logo-icon" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2"/>
            <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2"/>
            <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2"/>
          </svg>
          <span class="logo-text">UniTeam</span>
        </div>
        <el-button @click="$router.push('/')">返回首页</el-button>
      </div>
    </header>

    <div class="container">
      <div class="profile-header">
        <h1>个人信息</h1>
        <p>完善你的个人信息，让队友更了解你</p>
      </div>

      <el-card class="profile-card">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
        >
          <!-- 头像 -->
          <el-form-item label="头像">
            <div class="avatar-upload">
              <el-avatar :src="form.avatar" :size="80" />
              <div class="avatar-tip">
                <p>{{ form.avatar?.includes('dicebear') ? '当前使用自动生成头像' : '自定义头像' }}</p>
                <el-upload
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                  :http-request="handleAvatarUpload"
                  accept="image/jpeg,image/jpg,image/png,image/gif"
                >
                  <el-button size="small" :loading="uploadLoading">
                    {{ uploadLoading ? '上传中...' : '更换头像' }}
                  </el-button>
                </el-upload>
              </div>
            </div>
          </el-form-item>

          <!-- 昵称 -->
          <el-form-item label="昵称" prop="nickname">
            <el-input
              v-model="form.nickname"
              placeholder="请输入昵称"
              maxlength="20"
              show-word-limit
            />
          </el-form-item>

          <!-- 邮箱（只读） -->
          <el-form-item label="邮箱">
            <el-input v-model="form.email" disabled />
          </el-form-item>

          <!-- 学院 -->
          <el-form-item label="学院" prop="college">
            <el-input
              v-model="form.college"
              placeholder="请输入学院"
            />
          </el-form-item>

          <!-- 专业 -->
          <el-form-item label="专业" prop="major">
            <el-input
              v-model="form.major"
              placeholder="请输入专业"
            />
          </el-form-item>

          <!-- 年级 -->
          <el-form-item label="年级" prop="grade">
            <el-select v-model="form.grade" placeholder="请选择年级">
              <el-option label="大一" value="大一" />
              <el-option label="大二" value="大二" />
              <el-option label="大三" value="大三" />
              <el-option label="大四" value="大四" />
              <el-option label="研一" value="研一" />
              <el-option label="研二" value="研二" />
              <el-option label="研三" value="研三" />
              <el-option label="博一" value="博一" />
              <el-option label="博二" value="博二" />
              <el-option label="博三" value="博三" />
              <el-option label="博四" value="博四" />
            </el-select>
          </el-form-item>

          <!-- 微信号 -->
          <el-form-item label="微信号">
            <el-input
              v-model="form.wechat"
              placeholder="请输入微信号"
            >
              <template #append>
                <el-icon><Lock /></el-icon>
                加密
              </template>
            </el-input>
            <div class="form-tip">仅组队成功后对方可见</div>
          </el-form-item>

          <!-- QQ号 -->
          <el-form-item label="QQ号">
            <el-input
              v-model="form.qq"
              placeholder="请输入QQ号"
            >
              <template #append>
                <el-icon><Lock /></el-icon>
                加密
              </template>
            </el-input>
            <div class="form-tip">仅组队成功后对方可见</div>
          </el-form-item>

          <!-- 技能标签 -->
          <el-form-item label="技能标签">
            <div class="skills-container">
              <el-tag
                v-for="skill in form.skills"
                :key="skill"
                closable
                @close="removeSkill(skill)"
                style="margin-right: 8px; margin-bottom: 8px"
              >
                {{ skill }}
              </el-tag>
              <el-input
                v-if="skillInputVisible"
                ref="skillInputRef"
                v-model="skillInputValue"
                size="small"
                style="width: 120px"
                @keyup.enter="handleSkillInputConfirm"
                @blur="handleSkillInputConfirm"
              />
              <el-button
                v-else
                size="small"
                @click="showSkillInput"
                :disabled="form.skills.length >= 5"
              >
                + 添加技能
              </el-button>
            </div>
            <div class="form-tip">最多添加5个技能标签</div>
            <div class="preset-skills">
              <span class="preset-label">常用技能：</span>
              <el-tag
                v-for="skill in presetSkills"
                :key="skill"
                size="small"
                style="margin-right: 8px; cursor: pointer"
                @click="addPresetSkill(skill)"
              >
                {{ skill }}
              </el-tag>
            </div>
          </el-form-item>

          <!-- 反向邀约开关 -->
          <el-form-item label="接受邀请">
            <el-switch v-model="form.allowInvite" />
            <div class="form-tip">开启后，队长可以主动搜索并邀请你</div>
          </el-form-item>

          <!-- 提交按钮 -->
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleSubmit">
              保存信息
            </el-button>
            <el-button @click="$router.push('/')">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules, UploadRequestOptions } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getUserProfile, updateUserProfile } from '@/api/user'
import { uploadAvatar } from '@/api/file'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const uploadLoading = ref(false)
const skillInputVisible = ref(false)
const skillInputValue = ref('')
const skillInputRef = ref()

const form = reactive({
  email: '',
  nickname: '',
  avatar: '',
  college: '',
  major: '',
  grade: '',
  wechat: '',
  qq: '',
  skills: [] as string[],
  allowInvite: true
})

const rules: FormRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  college: [
    { required: true, message: '请输入学院', trigger: 'blur' }
  ],
  major: [
    { required: true, message: '请输入专业', trigger: 'blur' }
  ],
  grade: [
    { required: true, message: '请选择年级', trigger: 'change' }
  ]
}

const presetSkills = [
  'Python', 'Java', 'C++', 'JavaScript',
  '数据分析', '机器学习', '前端开发', '后端开发',
  '论文写作', '视频剪辑', 'PPT制作', '数学建模'
]

// 加载用户信息
const loadProfile = async () => {
  try {
    const res = await getUserProfile()
    Object.assign(form, res.data)
  } catch (error) {
    console.error(error)
  }
}

// 上传前验证
const beforeAvatarUpload = (file: File) => {
  const isImage = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只支持 JPG、PNG、GIF 格式的图片')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

// 自定义上传
const handleAvatarUpload = async (options: UploadRequestOptions) => {
  uploadLoading.value = true
  try {
    const res = await uploadAvatar(options.file as File)
    form.avatar = res.data.url
    ElMessage.success('头像上传成功')
  } catch (error) {
    console.error(error)
  } finally {
    uploadLoading.value = false
  }
}

// 移除技能
const removeSkill = (skill: string) => {
  form.skills = form.skills.filter(s => s !== skill)
}

// 显示技能输入框
const showSkillInput = () => {
  skillInputVisible.value = true
  nextTick(() => {
    skillInputRef.value?.focus()
  })
}

// 确认添加技能
const handleSkillInputConfirm = () => {
  if (skillInputValue.value && form.skills.length < 5) {
    if (!form.skills.includes(skillInputValue.value)) {
      form.skills.push(skillInputValue.value)
    }
  }
  skillInputVisible.value = false
  skillInputValue.value = ''
}

// 添加预设技能
const addPresetSkill = (skill: string) => {
  if (form.skills.length >= 5) {
    ElMessage.warning('最多只能添加5个技能标签')
    return
  }
  if (!form.skills.includes(skill)) {
    form.skills.push(skill)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await updateUserProfile({
        nickname: form.nickname,
        avatar: form.avatar,
        college: form.college,
        major: form.major,
        grade: form.grade,
        wechat: form.wechat,
        qq: form.qq,
        skills: form.skills,
        allowInvite: form.allowInvite
      })
      // 更新本地用户信息
      userStore.setUserInfo({
        ...userStore.userInfo!,
        nickname: form.nickname,
        avatar: form.avatar,
        college: form.college,
        major: form.major,
        grade: form.grade,
        allowInvite: form.allowInvite
      })
      ElMessage.success('保存成功')
      router.push('/')
    } catch (error) {
      console.error(error)
    } finally {
      loading.value = false
    }
  })
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped lang="scss">
.profile-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

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

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 24px;
}

.profile-header {
  margin-bottom: 24px;

  h1 {
    font-size: 28px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px;
  }

  p {
    color: #909399;
    margin: 0;
  }
}

.profile-card {
  .avatar-upload {
    display: flex;
    align-items: center;
    gap: 16px;

    .avatar-tip {
      p {
        margin: 0 0 8px;
        color: #606266;
        font-size: 14px;
      }
    }
  }

  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }

  .skills-container {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
  }

  .preset-skills {
    margin-top: 12px;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 4px;

    .preset-label {
      font-size: 12px;
      color: #909399;
      margin-right: 8px;
    }
  }
}
</style>
