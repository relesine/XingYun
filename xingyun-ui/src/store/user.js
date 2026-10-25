/**
 * 用户登录状态（Pinia），并用 localStorage 持久化，刷新页面不掉登录。
 * userType: 1 学员 / 2 讲师 / 3 管理员
 */
import { defineStore } from 'pinia'
import { safeJsonParse } from '@/utils/safeParse'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: safeJsonParse('xy_user')
  }),
  getters: {
    isLogin: (s) => !!s.userInfo?.token,
    userType: (s) => s.userInfo?.userInfo?.userType ?? 0,
    username: (s) => s.userInfo?.userInfo?.username ?? '',
    nickname: (s) => s.userInfo?.userInfo?.nickname ?? s.userInfo?.userInfo?.username ?? '',
    token: (s) => s.userInfo?.token ?? ''
  },
  actions: {
    setLogin(loginVo) {
      this.userInfo = loginVo
      localStorage.setItem('xy_user', JSON.stringify(loginVo))
    },
    clear() {
      this.userInfo = null
      localStorage.removeItem('xy_user')
    }
  }
})
