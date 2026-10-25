<template>
  <div>
    <div class="page-card mb12">
      <h3 class="mb12">领券中心</h3>
      <el-row :gutter="16">
        <el-col v-for="c in available" :key="c.id" :span="8">
          <div class="coupon">
            <div class="left">
              <template v-if="c.couponType === 3">
                <div class="val">{{ Number(c.discountValue) }}<small>折</small></div>
              </template>
              <template v-else>
                <div class="val">¥<span>{{ money(c.discountValue) }}</span></div>
                <div class="cond">{{ c.couponType === 1 ? `满${money(c.threshold)}可用` : '无门槛' }}</div>
              </template>
            </div>
            <div class="mid">
              <div class="cname">{{ c.couponName }}</div>
              <div class="text-gray">{{ c.validDays }}天有效 · 已领{{ c.receiveCount }}/{{ c.totalCount === 0 ? '∞' :
                c.totalCount }}
              </div>
            </div>
            <el-button type="danger" size="small" @click="receive(c)">领取</el-button>
          </div>
        </el-col>
      </el-row>
      <el-empty v-if="!available.length" description="暂无可领优惠券" />
    </div>

    <div class="page-card">
      <div class="page-head">
        <h1 class="page-title">我的优惠券</h1>
        <span class="page-sub">已领取与可使用的优惠券</span>
      </div>
      <div class="toolbar">
        <el-radio-group v-model="status" @change="loadMine">
          <el-radio-button :label="null">全部</el-radio-button>
          <el-radio-button :label="0">未使用</el-radio-button>
          <el-radio-button :label="1">已使用</el-radio-button>
          <el-radio-button :label="2">已过期</el-radio-button>
        </el-radio-group>
      </div>
      <el-table :data="mine">
        <el-table-column label="优惠券" min-width="200">
          <template #default="{ row }">{{ row.coupon?.couponName }}</template>
        </el-table-column>
        <el-table-column label="优惠" width="140">
          <template #default="{ row }">{{ desc(row.coupon) }}</template>
        </el-table-column>
        <el-table-column label="到期时间" width="180">
          <template #default="{ row }">{{ minuteText(row.userCoupon.expireTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="['warning', 'success', 'info'][row.userCoupon.status]">{{
              ['未使用', '已使用', '已过期'][row.userCoupon.status] }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { couponApi } from '@/api'
import { money, minuteText } from '@/utils/format'

const available = ref([])
const mine = ref([])
const status = ref(0)
function desc(c) {
  if (!c) return '-'
  if (c.couponType === 1) return `满${money(c.threshold)}减${money(c.discountValue)}`
  if (c.couponType === 2) return `立减${money(c.discountValue)}`
  return `${Number(c.discountValue)}折`
}
async function loadAvailable() { available.value = await couponApi.available() }
async function loadMine() { mine.value = await couponApi.my(status.value) }
async function receive(c) {
  await couponApi.receive(c.id)
  ElMessage.success('领取成功')
  loadAvailable(); loadMine()
}
onMounted(() => { loadAvailable(); loadMine() })
</script>

<style scoped>
.coupon {
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px dashed #f56c6c;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 14px;
}

.coupon .left {
  width: 96px;
  text-align: center;
  border-right: 1px dashed #f56c6c;
}

.coupon .val {
  color: #f56c6c;
  font-size: 26px;
  font-weight: bold;
}

.coupon .val small {
  font-size: 13px;
}

.coupon .cond {
  font-size: 12px;
  color: #909399;
}

.coupon .mid {
  flex: 1;
}

.cname {
  font-weight: 600;
  margin-bottom: 4px;
}
</style>
