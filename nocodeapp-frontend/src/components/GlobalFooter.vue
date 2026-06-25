<script setup lang="ts">
withDefaults(
  defineProps<{
    copyright?: string;
    links?: Array<{ label: string; url: string; icon?: string }>;
  }>(),
  {
    copyright: "Copyright &copy; 2026 NoCodeApp. All rights reserved.",
    links: () => [],
  },
);
</script>

<template>
  <footer class="global-footer">
    <div class="footer-inner">
      <div class="footer-links" v-if="links?.length">
        <a
          v-for="link in links"
          :key="link.label"
          :href="link.url"
          target="_blank"
          rel="noopener noreferrer"
          class="footer-link"
        >
          {{ link.label }}
        </a>
      </div>
      <div class="footer-copyright" v-html="copyright"></div>
    </div>
  </footer>
</template>

<style scoped>
.global-footer {
  width: 100%;
  background: #f5f5f5;
  border-top: 1px solid #e8e8e8;
  text-align: center;
  padding: 24px 16px;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.footer-link {
  color: #666;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}

.footer-link:hover {
  color: #1890ff;
}

.footer-copyright {
  color: #999;
  font-size: 13px;
}
</style>
