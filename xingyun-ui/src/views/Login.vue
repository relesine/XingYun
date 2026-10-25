<template>
  <div class="login-page">
    <div class="login-card">
      <!-- 品牌信息区：小屏隐藏 -->
      <aside class="brand-panel">
        <div class="brand-top">
          <span class="brand-mark">星</span>
          <span class="brand-name">星云课堂</span>
        </div>
        <h1>系统学习，稳步进阶</h1>
        <p class="brand-desc">面向开发、数据与设计方向的职业技能课程平台，学习进度与证书全程记录。</p>
        <ul class="brand-points">
          <li>付费课程前 3 节免费试看</li>
          <li>学习进度自动保存，支持断点续播</li>
          <li>讲师答疑，完成课时自动颁发证书</li>
        </ul>
        <div class="brand-stats">
          <div><strong>12</strong><span>门精品课程</span></div>
          <div><strong>5</strong><span>位认证讲师</span></div>
          <div><strong>3</strong><span>类角色入口</span></div>
        </div>
      </aside>

      <section class="form-panel">
        <div class="form-head">
          <h2>登录星云课堂</h2>
          <p>学员、讲师、管理员使用统一账号登录</p>
        </div>

        <el-tabs v-model="mode" class="auth-tabs">
          <el-tab-pane label="账号登录" name="login">
            <el-form ref="loginRef" :model="loginForm" :rules="loginRules" @keyup.enter="doLogin">
              <el-form-item prop="username">
                <el-input
                  v-model="loginForm.username"
                  size="large"
                  placeholder="用户名"
                  :prefix-icon="User"
                  clearable
                />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  size="large"
                  show-password
                  placeholder="密码"
                  :prefix-icon="Lock"
                />
              </el-form-item>
              <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="doLogin">
                登录
              </el-button>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="注册学员账号" name="register">
            <el-form ref="regRef" :model="regForm" :rules="regRules" label-position="top">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="regForm.username" placeholder="4-20 位字母或数字" />
              </el-form-item>
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="regForm.nickname" placeholder="2-12 个字，用于学习记录展示" />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model="regForm.password" type="password" show-password placeholder="至少 6 位" />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirm">
                <el-input v-model="regForm.confirm" type="password" show-password placeholder="再次输入密码" />
              </el-form-item>
              <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="doRegister">
                注册
              </el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </section>
    </div>
    <div class="login-footer">© 2026 星云课堂 · 知识点亮星云</div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { authApi } from '@/api'
import { useUserStore } from '@/store/user'
import { homeByType } from '@/router'

const router = useRouter()
const store = useUserStore()
const mode = ref('login')
const loading = ref(false)
const loginRef = ref(null)
const regRef = ref(null)
const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', nickname: '', password: '', confirm: '' })

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const regRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9]{4,20}$/, message: '用户名为 4-20 位字母或数字', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 12, message: '昵称 2-12 个字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: (rule, value, cb) => (value === regForm.password ? cb() : cb(new Error('两次输入的密码不一致'))), trigger: 'blur' }
  ]
}

async function doLogin() {
  if (!loginRef.value) return
  await loginRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const vo = await authApi.login({ ...loginForm })
      store.setLogin(vo)
      ElMessage.success('登录成功')
      router.replace(homeByType(vo.userInfo.userType))
    } catch (e) {
      // 请求层已统一提示
    } finally {
      loading.value = false
    }
  })
}

async function doRegister() {
  if (!regRef.value) return
  await regRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await authApi.register({ username: regForm.username, nickname: regForm.nickname, password: regForm.password })
      ElMessage.success('注册成功，请使用新账号登录')
      loginForm.username = regForm.username
      mode.value = 'login'
    } catch (e) { /* 请求层已统一提示 */ } finally { loading.value = false }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  background: var(--xy-bg);
}
.login-card {
  width: 860px;
  max-width: 100%;
  display: grid;
  grid-template-columns: 340px minmax(0, 1fr);
  background: var(--xy-surface);
  border: 1px solid var(--xy-border);
  border-radius: 8px;
  overflow: hidden;
}

.brand-panel {
  padding: 32px 28px;
  background: #f6f9f7;
  border-right: 1px solid var(--xy-border-light);
  display: flex;
  flex-direction: column;
}
.brand-top { display: flex; align-items: center; gap: 8px; }
.brand-mark {
  width: 28px; height: 28px; border-radius: 6px;
  background: var(--xy-primary); color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 15px; font-weight: 600;
}
.brand-name { font-size: 16px; font-weight: 600; color: var(--xy-text); }
.brand-panel h1 { margin-top: 36px; font-size: 22px; line-height: 1.5; font-weight: 600; color: var(--xy-text); }
.brand-desc { margin-top: 10px; font-size: 13px; line-height: 1.8; color: var(--xy-text-2); }
.brand-points { margin-top: 16px; list-style: none; display: flex; flex-direction: column; gap: 8px; }
.brand-points li {
  position: relative; padding-left: 12px;
  font-size: 13px; color: var(--xy-text-2);
}
.brand-points li::before {
  content: ''; position: absolute; left: 0; top: 9px;
  width: 4px; height: 4px; border-radius: 50%;
  background: var(--xy-primary);
}
.brand-stats {
  margin-top: auto; padding-top: 28px;
  display: grid; grid-template-columns: repeat(3, 1fr);
  border-top: 1px solid var(--xy-border);
}
.brand-stats strong { display: block; font-size: 20px; font-weight: 600; color: var(--xy-text); }
.brand-stats span { font-size: 12px; color: var(--xy-text-3); }

.form-panel { padding: 32px 36px; }
.form-head h2 { font-size: 20px; font-weight: 600; color: var(--xy-text); }
.form-head p { margin-top: 6px; font-size: 13px; color: var(--xy-text-3); }
.auth-tabs { margin-top: 12px; }
.auth-tabs :deep(.el-tabs__header) { margin-bottom: 20px; }
.auth-tabs :deep(.el-tabs__item) { padding: 0 16px; height: 40px; line-height: 40px; }
.submit-btn { width: 100%; height: 40px; margin-top: 4px; }

.login-footer { margin-top: 20px; font-size: 12px; color: var(--xy-text-3); }

@media (max-width: 820px) {
  .login-card { grid-template-columns: 1fr; width: 420px; }
  .brand-panel { display: none; }
  .form-panel { padding: 26px 22px; }
}
</style>
