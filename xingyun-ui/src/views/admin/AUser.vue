<template>
  <div class="page-card">
    <div class="page-head">
      <h1 class="page-title">用户管理</h1>
      <span class="page-sub">平台注册用户、身份与账号状态管理</span>
    </div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="账号/昵称/手机号" style="width:220px" clearable @keyup.enter="reload" />
      <el-select v-model="query.userType" placeholder="身份" clearable style="width:130px" @change="reload">
        <el-option :value="1" label="学员" /><el-option :value="2" label="讲师" /><el-option :value="3" label="管理员" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width:120px" @change="reload">
        <el-option :value="0" label="正常" /><el-option :value="1" label="禁用" />
      </el-select>
      <el-button type="primary" @click="reload">查询</el-button>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="账号" width="130" />
      <el-table-column prop="nickname" label="昵称" width="130" />
      <el-table-column label="身份" width="90">
        <template #default="{ row }">
          <el-tag :type="['', 'success', 'warning', 'danger'][row.userType]">{{ ['', '学员', '讲师', '管理员'][row.userType]
            }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'info'">{{ userStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="170">
        <template #default="{ row }">{{ minuteText(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" :type="row.status === 0 ? 'warning' : 'primary'" @click="toggle(row)">
            {{ row.status === 0 ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" @click="resetPwd(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="pager" background layout="prev,pager,next,total" :total="total" :page-size="query.size"
      :current-page="query.page" @current-change="onPage" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api'
import { minuteText, userStatusText } from '@/utils/format'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, keyword: '', userType: null, status: null })
async function load() {
  loading.value = true
  try { const d = await adminApi.userPage({ ...query }); list.value = d.records; total.value = d.total } finally { loading.value = false }
}
function reload() { query.page = 1; load() }
function onPage(p) { query.page = p; load() }
async function toggle(row) {
  await adminApi.userStatus(row.id, row.status === 0 ? 1 : 0)
  ElMessage.success('状态已更新'); load()
}
async function resetPwd(row) {
  await ElMessageBox.confirm(`将用户【${row.nickname}】的密码重置为系统默认密码？`, '提示', { type: 'warning' })
    .catch(() => Promise.reject()).then(async () => {
      await adminApi.resetPwd(row.id); ElMessage.success('已重置为系统默认密码')
    }).catch(() => { })
}
onMounted(load)
</script>
