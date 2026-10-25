<template>
  <div class="page-card">
    <div class="page-head">
      <h1 class="page-title">订单管理</h1>
      <span class="page-sub">学员课程购买与支付记录</span>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="190" />
      <el-table-column prop="courseName" label="课程" min-width="160" />
      <el-table-column prop="userId" label="下单用户ID" width="110" />
      <el-table-column label="实付金额" width="100">
        <template #default="{ row }"><span class="price">¥{{ money(row.payAmount) }}</span></template>
      </el-table-column>
      <el-table-column label="支付方式" width="90">
        <template #default="{ row }">{{ { 1: '微信', 2: '支付宝' }[row.payType] || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }"><el-tag :type="orderType(row.status)">{{ orderText(row.status)
        }}</el-tag></template>
      </el-table-column>
      <el-table-column label="支付时间" width="170">
        <template #default="{ row }">{{ minuteText(row.payTime) || '-' }}</template>
      </el-table-column>
      <el-table-column label="创建时间" width="170">
        <template #default="{ row }">{{ minuteText(row.createTime) }}</template>
      </el-table-column>
    </el-table>
    <el-pagination class="pager" background layout="prev,pager,next,total" :total="total" :page-size="query.size"
      :current-page="query.page" @current-change="onPage" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '@/api'
import { money, minuteText, orderText, orderType } from '@/utils/format'
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10 })
async function load() {
  loading.value = true
  try { const d = await adminApi.orderPage({ ...query }); list.value = d.records; total.value = d.total } finally { loading.value = false }
}
function onPage(p) { query.page = p; load() }
onMounted(load)
</script>
