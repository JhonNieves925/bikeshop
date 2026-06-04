<template>
  <div ref="container" class="ad-wrap"></div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  adKey:  { type: String, required: true },
  width:  { type: Number, required: true },
  height: { type: Number, required: true },
})

const container = ref(null)

onMounted(() => {
  window.atOptions = { key: props.adKey, format: 'iframe', height: props.height, width: props.width, params: {} }
  const s = document.createElement('script')
  s.src = `https://www.highperformanceformat.com/${props.adKey}/invoke.js`
  container.value.appendChild(s)
})
</script>

<style scoped>
.ad-wrap {
  overflow: hidden;
  display: flex;
  justify-content: center;
}
</style>
