<template>
  <div class="layout">
    <!-- 顶部导航：白底 + 1px 分隔线，信息密度对标在线教育头部产品 -->
    <header class="header">
      <div class="header-inner">
        <div class="logo" @click="$router.push('/home')">
          <span class="logo-mark">星</span>
          <span class="logo-text">星云课堂</span>
        </div>

        <el-menu class="menu" mode="horizontal" :default-active="activeMenu" router :ellipsis="false">
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/courses">全部课程</el-menu-item>
          <el-menu-item index="/my">我的学习</el-menu-item>
          <el-menu-item index="/orders">我的订单</el-menu-item>
          <el-menu-item index="/coupons">优惠券</el-menu-item>
        </el-menu>

        <div class="header-search">
          <el-input
            v-model="keyword"
            placeholder="搜索课程"
            clearable
            @keyup.enter="search"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>

        <div class="right">
          <el-button
            v-if="type === 2 || type === 3"
            text
            class="role-entry"
            @click="$router.push('/teacher/dashboard')"
          >讲师工作台</el-button>
          <el-button
            v-if="type === 3"
            text
            class="role-entry"
            @click="$router.push('/admin/dashboard')"
          >管理后台</el-button>

          <el-dropdown @command="onCommand" trigger="click">
            <span class="user">
              <el-avatar :size="28" :src="avatarUrl">{{ nickname.charAt(0) }}</el-avatar>
              <span class="uname">{{ nickname }}</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
                <el-dropdown-item command="certificates">我的证书</el-dropdown-item>
                <el-dropdown-item command="wrongbook">错题本</el-dropdown-item>
                <el-dropdown-item command="profile" divided>个人资料</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="main">
      <router-view v-slot="{ Component }">
        <transition name="fade"><component :is="Component" /></transition>
      </router-view>
    </main>

    <footer class="footer">
      <div class="footer-inner">
        <span>© 2026 星云课堂 · 知识点亮星云</span>
        <span class="footer-links">课程服务 · 学习中心 · 帮助反馈</span>
      </div>
    </footer>
    <el-backtop :right="40" :bottom="40" />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { authApi } from '@/api'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const nickname = computed(() => store.nickname)
const type = computed(() => store.userType)
const avatarUrl = computed(() => store.userInfo?.userInfo?.avatar || '')
const keyword = ref('')

// 详情/学习等子页面归属到对应一级导航，保持导航高亮稳定
const activeMenu = computed(() => {
  const p = route.path
  if (p.startsWith('/course') || p === '/courses') return '/courses'
  if (p.startsWith('/learn') || p === '/my') return '/my'
  if (p.startsWith('/orders')) return '/orders'
  if (p.startsWith('/coupons')) return '/coupons'
  return p
})

function search() {
  router.push({ path: '/courses', query: keyword.value ? { keyword: keyword.value } : {} })
}

async function onCommand(cmd) {
  if (cmd === 'profile') router.push('/profile')
  else if (cmd === 'favorites') router.push('/favorites')
  else if (cmd === 'certificates') router.push('/certificates')
  else if (cmd === 'wrongbook') router.push('/wrongbook')
  else if (cmd === 'logout') {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' }).catch(() => {})
    try { await authApi.logout() } catch (e) { /* 退出接口异常不阻断本地清理 */ }
    store.clear()
    router.replace('/login')
  }
}
</script>

<style scoped>
.layout { min-height: 100vh; display: flex; flex-direction: column; }

.header {
  height: 60px;
  background: var(--xy-surface);
  border-bottom: 1px solid var(--xy-border);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1200px;
  height: 100%;
  margin: 0 auto;
  padding: 0 16px;
  display: flex;
  align-items: center;
  gap: 20px;
}
.logo { display: flex; align-items: center; gap: 8px; cursor: pointer; flex-shrink: 0; }
.logo-mark {
  width: 28px; height: 28px; border-radius: 6px;
  background: var(--xy-primary); color: #fff;
  font-size: 15px; font-weight: 600;
  display: flex; align-items: center; justify-content: center;
}
.logo-text { font-size: 17px; font-weight: 600; color: var(--xy-text); letter-spacing: 0.5px; }

.menu { flex: 0 1 auto; border-bottom: none !important; height: 60px; }
.menu :deep(.el-menu-item) {
  height: 60px;
  line-height: 60px;
  padding: 0 14px;
  font-size: 14px;
  color: var(--xy-text-2);
  border-bottom: 2px solid transparent;
}
.menu :deep(.el-menu-item:hover) { background: transparent !important; color: var(--xy-text); }
.menu :deep(.el-menu-item.is-active) {
  background: transparent !important;
  color: var(--xy-text) !important;
  font-weight: 500;
  border-bottom-color: var(--xy-primary);
}

.header-search { width: 180px; margin-left: auto; }
.header-search :deep(.el-input__wrapper) { background: #f7f8fa; box-shadow: none !important; }
.header-search :deep(.el-input__wrapper.is-focus) { background: #fff; box-shadow: 0 0 0 1px var(--xy-primary) inset !important; }
.header-search :deep(.el-input__inner) { font-size: 13px; }

.right { display: flex; align-items: center; gap: 4px; flex-shrink: 0; }
.role-entry { color: var(--xy-text-2); font-size: 13px; padding: 0 6px; }
.user { display: flex; align-items: center; gap: 6px; cursor: pointer; padding-left: 8px; }
.uname { font-size: 13px; color: var(--xy-text); max-width: 88px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.arrow { color: var(--xy-text-3); font-size: 12px; }

.main { flex: 1; width: 100%; max-width: 1200px; margin: 0 auto; padding: 16px; }

.footer { border-top: 1px solid var(--xy-border); background: var(--xy-surface); }
.footer-inner {
  max-width: 1200px; margin: 0 auto; padding: 18px 16px;
  display: flex; justify-content: space-between; align-items: center;
  font-size: 12px; color: var(--xy-text-3);
}
.footer-links { color: var(--xy-text-3); }

@media (max-width: 1080px) {
  .header-search { display: none; }
  .menu :deep(.el-menu-item) { padding: 0 10px; }
}
@media (max-width: 860px) {
  .role-entry { display: none; }
  .logo-text { display: none; }
}
@media (max-width: 640px) {
  .menu :deep(.el-menu-item) { padding: 0 8px; font-size: 13px; }
  .uname { display: none; }
  .footer-inner { flex-direction: column; gap: 6px; }
}
</style>
