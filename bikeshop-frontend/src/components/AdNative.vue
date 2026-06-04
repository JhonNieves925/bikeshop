<template>
  <div ref="wrapper" class="ad-native"></div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const wrapper = ref(null)

onMounted(() => {
  const div = document.createElement('div')
  div.id = 'container-8a767160a6c109f817fc727cbef842f6'
  wrapper.value.appendChild(div)

  const s = document.createElement('script')
  s.async = true
  s.setAttribute('data-cfasync', 'false')
  s.src = 'https://pl29641303.effectivecpmnetwork.com/8a767160a6c109f817fc727cbef842f6/invoke.js'
  wrapper.value.appendChild(s)
})
</script>

<style scoped>
.ad-native {
  width: 100%;
  overflow: hidden;
}
</style>
