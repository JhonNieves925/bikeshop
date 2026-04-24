<template>
  <RouterView />
  <PwaInstall />
</template>

<script setup>
import { RouterView } from 'vue-router'
import PwaInstall from '@/components/PwaInstall.vue'
</script>
