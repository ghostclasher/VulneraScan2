import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],

  // ✅ IMPORTANT FOR VERCEL DEPLOYMENT
  base: "/", 

  build: {
    outDir: "dist"
  }
})

