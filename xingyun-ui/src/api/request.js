/**
 * axios 统一封装：
 * 1) 自动携带登录 token；2) 统一解包后端 Result{code,msg,data}；
 * 3) 401 自动清理登录态并跳登录；4) 传输层异常给出可定位的提示。
 */
import axios from "axios";
import { ElMessage } from "element-plus";
import router from "@/router";
import { useUserStore } from "@/store/user";
import { safeJsonParse } from "@/utils/safeParse";

const service = axios.create({
  // 开发期经 Vite 代理到 8080；生产期由 Nginx 同域反代，也可用 VITE_API_BASE 指向独立网关
  baseURL: import.meta.env.VITE_API_BASE || "/api",
  timeout: 15000,
});

service.interceptors.request.use((config) => {
  const userInfo = safeJsonParse("xy_user");
  if (userInfo?.token) {
    config.headers["Authorization"] = `Bearer ${userInfo.token}`;
  }
  return config;
});

// 标记是否正在跳转登录，避免同时弹多个提示
let redirecting = false;

service.interceptors.response.use(
  (response) => {
    // 文件流等非标准 Result 直接返回
    if (response.config.responseType === "blob") return response.data;
    const res = response.data;
    if (res.code === 200) {
      return res.data; // 成功时直接把 data 交给业务方
    }
    if (res.code === 401) {
      handleLogout();
      return Promise.reject(new Error(res.msg || "登录已过期"));
    }
    if (res.code === 403) {
      ElMessage.error(res.msg || "没有访问权限");
      return Promise.reject(new Error(res.msg || "forbidden"));
    }
    ElMessage.error(res.msg || "操作失败");
    return Promise.reject(new Error(res.msg || "error"));
  },
  (error) => {
    const status = error.response?.status;
    if (status === 401) {
      handleLogout();
    } else if (
      error.code === "ECONNABORTED" ||
      error.message?.includes("timeout")
    ) {
      ElMessage.error("请求超时，请检查网络后重试");
    } else if (!error.response) {
      ElMessage.error("无法连接服务器，请确认服务已启动");
    } else if (status >= 500) {
      ElMessage.error("服务暂时不可用，请稍后再试");
    } else {
      ElMessage.error(error.response.data?.msg || "请求失败，请稍后再试");
    }
    return Promise.reject(error);
  },
);

function handleLogout() {
  if (redirecting) return;
  redirecting = true;
  ElMessage.warning("登录已过期，请重新登录");
  localStorage.removeItem("xy_user");
  try {
    // 同步清空内存中的登录态，避免未刷新页面时仍带旧 token 发起请求
    useUserStore().clear();
  } catch {
    /* Pinia 尚未初始化时忽略 */
  }
  router.replace("/login").finally(() => {
    redirecting = false;
  });
}

export default service;
