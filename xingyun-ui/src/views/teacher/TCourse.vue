<template>
  <div class="page-card">
    <div class="page-head">
      <h1 class="page-title">课程管理</h1>
      <span class="page-sub">仅可管理本人课程，提交后需平台审核</span>
    </div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="课程名搜索" style="width:220px" clearable @keyup.enter="reload" />
      <el-button type="primary" @click="reload">查询</el-button>
      <span class="flex-grow"></span>
      <el-button type="primary" @click="openAdd"><el-icon>
          <Plus />
        </el-icon> 新建课程</el-button>
    </div>

    <el-table :data="list" v-loading="loading">
      <el-table-column prop="courseName" label="课程名称" min-width="180" />
      <el-table-column label="价格" width="90">
        <template #default="{ row }">
          <span v-if="row.isFree === 1" class="price-free">免费</span>
          <span v-else class="price">¥{{ money(row.price) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="studentCount" label="学员" width="70" />
      <el-table-column label="审核" width="90">
        <template #default="{ row }"><el-tag :type="auditType(row.auditStatus)">{{ auditText(row.auditStatus)
        }}</el-tag></template>
      </el-table-column>
      <el-table-column label="上架" width="90">
        <template #default="{ row }"><el-tag :type="shelfType(row.status)">{{ shelfText(row.status)
        }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="400">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/teacher/lesson/${row.id}`)">课时</el-button>
          <el-button size="small" type="info"
            @click="$router.push(`/teacher/student?courseId=${row.id}`)">学员</el-button>
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'primary'" @click="toggleShelf(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="pager" background layout="prev,pager,next,total" :total="total" :page-size="query.size"
      :current-page="query.page" @current-change="onPage" />

    <el-dialog v-model="dialog" :title="form.id ? '编辑课程' : '新建课程'" width="640px">
      <el-form :model="form" label-width="92px">
        <el-form-item label="课程名称" required><el-input v-model="form.courseName" /></el-form-item>
        <el-form-item label="课程分类">
          <el-select v-model="form.categoryId" placeholder="一级分类" @change="form.category2Id = null" style="width:48%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-select v-model="form.category2Id" placeholder="二级分类" style="width:48%;margin-left:4%">
            <el-option v-for="c in subCategories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否免费">
          <el-radio-group v-model="form.isFree">
            <el-radio :label="1">免费</el-radio>
            <el-radio :label="0">付费</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="价格" v-if="form.isFree === 0">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="10" />
          <span class="text-gray" style="margin-left:8px">划线价</span>
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" :step="10" style="margin-left:8px" />
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="form.difficulty" style="width:160px">
            <el-option :value="1" label="入门" /><el-option :value="2" label="进阶" /><el-option :value="3" label="高级" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签"><el-input v-model="form.tags" placeholder="多个用逗号分隔，如：Java,后端" /></el-form-item>
        <el-form-item label="课程封面">
          <el-upload :action="uploadUrl" :headers="headers" name="file" :data="{ type: 'cover' }"
            :show-file-list="false" :on-success="onCover">
            <el-button>上传封面</el-button>
          </el-upload>
          <el-image v-if="form.cover" :src="form.cover" style="width:120px;margin-left:10px" />
        </el-form-item>
        <el-form-item label="简介"><el-input v-model="form.intro" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="详细介绍"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存（提交后待平台审核）</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { teacherApi, categoryApi, uploadUrl } from '@/api'
import { money, auditText, auditType, shelfText, shelfType } from '@/utils/format'
import { useUserStore } from '@/store/user'

const store = useUserStore()
const headers = { Authorization: `Bearer ${store.token}` }
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, keyword: '' })
const categories = ref([])
const dialog = ref(false)
const emptyForm = () => ({ id: null, courseName: '', categoryId: null, category2Id: null, isFree: 0, price: 99, originalPrice: 199, difficulty: 1, tags: '', cover: '', intro: '', description: '' })
const form = reactive(emptyForm())
const subCategories = computed(() => categories.value.find((c) => c.id === form.categoryId)?.children || [])

async function load() {
  loading.value = true
  try {
    const d = await teacherApi.coursePage({ ...query })
    list.value = d.records; total.value = d.total
  } finally { loading.value = false }
}
function reload() { query.page = 1; load() }
function onPage(p) { query.page = p; load() }
function openAdd() { Object.assign(form, emptyForm()); dialog.value = true }
function openEdit(row) { Object.assign(form, { ...emptyForm(), ...row }); dialog.value = true }
function onCover(res) { if (res.code === 200) { form.cover = res.data.url; ElMessage.success('封面上传成功') } }
async function save() {
  if (!form.courseName) return ElMessage.warning('请填写课程名称')
  await teacherApi.saveCourse({ ...form })
  ElMessage.success('保存成功')
  dialog.value = false; load()
}
async function toggleShelf(row) {
  await teacherApi.shelf(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态已更新'); load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除课程《${row.courseName}》吗？`, '危险操作', { type: 'warning' }).catch(() => Promise.reject()).then(async () => {
    await teacherApi.deleteCourse(row.id); ElMessage.success('已删除'); load()
  }).catch(() => { })
}
onMounted(() => {
  // 分类加载失败不应阻塞课程列表：两者互不依赖
  categoryApi.tree()
    .then((d) => { categories.value = d || [] })
    .catch(() => {})
  load()
})
</script>
