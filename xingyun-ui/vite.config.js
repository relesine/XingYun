import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// Vite 配置：开发服务器端口 5173，把 /api 请求代理到后端 8080（从而规避浏览器跨域）
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // 用 @ 指向 src 目录，import 路径更短
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    host: '0.0.0.0', // 允许局域网/手机访问，方便真机调试
    proxy: {
      // 后端所有接口以 /api 开头（后端 server.servlet.context-path=/api）
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 上传的静态资源（封面、头像、课件）也走后端。
      // 后端 context-path=/api，故这里要把 /upload 重写成 /api/upload 再转发，否则 404（裂图根因）
      '/upload': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => '/api' + path
      }
    }
  },
  build: {
    outDir: 'dist',
    target: 'es2015', // 兼容企业内网常见的老版本 Chromium 内核浏览器
    cssCodeSplit: true,
    sourcemap: false,
    reportCompressedSize: false,
    chunkSizeWarningLimit: 1200,
    rollupOptions: {
      output: {
        // 按依赖维度拆包：业务代码迭代时可复用框架与组件库的浏览器缓存
        manualChunks: {
          vue: ['vue', 'vue-router', 'pinia'],
          'element-plus': ['element-plus'],
          vendor: ['axios']
        }
      }
    }
  }
})
