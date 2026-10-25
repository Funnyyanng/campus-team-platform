<template>
  <el-dialog
    v-model="visible"
    title="完善个人信息"
    width="500px"
    :close-on-click-modal="false"
    :show-close="false"
  >
    <div class="dialog-content">
      <el-alert
        title="请先完善个人信息"
        type="warning"
        :closable="false"
        show-icon
      >
        <template #default>
          <p>为了更好地匹配队友，您需要完善以下信息：</p>
          <ul>
            <li>专业</li>
            <li>年级</li>
            <li>技能标签（至少1个）</li>
          </ul>
        </template>
      </el-alert>
    </div>

    <template #footer>
      <el-button @click="handleCancel">稍后再说</el-button>
      <el-button type="primary" @click="handleGoToProfile">
        去完善信息
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const visible = ref(false)

const emit = defineEmits(['cancel'])

const show = () => {
  visible.value = true
}

const handleCancel = () => {
  visible.value = false
  emit('cancel')
}

const handleGoToProfile = () => {
  visible.value = false
  router.push('/profile')
}

defineExpose({
  show
})
</script>

<style scoped lang="scss">
.dialog-content {
  ul {
    margin: 12px 0 0 0;
    padding-left: 20px;
    
    li {
      margin: 8px 0;
      color: #606266;
    }
  }
}
</style>
