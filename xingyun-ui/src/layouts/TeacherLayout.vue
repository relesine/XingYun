<template>
  <div class="layout">
    <aside class="aside">
      <div class="logo" @click="$router.push('/teacher/dashboard')">
        <span class="logo-mark">星</span>
        <span class="logo-text">星云讲师工作台</span>
      </div>

      <el-menu :default-active="activeMenu" router class="side-menu">
        <el-menu-item-group title="工作台">
          <el-menu-item index="/teacher/dashboard">
            <el-icon><DataLine /></el-icon><span>数据看板</span>
          </el-menu-item>
        </el-menu-item-group>

        <el-menu-item-group title="教学管理">
          <el-menu-item index="/teacher/course">
            <el-icon><Notebook /></el-icon><span>课程管理</span>
          </el-menu-item>
          <el-menu-item index="/teacher/student">
            <el-icon><UserFilled /></el-icon><span>学员进度</span>
          </el-menu-item>
        </el-menu-item-group>

        <el-menu-item-group title="教学互动">
          <el-menu-item index="/teacher/grade">
            <el-icon><EditPen /></el-icon><span>作业批改</span>
          </el-menu-item>
          <el-menu-item index="/teacher/qa">
            <el-icon><ChatDotRound /></el-icon><span>学员答疑</span>
          </el-menu-item>
        </el-menu-item-group>

        <el-menu-item-group title="账号">
          <el-menu-item index="/teacher/profile">
            <el-icon><User /></el-icon><span>个人资料</span>
          </el-menu-item>
        </el-menu-item-group>
      </el-menu>
    </aside>

    <div class="layout-main">
      <header class="header">
        <div class="header-left">
          <span class="header-title">{{ $route.meta.title || '讲师工作台' }}</span>
          <el-tag size="small" type="info" class="role-tag">认证讲师</el-tag>
        </div>
        <div class="header-right">
          <el-button text class="to-student" @click="$router.push('/home')">前往学员端</el-button>
          <el-button v-if="type === 3" text class="to-student" @click="$router.push('/admin/dashboard')">管理后台</el-button>
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
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { authApi } from '@/api'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const nickname = computed(() => store.nickname)
const type = computed(() => store.userType)
// 课时管理归属于课程管理，保持一级菜单高亮
const activeMenu = computed(() => (route.path.startsWith('/teacher/lesson') ? '/teacher/course' : route.path))

async function onCommand(cmd) {
  if (cmd === 'profile') router.push('/teacher/profile')
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
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
}
.logo {
  height: 56px;
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

.side-menu { padding: 8px; }
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
.side-menu :deep(.el-menu-item .el-icon) { font-size: 16px; margin-right: 8px; }

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
