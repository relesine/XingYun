<template>
  <div class="nf-wrap">
    <div class="nf-code">404</div>
    <div class="nf-text">页面不存在或已被移除</div>
    <div class="nf-actions">
      <el-button type="primary" @click="goHome">返回首页</el-button>
      <el-button @click="goBack">返回上一页</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { homeByType } from '@/router'
const router = useRouter()
const store = useUserStore()
function goHome() {
  router.replace(store.userType ? homeByType(store.userType) : '/login')
}
function goBack() { router.go(-1) }
</script>

<style scoped>
.nf-wrap {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: var(--xy-bg);
}

.nf-code {
  font-size: 72px;
  font-weight: 600;
  letter-spacing: 2px;
  color: #d5d8dd;
}

.nf-text {
  font-size: 15px;
  color: var(--xy-text-2);
}

.nf-actions {
  margin-top: 12px;
}
</style>
