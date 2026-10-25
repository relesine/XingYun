<template>
  <div class="mb12">
    <div class="page-head">
      <h1 class="page-title">个人资料</h1>
      <span class="page-sub">账号信息与密码安全</span>
    </div>
  </div>
  <el-row :gutter="16">
    <el-col :md="7" :sm="24" class="mb12">
      <div class="page-card stat-card">
        <el-avatar :size="72" :src="form.avatar">{{ username.charAt(0) }}</el-avatar>
        <h3 class="mt12">{{ username }}</h3>
        <div class="text-gray">{{ typeName }} · 累计学习 {{ stat.studyMinutes || 0 }} 分钟 · {{ stat.integral || 0 }} 积分</div>
        <el-divider />
        <el-statistic title="在学课程" :value="stat.learnedCourseCount || 0" />
        <el-statistic class="mt12" title="完成课时" :value="stat.finishedLessonCount || 0" />
      </div>
    </el-col>

    <el-col :md="17" :sm="24">
      <div class="page-card mb12">
        <div class="section-title mb12">基本资料</div>
        <el-form :model="form" label-width="90px" style="max-width:560px">
          <el-form-item label="头像">
            <el-upload :action="uploadUrl" :headers="headers" name="file" :data="{ type: 'avatar' }"
              :show-file-list="false" :on-success="onAvatar">
              <el-avatar :size="56" :src="form.avatar">点我上传</el-avatar>
            </el-upload>
          </el-form-item>
          <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="form.gender"><el-radio :label="1">男</el-radio><el-radio
                :label="2">女</el-radio></el-radio-group>
          </el-form-item>
          <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
          <el-form-item label="头衔" v-if="form.title !== undefined"><el-input v-model="form.title"
              placeholder="讲师头衔，如：高级架构师" /></el-form-item>
          <el-form-item label="简介"><el-input v-model="form.intro" type="textarea" :rows="3" /></el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveProfile">保存资料</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="page-card">
        <div class="section-title mb12">修改密码</div>
        <el-form :model="pwd" label-width="90px" style="max-width:560px">
          <el-form-item label="原密码"><el-input v-model="pwd.oldPassword" type="password" show-password /></el-form-item>
          <el-form-item label="新密码"><el-input v-model="pwd.newPassword" type="password" show-password
              placeholder="至少6位" /></el-form-item>
          <el-form-item>
            <el-button type="primary" @click="changePwd">确认修改</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { authApi, learnApi, uploadUrl } from '@/api'
import { useUserStore } from '@/store/user'
import { safeJsonParse } from '@/utils/safeParse'

const store = useUserStore()
const username = computed(() => store.username)
const typeName = computed(() => {
  const map = { 1: '学员', 2: '讲师', 3: '管理员' }
  return map[store.userType] || '用户'
})
const headers = computed(() => ({ Authorization: `Bearer ${store.token}` }))
const form = reactive({ nickname: '', gender: 1, avatar: '', phone: '', email: '', intro: '', title: '' })
const pwd = reactive({ oldPassword: '', newPassword: '' })
const stat = ref({})

function onAvatar(res) {
  // 后端统一返回 Result，data 为文件可访问 URL
  if (res.code === 200) { form.avatar = res.data.url; ElMessage.success('头像已上传，记得保存资料') }
  else ElMessage.error(res.msg || '上传失败')
}
async function saveProfile() {
  await authApi.updateProfile(form)
  ElMessage.success('资料已保存')
  // 同步刷新本地缓存的昵称头像
  const fresh = await authApi.info()
  const raw = safeJsonParse('xy_user')
  if (raw) {
    raw.userInfo = fresh
    store.setLogin(raw)
  }
}
async function changePwd() {
  if (!pwd.oldPassword || !pwd.newPassword) return ElMessage.warning('请填写完整')
  await authApi.changePwd(pwd)
  ElMessage.success('密码修改成功，下次登录请使用新密码')
  pwd.oldPassword = ''; pwd.newPassword = ''
}
onMounted(async () => {
  const info = await authApi.info()
  Object.assign(form, {
    nickname: info.nickname, gender: info.gender ?? 1, avatar: info.avatar || '',
    phone: info.phone || '', email: info.email || '', intro: info.intro || '', title: info.title || ''
  })
  try { stat.value = await learnApi.profile() } catch (e) { }
})
</script>

<style scoped>
.stat-card {
  text-align: center;
  padding: 24px 16px;
}
</style>
