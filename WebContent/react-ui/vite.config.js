import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/data': 'http://localhost:8080',
      '/authentication': 'http://localhost:8080',
    }
  },
  build: {
    outDir: '../buddilive-react',
    emptyOutDir: true,
    rolldownOptions: {
      output: {
        // Let the bundler preserve dependency initialization order. Forced
        // size-based vendor groups created circular initialization imports.
        entryFileNames: 'assets/index.js',
        chunkFileNames: 'assets/[name].js',
        assetFileNames: 'assets/[name][extname]',
      }
    }
  }
})
