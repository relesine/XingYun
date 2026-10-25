<template>
  <div class="page-card" v-if="detail">
    <div class="page-head">
      <h1 class="page-title">课时管理</h1>
      <span class="page-sub">维护课程章节、课时与视频素材</span>
    </div>
    <div class="toolbar">
      <el-button @click="$router.push('/teacher/course')">← 返回课程</el-button>
      <span class="text-secondary">《{{ detail.courseName }}》</span>
      <span class="flex-grow"></span>
      <el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon> 新增课时</el-button>
    </div>

    <el-collapse v-model="open">
      <el-collapse-item v-for="(ch, ci) in detail.chapters" :key="ci" :name="ci">
        <template #title>
          <span class="chapter">{{ ch.chapter }}</span>
          <el-tag size="small">{{ ch.lessons.length }}节</el-tag>
        </template>
        <el-table :data="ch.lessons" border size="small">
          <el-table-column prop="sort" label="序号" width="70" />
          <el-table-column prop="lessonName" label="课时名称" min-width="200" />
          <el-table-column label="时长" width="90">
            <template #default="{ row }">{{ duration(row.duration) }}</template>
          </el-table-column>
          <el-table-column label="试看" width="80">
            <template #default="{ row }">
              <el-tag v-if="row.isFreeTrial === 1" size="small" type="success">是</el-tag>
              <span v-else class="text-gray">否</span>
            </template>
          </el-table-column>
          <el-table-column label="视频" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="row.videoId ? 'primary' : 'info'">{{ row.videoId ? '已配' : '无' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button size="small" @click="openEdit(row, ch.chapter)">编辑</el-button>
              <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>
    </el-collapse>
    <el-empty v-if="!detail.chapters.length" description="还没有课时，点击右上角新增" />

    <el-dialog v-model="dialog" :title="form.id ? '编辑课时' : '新增课时'" width="560px">
      <el-form :model="form" label-width="92px">
        <el-form-item label="所属章节" required>
          <el-select v-model="form.chapter" placeholder="选择已有章节或输入新章节" filterable allow-create style="width:100%">
            <el-option v-for="c in chapterNames" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="课时名称" required><el-input v-model="form.lessonName" /></el-form-item>
        <el-form-item label="排序序号"><el-input-number v-model="form.sort" :min="1" /></el-form-item>
        <el-form-item label="时长(秒)"><el-input-number v-model="form.duration" :min="0" :step="60" /></el-form-item>
        <el-form-item label="免费试看">
          <el-switch v-model="form.isFreeTrial" :active-value="1" :inactive-value="0" />
          <span class="text-gray" style="margin-left:8px">付费课前3节建议开启</span>
        </el-form-item>
        <el-form-item label="关联视频">
          <el-upload :action="uploadUrl" :headers="headers" name="file" :data="{ type: 'video' }"
            :show-file-list="false" :on-success="onVideo" style="display:inline-block">
            <el-button>上传视频</el-button>
          </el-upload>
          <el-button style="margin-left:8px" @click="openLibrary">从视频库选择</el-button>
          <div class="text-gray mt12" v-if="form.videoId">已选视频ID：{{ form.videoId }} {{ videoName }}</div>
        </el-form-item>
        <el-form-item label="上架状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存课时</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="libDialog" title="视频资源库" width="560px">
      <el-table :data="library" border highlight-current-row @current-change="pickVideo">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="videoName" label="视频名称" />
        <el-table-column label="时长" width="90">
          <template #default="{ row }">{{ duration(row.duration) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { courseApi, teacherApi, uploadUrl } from '@/api'
import { duration } from '@/utils/format'
import { useUserStore } from '@/store/user'

const route = useRoute()
const courseId = Number(route.params.courseId)
const store = useUserStore()
const headers = { Authorization: `Bearer ${store.token}` }
const detail = ref(null)
const open = ref([0])
const dialog = ref(false)
const libDialog = ref(false)
const library = ref([])
const videoName = ref('')
const emptyForm = () => ({ id: null, courseId, chapter: '', lessonName: '', videoId: null, duration: 600, sort: 1, isFreeTrial: 0, status: 1 })
const form = reactive(emptyForm())
const chapterNames = computed(() => detail.value.chapters.map((c) => c.chapter))

async function load() { detail.value = await courseApi.detail(courseId) }
function openAdd() { Object.assign(form, emptyForm()); if (chapterNames.value[0]) form.chapter = chapterNames.value[0]; dialog.value = true }
function openEdit(row, chapter) { Object.assign(form, emptyForm(), { ...row, chapter }); videoName.value = ''; dialog.value = true }
function onVideo(res) {
  if (res.code === 200) { form.videoId = res.data.videoId; videoName.value = res.data.filePath; ElMessage.success('视频上传完成，已自动关联') }
}
async function openLibrary() { library.value = await teacherApi.videoLibrary(); libDialog.value = true }
function pickVideo(row) {
  if (!row) return
  form.videoId = row.id; videoName.value = row.videoName; libDialog.value = false
}
async function save() {
  if (!form.chapter || !form.lessonName) return ElMessage.warning('请填写章节与课时名称')
  await teacherApi.saveLesson({ ...form })
  ElMessage.success('课时已保存'); dialog.value = false; load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除课时《${row.lessonName}》吗？`, '提示', { type: 'warning' }).catch(() => Promise.reject()).then(async () => {
    await teacherApi.deleteLesson(row.id); ElMessage.success('已删除'); load()
  }).catch(() => {})
}
onMounted(load)
</script>

<style scoped>
.chapter { font-weight: 600; margin-right: 12px; }
</style>
