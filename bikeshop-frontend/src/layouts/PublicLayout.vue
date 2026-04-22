<template>
  <div class="min-h-screen bg-[#0a0a0a] text-white flex flex-col">
    <NavBar />
    <CartSidebar />
    <main class="flex-1">
      <RouterView />
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import NavBar from '@/components/common/NavBar.vue'
import AppFooter from '@/components/common/AppFooter.vue'
import CartSidebar from '@/components/cart/CartSidebar.vue'
import { RouterView } from 'vue-router'
</script>
