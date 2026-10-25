<template>
  <div class="layout">
    <aside class="aside">
      <div class="logo" @click="$router.push('/admin/dashboard')">
        <span class="logo-mark">星</span>
        <span class="logo-text">星云管理后台</span>
      </div>

      <el-menu :default-active="$route.path" router class="side-menu">
        <el-menu-item-group title="概览">
          <el-menu-item index="/admin/dashboard">
            <el-icon><Odometer /></el-icon><span>运营大盘</span>
          </el-menu-item>
        </el-menu-item-group>

        <el-menu-item-group title="内容运营">
          <el-menu-item index="/admin/course">
            <el-icon><Notebook /></el-icon><span>课程审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/category">
            <el-icon><Menu /></el-icon><span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/announcement">
            <el-icon><Bell /></el-icon><span>公告管理</span>
          </el-menu-item>
        </el-menu-item-group>

        <el-menu-item-group title="交易与用户">
          <el-menu-item index="/admin/order">
            <el-icon><List /></el-icon><span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/coupon">
            <el-icon><Discount /></el-icon><span>优惠券</span>
          </el-menu-item>
          <el-menu-item index="/admin/user">
            <el-icon><UserFilled /></el-icon><span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/comment">
            <el-icon><ChatLineSquare /></el-icon><span>评论管理</span>
          </el-menu-item>
        </el-menu-item-group>
      </el-menu>
    </aside>

    <div class="layout-main">
      <header class="header">
        <div class="header-left">
          <span class="header-title">{{ $route.meta.title || '管理后台' }}</span>
          <el-tag size="small" type="info" class="role-tag">平台管理员</el-tag>
        </div>
        <div class="header-right">
          <el-button text class="to-student" @click="$router.push('/home')">前往学员端</el-button>
          <el-dropdown @command="onCommand" trigger="click">
            <span class="user">
              <el-avatar :size="28">{{ nickname.charAt(0) }}</el-avatar>
              <span class="uname">{{ nickname }}</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade"><component :is="Component" /></transition>
        </router-view>
      </main>
      <el-backtop :right="40" :bottom="40" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { authApi } from '@/api'

const router = useRouter()
const store = useUserStore()
const nickname = computed(() => store.nickname)

async function onCommand(cmd) {
  if (cmd === 'profile') router.push('/admin/profile')
  else if (cmd === 'logout') {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' }).catch(() => {})
    try { await authApi.logout() } catch (e) { /* 忽略退出接口异常 */ }
    store.clear()
    router.replace('/login')
  }
}
</script>

<style scoped>
.layout { min-height: 100vh; display: flex; background: var(--xy-bg); }

.aside {
  width: 216px;
  flex-shrink: 0;
  background: var(--xy-surface);
  border-right: 1px solid var(--xy-border);
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
}
.logo {
  height: 56px; flex-shrink: 0;
  display: flex; align-items: center; gap: 8px;
  padding: 0 16px;
  border-bottom: 1px solid var(--xy-border-light);
  cursor: pointer;
}
.logo-mark {
  width: 26px; height: 26px; border-radius: 5px;
  background: var(--xy-primary); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 600;
}
.logo-text { font-size: 15px; font-weight: 600; color: var(--xy-text); }

.side-menu { padding: 8px; flex: 1; }
.side-menu :deep(.el-menu-item-group__title) {
  padding: 14px 10px 4px;
  font-size: 12px;
  color: var(--xy-text-3);
  line-height: 18px;
}
.side-menu :deep(.el-menu-item) {
  height: 38px; line-height: 38px;
  border-radius: var(--xy-radius-sm);
  margin-bottom: 2px;
  padding-left: 10px !important;
  font-size: 13px;
}
.side-menu :deep(.el-menu-item .el-icon) { font-size: 16px; margin-right: 8px; color: inherit; }

.layout-main { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.header {
  height: 56px;
  background: var(--xy-surface);
  border-bottom: 1px solid var(--xy-border);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 20px;
  position: sticky; top: 0; z-index: 50;
}
.header-left { display: flex; align-items: center; gap: 10px; }
.header-title { font-size: 15px; font-weight: 600; color: var(--xy-text); }
.role-tag { border: none; background: #f2f3f5; color: var(--xy-text-2); }
.header-right { display: flex; align-items: center; gap: 8px; }
.to-student { font-size: 13px; color: var(--xy-text-2); }
.user { display: flex; align-items: center; gap: 6px; cursor: pointer; padding-left: 8px; }
.uname { font-size: 13px; color: var(--xy-text); }
.arrow { font-size: 12px; color: var(--xy-text-3); }

.main { flex: 1; padding: 16px; }

@media (max-width: 900px) {
  .aside { width: 64px; }
  .logo-text, .side-menu :deep(.el-menu-item span) { display: none; }
  .side-menu :deep(.el-menu-item-group__title) { display: none; }
  .side-menu :deep(.el-menu-item) { padding-left: 16px !important; }
}
</style>
