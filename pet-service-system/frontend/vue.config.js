module.exports = {
  devServer: {
    port: 8081,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',  // 直接指向根路径
        changeOrigin: true
      }
    }
  },
  outputDir: 'dist',
  assetsDir: 'static'
};