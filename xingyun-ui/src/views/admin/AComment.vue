<template>
  <div class="page-card">
    <div class="page-head">
      <h1 class="page-title">评论管理</h1>
      <span class="page-sub">学员课程评价的展示与屏蔽</span>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="courseId" label="课程ID" width="90" />
      <el-table-column prop="nickname" label="用户" width="130" />
      <el-table-column label="评分" width="150">
        <template #default="{ row }"><el-rate :model-value="row.score" disabled size="small" /></template>
      </el-table-column>
      <el-table-column prop="content" label="评论内容" min-width="240" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="['warning', 'success', 'info'][row.status]">{{ ['待审核', '显示', '屏蔽'][row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="160">
        <template #default="{ row }">{{ minuteText(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button v-if="row.status !== 1" size="small" type="primary" @click="setStatus(row, 1)">设为显示</el-button>
          <el-button v-if="row.status !== 2" size="small" type="danger" @click="setStatus(row, 2)">屏蔽</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="pager" background layout="prev,pager,next,total" :total="total" :page-size="query.size"
      :current-page="query.page" @current-change="onPage" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api'
import { minuteText } from '@/utils/format'
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10 })
async function load() {
  loading.value = true
  try { const d = await adminApi.commentPage({ ...query }); list.value = d.records; total.value = d.total } finally { loading.value = false }
}
function onPage(p) { query.page = p; load() }
async function setStatus(row, status) {
  await adminApi.commentStatus(row.id, status)
  ElMessage.success('已更新'); load()
}
onMounted(load)
</script>
