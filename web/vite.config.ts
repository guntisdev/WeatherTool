import { defineConfig } from 'vite'
import VitePluginHtmlEnv from 'vite-plugin-html-env'
import solidPlugin from 'vite-plugin-solid'

export default defineConfig({
  plugins: [solidPlugin(), VitePluginHtmlEnv()],
  server: {
    port: 3000,
  },
  build: {
    target: 'esnext',
  },
});
