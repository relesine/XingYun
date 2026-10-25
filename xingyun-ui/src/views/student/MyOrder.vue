<template>
  <div class="page-card">
    <div class="page-head">
      <h1 class="page-title">我的订单</h1>
      <span class="page-sub">课程购买记录与支付状态</span>
    </div>
    <div class="toolbar">
      <el-radio-group v-model="status" @change="load">
        <el-radio-button :label="null">全部</el-radio-button>
        <el-radio-button :label="0">待支付</el-radio-button>
        <el-radio-button :label="1">已支付</el-radio-button>
        <el-radio-button :label="2">已取消</el-radio-button>
      </el-radio-group>
    </div>
    <el-table :data="list">
      <el-table-column prop="orderNo" label="订单号" width="190" />
      <el-table-column prop="courseName" label="课程" min-width="180">
        <template #default="{ row }">
          <el-link type="primary" @click="$router.push(`/course/${row.courseId}`)">{{ row.courseName ||
            ('课程#' + row.courseId) }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="原价" width="90">
        <template #default="{ row }">¥{{ money(row.originalAmount) }}</template>
      </el-table-column>
      <el-table-column label="优惠" width="80">
        <template #default="{ row }">¥{{ money(row.discountAmount) }}</template>
      </el-table-column>
      <el-table-column label="实付" width="90">
        <template #default="{ row }"><span class="price">¥{{ money(row.payAmount) }}</span></template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }"><el-tag :type="orderType(row.status)">{{ orderText(row.status)
            }}</el-tag></template>
      </el-table-column>
      <el-table-column label="下单时间" width="160">
        <template #default="{ row }">{{ minuteText(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button size="small" type="danger" @click="pay(row)">立即支付</el-button>
            <el-button size="small" @click="cancel(row)">取消</el-button>
          </template>
          <el-button v-else-if="row.status === 1" size="small" type="primary"
            @click="$router.push(`/learn/${row.courseId}`)">去学习</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api'
import { money, minuteText, orderText, orderType } from '@/utils/format'

const list = ref([])
const status = ref(null)
async function load() { list.value = await orderApi.my() }
async function pay(row) {
  await orderApi.pay(row.orderNo)
  ElMessage.success('支付成功')
  load()
}
async function cancel(row) {
  await ElMessageBox.confirm('确定取消该订单吗？', '提示', { type: 'warning' }).catch(() => Promise.reject())
    .then(async () => { await orderApi.cancel(row.id); ElMessage.success('已取消'); load() }).catch(() => { })
}
onMounted(load)
</script>
