<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

const props = defineProps<{
  content: string
}>()

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight(str: string, lang: string) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return (
          '<pre class="hljs"><code>' +
          hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
          '</code></pre>'
        )
      } catch {
        // fallback
      }
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  },
})

const renderedHtml = computed(() => md.render(props.content))
</script>

<template>
  <div class="md-renderer" v-html="renderedHtml" />
</template>

<style>
@import 'highlight.js/styles/github.css';
</style>

<style scoped>
.md-renderer {
  line-height: 1.65;
  word-break: break-word;
}

.md-renderer :deep(p) {
  margin: 0 0 8px;
}

.md-renderer :deep(p:last-child) {
  margin-bottom: 0;
}

.md-renderer :deep(pre) {
  margin: 8px 0;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
}

.md-renderer :deep(code) {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
}

.md-renderer :deep(p > code) {
  padding: 2px 6px;
  border-radius: 4px;
  background: #f0f0f0;
  color: #d63384;
  font-size: 0.9em;
}

.md-renderer :deep(ul),
.md-renderer :deep(ol) {
  padding-left: 20px;
  margin: 8px 0;
}

.md-renderer :deep(blockquote) {
  margin: 8px 0;
  padding: 4px 12px;
  border-left: 4px solid #1677ff;
  background: #f8fafc;
  color: #475569;
}

.md-renderer :deep(h1),
.md-renderer :deep(h2),
.md-renderer :deep(h3),
.md-renderer :deep(h4) {
  margin: 12px 0 8px;
  color: #111827;
}

.md-renderer :deep(a) {
  color: #1677ff;
  text-decoration: none;
}

.md-renderer :deep(a:hover) {
  text-decoration: underline;
}

.md-renderer :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 8px 0;
}

.md-renderer :deep(th),
.md-renderer :deep(td) {
  padding: 6px 10px;
  border: 1px solid #e5e7eb;
  text-align: left;
}

.md-renderer :deep(th) {
  background: #f8fafc;
  font-weight: 600;
}

.md-renderer :deep(img) {
  max-width: 100%;
  border-radius: 6px;
}
</style>
