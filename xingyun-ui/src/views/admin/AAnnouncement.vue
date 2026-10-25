<template>
  <div class="page-card">
    <div class="page-head">
      <h1 class="page-title">公告管理</h1>
      <span class="page-sub">首页公告栏展示内容</span>
    </div>
    <div class="toolbar">
      <span class="flex-grow"></span>
      <el-button type="primary" @click="openAdd">发布公告</el-button>
    </div>
    <el-table :data="list">
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column label="类型" width="110">
        <template #default="{ row }">{{ row.type === 2 ? '课程通知' : '系统公告' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="发布时间" width="170">
        <template #default="{ row }">{{ minuteText(row.publishTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="form.id ? '编辑公告' : '发布公告'" width="560px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type"><el-radio :label="1">系统公告</el-radio><el-radio :label="2">课程通知</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="5" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status"><el-radio :label="1">发布</el-radio><el-radio :label="0">存草稿</el-radio></el-radio-group>
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
import { minuteText } from '@/utils/format'
const list = ref([])
const dialog = ref(false)
const emptyForm = () => ({ id: null, title: '', content: '', type: 1, status: 1 })
const form = reactive(emptyForm())
async function load() { list.value = await adminApi.announceList() }
function openAdd() { Object.assign(form, emptyForm()); dialog.value = true }
function openEdit(row) { Object.assign(form, emptyForm(), row); dialog.value = true }
async function save() {
  if (!form.title || !form.content) return ElMessage.warning('标题和内容必填')
  await adminApi.announceSave({ ...form })
  ElMessage.success('保存成功'); dialog.value = false; load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除公告「${row.title}」？`, '提示', { type: 'warning' }).catch(() => Promise.reject()).then(async () => {
    await adminApi.announceDelete(row.id); ElMessage.success('已删除'); load()
  }).catch(() => {})
}
onMounted(load)
</script>
