module.exports = {
  devServer: {
    port: 8081,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 添加图片代理
      '/upload': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  outputDir: 'dist',
  assetsDir: 'static'
};