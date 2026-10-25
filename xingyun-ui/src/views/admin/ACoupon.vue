<template>
  <div class="page-card">
    <div class="page-head">
      <h1 class="page-title">优惠券管理</h1>
      <span class="page-sub">优惠券的发放与使用情况</span>
    </div>
    <div class="toolbar">
      <span class="flex-grow"></span>
      <el-button type="primary" @click="openAdd">新增优惠券</el-button>
    </div>
    <el-table :data="list">
      <el-table-column prop="couponName" label="名称" min-width="140" />
      <el-table-column label="类型" width="90">
        <template #default="{ row }">{{ ['','满减','立减','折扣'][row.couponType] }}</template>
      </el-table-column>
      <el-table-column label="门槛" width="90">
        <template #default="{ row }">¥{{ money(row.threshold) }}</template>
      </el-table-column>
      <el-table-column label="优惠值" width="100">
        <template #default="{ row }">{{ row.couponType === 3 ? Number(row.discountValue) + '折' : '¥' + money(row.discountValue) }}</template>
      </el-table-column>
      <el-table-column label="库存(已领)" width="110">
        <template #default="{ row }">{{ row.totalCount === 0 ? '不限' : row.totalCount }}（{{ row.receiveCount }}）</template>
      </el-table-column>
      <el-table-column prop="validDays" label="有效天数" width="90" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="form.id ? '编辑优惠券' : '新增优惠券'" width="480px">
      <el-form :model="form" label-width="96px">
        <el-form-item label="名称"><el-input v-model="form.couponName" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.couponType">
            <el-radio :label="1">满减</el-radio><el-radio :label="2">立减</el-radio><el-radio :label="3">折扣</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="使用门槛" v-if="form.couponType === 1"><el-input-number v-model="form.threshold" :min="0" :precision="2" /> 元</el-form-item>
        <el-form-item :label="form.couponType === 3 ? '折扣(如8.5)' : '优惠金额'">
          <el-input-number v-model="form.discountValue" :min="0" :step="0.1" :precision="2" />
        </el-form-item>
        <el-form-item label="总库存"><el-input-number v-model="form.totalCount" :min="0" /> <span class="text-gray">0表示不限量</span></el-form-item>
        <el-form-item label="有效天数"><el-input-number v-model="form.validDays" :min="1" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api'
import { money } from '@/utils/format'
const list = ref([])
const dialog = ref(false)
const emptyForm = () => ({ id: null, couponName: '', couponType: 1, threshold: 0, discountValue: 10, totalCount: 100, validDays: 30, status: 1 })
const form = reactive(emptyForm())
async function load() { list.value = await adminApi.couponList() }
function openAdd() { Object.assign(form, emptyForm()); dialog.value = true }
function openEdit(row) { Object.assign(form, emptyForm(), row); dialog.value = true }
async function save() {
  if (!form.couponName) return ElMessage.warning('请填写名称')
  await adminApi.couponSave({ ...form })
  ElMessage.success('保存成功'); dialog.value = false; load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除优惠券「${row.couponName}」？`, '提示', { type: 'warning' }).catch(() => Promise.reject()).then(async () => {
    await adminApi.couponDelete(row.id); ElMessage.success('已删除'); load()
  }).catch(() => {})
}
onMounted(load)
</script>
