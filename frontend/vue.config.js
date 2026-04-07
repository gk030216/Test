module.exports = {
  devServer: {
    port: 8081,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 添加图片代理 - 代理到后端静态资源
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 保留原有 upload 代理（兼容旧数据）
      '/upload': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  outputDir: 'dist',
  assetsDir: 'static'
};