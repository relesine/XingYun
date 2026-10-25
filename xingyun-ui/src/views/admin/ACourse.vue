<template>
  <div class="page-card">
    <div class="page-head">
      <h1 class="page-title">课程审核</h1>
      <span class="page-sub">讲师提交的课程审核通过后上架</span>
    </div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="课程名称" style="width:200px" clearable @keyup.enter="reload" />
      <el-select v-model="query.auditStatus" placeholder="审核状态" clearable style="width:140px" @change="reload">
        <el-option :value="0" label="草稿" /><el-option :value="1" label="待审核" />
        <el-option :value="2" label="已通过" /><el-option :value="3" label="已驳回" />
      </el-select>
      <el-button type="primary" @click="reload">查询</el-button>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="courseName" label="课程" min-width="180" />
      <el-table-column prop="teacherName" label="讲师" width="110" />
      <el-table-column label="价格" width="90">
        <template #default="{ row }">
          <span v-if="row.isFree === 1" class="price-free">免费</span>
          <span v-else>¥{{ money(row.price) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审核" width="100">
        <template #default="{ row }"><el-tag :type="auditType(row.auditStatus)">{{ auditText(row.auditStatus)
        }}</el-tag></template>
      </el-table-column>
      <el-table-column label="上架" width="80">
        <template #default="{ row }"><el-tag :type="shelfType(row.status)">{{ shelfText(row.status)
        }}</el-tag></template>
      </el-table-column>
      <el-table-column label="推荐" width="70">
        <template #default="{ row }">{{ row.isRecommend === 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="操作" min-width="300">
        <template #default="{ row }">
          <el-button v-if="row.auditStatus !== 2" size="small" type="primary" @click="audit(row, 2)">通过</el-button>
          <el-button v-if="row.auditStatus !== 3" size="small" type="danger" @click="audit(row, 3)">驳回</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'primary'" @click="shelf(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button size="small" @click="recommend(row)">{{ row.isRecommend === 1 ? '取消推荐' : '设推荐' }}</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
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
import { money, auditText, auditType, shelfText, shelfType } from '@/utils/format'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, keyword: '', auditStatus: null })
async function load() {
  loading.value = true
  try { const d = await adminApi.coursePage({ ...query }); list.value = d.records; total.value = d.total } finally { loading.value = false }
}
function reload() { query.page = 1; load() }
function onPage(p) { query.page = p; load() }
async function audit(row, status) {
  let reason = ''
  if (status === 3) {
    const r = await ElMessageBox.prompt('请输入驳回原因', '驳回课程', { confirmButtonText: '确定', cancelButtonText: '取消' }).catch(() => null)
    if (!r) return
    reason = r.value
  }
  await adminApi.audit({ courseId: row.id, auditStatus: status, rejectReason: reason })
  ElMessage.success(status === 2 ? '已审核通过' : '已驳回'); load()
}
async function shelf(row) { await adminApi.shelf(row.id, row.status === 1 ? 0 : 1); ElMessage.success('状态已更新'); load() }
async function recommend(row) { await adminApi.recommend(row.id, row.isRecommend === 1 ? 0 : 1); ElMessage.success('操作成功'); load() }
async function remove(row) {
  await ElMessageBox.confirm(`确定删除课程《${row.courseName}》？`, '危险操作', { type: 'warning' }).catch(() => Promise.reject()).then(async () => {
    await adminApi.deleteCourse(row.id); ElMessage.success('已删除'); load()
  }).catch(() => { })
}
onMounted(load)
</script>
