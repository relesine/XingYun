/**
 * 路由配置 + 全局登录/角色守卫。
 * 三端通过不同前缀和布局区分：
 *   /xxx           学员端（userType=1，讲师/管理员也可访问，方便查看）
 *   /teacher/xxx   讲师端（userType=2,3）
 *   /admin/xxx     管理端（userType=3）
 */
import { createRouter, createWebHistory } from "vue-router";
import { safeJsonParse } from "@/utils/safeParse";

const StudentLayout = () => import("@/layouts/StudentLayout.vue");
const TeacherLayout = () => import("@/layouts/TeacherLayout.vue");
const AdminLayout = () => import("@/layouts/AdminLayout.vue");

const routes = [
  {
    path: "/login",
    component: () => import("@/views/Login.vue"),
    meta: { public: true },
  },

  // 学员端
  {
    path: "/",
    component: StudentLayout,
    redirect: "/home",
    children: [
      {
        path: "home",
        component: () => import("@/views/student/Home.vue"),
        meta: { title: "首页" },
      },
      {
        path: "courses",
        component: () => import("@/views/student/CourseList.vue"),
        meta: { title: "全部课程" },
      },
      {
        path: "course/:id",
        component: () => import("@/views/student/CourseDetail.vue"),
        meta: { title: "课程详情" },
      },
      {
        path: "learn/:id",
        component: () => import("@/views/student/Player.vue"),
        meta: { title: "在线学习", roles: [1, 2, 3] },
      },
      {
        path: "my",
        component: () => import("@/views/student/MyCourse.vue"),
        meta: { title: "我的学习", roles: [1, 2, 3] },
      },
      {
        path: "orders",
        component: () => import("@/views/student/MyOrder.vue"),
        meta: { title: "我的订单", roles: [1, 2, 3] },
      },
      {
        path: "coupons",
        component: () => import("@/views/student/MyCoupon.vue"),
        meta: { title: "我的优惠券", roles: [1, 2, 3] },
      },
      {
        path: "favorites",
        component: () => import("@/views/student/Favorite.vue"),
        meta: { title: "我的收藏", roles: [1, 2, 3] },
      },
      {
        path: "certificates",
        component: () => import("@/views/student/Certificate.vue"),
        meta: { title: "我的证书", roles: [1, 2, 3] },
      },
      {
        path: "wrongbook",
        component: () => import("@/views/student/WrongBook.vue"),
        meta: { title: "错题本", roles: [1, 2, 3] },
      },
      {
        path: "profile",
        component: () => import("@/views/student/Profile.vue"),
        meta: { title: "个人资料", roles: [1, 2, 3] },
      },
    ],
  },

  // 讲师端
  {
    path: "/teacher",
    component: TeacherLayout,
    redirect: "/teacher/dashboard",
    children: [
      {
        path: "dashboard",
        component: () => import("@/views/teacher/TDashboard.vue"),
        meta: { title: "数据看板", roles: [2, 3] },
      },
      {
        path: "course",
        component: () => import("@/views/teacher/TCourse.vue"),
        meta: { title: "课程管理", roles: [2, 3] },
      },
      {
        path: "lesson/:courseId",
        component: () => import("@/views/teacher/TLesson.vue"),
        meta: { title: "课时管理", roles: [2, 3] },
      },
      {
        path: "student",
        component: () => import("@/views/teacher/TStudent.vue"),
        meta: { title: "学员进度", roles: [2, 3] },
      },
      {
        path: "grade",
        component: () => import("@/views/teacher/TGrade.vue"),
        meta: { title: "作业批改", roles: [2, 3] },
      },
      {
        path: "qa",
        component: () => import("@/views/teacher/TQa.vue"),
        meta: { title: "学员答疑", roles: [2, 3] },
      },
      {
        path: "profile",
        component: () => import("@/views/student/Profile.vue"),
        meta: { title: "个人资料", roles: [2, 3] },
      },
    ],
  },

  // 管理端
  {
    path: "/admin",
    component: AdminLayout,
    redirect: "/admin/dashboard",
    children: [
      {
        path: "dashboard",
        component: () => import("@/views/admin/ADashboard.vue"),
        meta: { title: "运营大盘", roles: [3] },
      },
      {
        path: "user",
        component: () => import("@/views/admin/AUser.vue"),
        meta: { title: "用户管理", roles: [3] },
      },
      {
        path: "course",
        component: () => import("@/views/admin/ACourse.vue"),
        meta: { title: "课程审核", roles: [3] },
      },
      {
        path: "order",
        component: () => import("@/views/admin/AOrder.vue"),
        meta: { title: "订单管理", roles: [3] },
      },
      {
        path: "comment",
        component: () => import("@/views/admin/AComment.vue"),
        meta: { title: "评论管理", roles: [3] },
      },
      {
        path: "category",
        component: () => import("@/views/admin/ACategory.vue"),
        meta: { title: "分类管理", roles: [3] },
      },
      {
        path: "coupon",
        component: () => import("@/views/admin/ACoupon.vue"),
        meta: { title: "优惠券管理", roles: [3] },
      },
      {
        path: "announcement",
        component: () => import("@/views/admin/AAnnouncement.vue"),
        meta: { title: "公告管理", roles: [3] },
      },
      {
        path: "profile",
        component: () => import("@/views/student/Profile.vue"),
        meta: { title: "个人资料", roles: [3] },
      },
    ],
  },

  // 404 兜底页（独立全屏，不套布局）
  {
    path: "/:pathMatch(.*)*",
    component: () => import("@/views/NotFound.vue"),
    meta: { public: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  // 切换页面回到顶部，浏览器前进/后退时恢复原位置
  scrollBehavior(to, from, savedPosition) {
    return savedPosition || { top: 0 };
  },
});

export function homeByType(type) {
  if (type === 3) return "/admin/dashboard";
  if (type === 2) return "/teacher/dashboard";
  return "/home";
}

router.beforeEach((to, from, next) => {
  const loginVo = safeJsonParse("xy_user");
  const type = loginVo?.userInfo?.userType;

  if (to.meta.public) {
    // 已登录再访问登录页 → 直接进对应首页
    if (to.path === "/login" && type) return next(homeByType(type));
    return next();
  }
  if (!loginVo?.token) return next("/login");
  if (to.meta.roles && !to.meta.roles.includes(type)) {
    return next(homeByType(type));
  }
  next();
});

// 页面标题：便于多标签浏览与浏览器历史识别
router.afterEach((to) => {
  document.title = to.meta.title
    ? `${to.meta.title} · 星云课堂`
    : "星云课堂 · 在线教育平台";
});

export default router;
