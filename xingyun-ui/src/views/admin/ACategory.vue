<template>
  <div class="page-card">
    <div class="page-head">
      <h1 class="page-title">分类管理</h1>
      <span class="page-sub">维护课程的一级与二级分类</span>
    </div>
    <div class="toolbar">
      <span class="flex-grow"></span>
      <el-button type="primary" @click="openAdd(0)">新增一级分类</el-button>
    </div>
    <el-table :data="treeData" row-key="id" :tree-props="{ children: 'children' }" default-expand-all>
      <el-table-column prop="name" label="分类名称" min-width="200" />
      <el-table-column prop="level" label="层级" width="80">
        <template #default="{ row }">{{ row.level === 1 ? '一级' : '二级' }}</template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'info'">{{ row.status === 0 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button v-if="row.level === 1" size="small" @click="openAdd(row.id)">加子分类</el-button>
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="form.id ? '编辑分类' : '新增分类'" width="440px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="分类名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="上级分类">
          <el-select v-model="form.parentId" style="width:100%">
            <el-option :value="0" label="作为一级分类" />
            <el-option v-for="r in roots" :key="r.id" :value="r.id" :label="r.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" placeholder="可选，emoji 或图标名" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="0" :inactive-value="1" active-text="启用" inactive-text="停用" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api'

const list = ref([])
const dialog = ref(false)
const emptyForm = () => ({ id: null, name: '', parentId: 0, icon: '', sort: 0, status: 0 })
const form = reactive(emptyForm())
const roots = computed(() => list.value.filter((c) => !c.parentId))
const treeData = computed(() => {
  const rootsArr = list.value.filter((c) => !c.parentId).map((r) => ({ ...r }))
  rootsArr.forEach((r) => { r.children = list.value.filter((c) => c.parentId === r.id) })
  return rootsArr
})
async function load() { list.value = await adminApi.categoryList() }
function openAdd(parentId) { Object.assign(form, emptyForm()); form.parentId = parentId; dialog.value = true }
function openEdit(row) { Object.assign(form, { ...emptyForm(), ...row }); dialog.value = true }
async function save() {
  if (!form.name) return ElMessage.warning('请输入分类名称')
  await adminApi.categorySave({ ...form })
  ElMessage.success('保存成功'); dialog.value = false; load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '提示', { type: 'warning' }).catch(() => Promise.reject()).then(async () => {
    await adminApi.categoryDelete(row.id); ElMessage.success('已删除'); load()
  }).catch(() => {})
}
onMounted(load)
</script>
