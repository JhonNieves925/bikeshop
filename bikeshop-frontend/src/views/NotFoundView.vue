<template>
  <div class="min-h-screen bg-[#0a0a0a] flex items-center justify-center text-center px-4">
    <div>
      <p class="text-[#E31837] text-8xl font-black mb-4">404</p>
      <h1 class="text-2xl font-black text-white mb-4">Página no encontrada</h1>
      <p class="text-[#a0a0a0] mb-8">La página que buscas no existe.</p>
      <RouterLink to="/" class="bg-[#E31837] hover:bg-[#b5112a] text-white px-8 py-3 rounded-xl font-bold transition-colors">
        Volver al inicio
      </RouterLink>
    </div>
  </div>
</template>

<script setup>
import { RouterLink } from 'vue-router'
</script>
