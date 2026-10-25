<template>
  <div class="page-card">
    <div class="page-head">
      <h1 class="page-title">我的收藏</h1>
      <span class="page-sub">收藏的课程</span>
    </div>
    <div class="mb12">
      <span class="text-gray">共 {{ list.length }} 门，点击卡片进入课程，右上角可取消收藏</span>
    </div>
    <el-empty v-if="!list.length" description="还没有收藏课程" />
    <el-row v-else :gutter="16">
      <el-col v-for="c in list" :key="c.id" :xs="12" :sm="8" :md="6" class="mb12">
        <CourseCard :course="c" />
        <el-button size="small" class="cancel" @click="cancel(c)">取消收藏</el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import CourseCard from '@/components/CourseCard.vue'
import { favoriteApi } from '@/api'

const list = ref([])
async function load() { list.value = await favoriteApi.my() }
async function cancel(c) {
  await favoriteApi.toggle(c.id)
  ElMessage.success('已取消收藏')
  load()
}
onMounted(load)
</script>

<style scoped>
.cancel {
  width: 100%;
  margin-top: 6px;
}
</style>
