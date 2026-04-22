<template>
  <header class="fixed top-0 left-0 right-0 z-50 transition-all duration-300"
    :class="scrolled ? 'bg-[#0a0a0a]/95 backdrop-blur-sm border-b border-[#2a2a2a]' : 'bg-transparent'">
    <div class="max-w-7xl mx-auto px-4 md:px-6 h-16 flex items-center justify-between gap-4">

      <!-- Logo + botón atrás -->
      <div class="flex items-center gap-3 shrink-0">
        <button v-if="showBack" @click="router.back()"
          class="text-[#a0a0a0] hover:text-white transition-colors text-sm flex items-center gap-1">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
          </svg>
          <span class="hidden sm:inline">Atrás</span>
        </button>
        <RouterLink to="/" class="flex items-center gap-1">
          <span class="text-[#E31837] text-xl font-black">BIKE</span>
          <span class="text-white text-xl font-black">SHOP</span>
        </RouterLink>
      </div>

      <!-- Nav links desktop -->
      <nav class="hidden md:flex items-center gap-8">
        <RouterLink to="/catalogo" class="text-sm text-[#a0a0a0] hover:text-white transition-colors tracking-wide uppercase">
          Catálogo
        </RouterLink>
        <button @click="irA('destacados')" class="text-sm text-[#a0a0a0] hover:text-white transition-colors tracking-wide uppercase">
          Destacados
        </button>
        <button @click="irA('novedades')" class="text-sm text-[#a0a0a0] hover:text-white transition-colors tracking-wide uppercase">
          Novedades
        </button>
        <RouterLink to="/mantenimiento" class="text-sm text-[#a0a0a0] hover:text-white transition-colors tracking-wide uppercase">
          Mantenimiento
        </RouterLink>
        <RouterLink to="/calculadora" class="text-sm text-[#a0a0a0] hover:text-[#E31837] transition-colors tracking-wide uppercase flex items-center gap-1">
          📐 Talla
        </RouterLink>
      </nav>

      <!-- Acciones -->
      <div class="flex items-center gap-2 md:gap-4">
        <!-- Carrito -->
        <button @click="cart.isOpen = true"
          class="relative p-2 text-[#a0a0a0] hover:text-white transition-colors" aria-label="Carrito">
          <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
          <span v-if="cart.totalItems > 0"
            class="absolute -top-1 -right-1 bg-[#E31837] text-white text-xs w-4 h-4 rounded-full flex items-center justify-center font-bold">
            {{ cart.totalItems > 9 ? '9+' : cart.totalItems }}
          </span>
        </button>

        <!-- Auth desktop -->
        <template v-if="auth.isLoggedIn">
          <RouterLink v-if="auth.isStaff" to="/admin"
            class="hidden md:block text-sm text-[#a0a0a0] hover:text-white transition-colors">Panel</RouterLink>
          <RouterLink v-else-if="auth.isCliente" to="/mi-cuenta"
            class="hidden md:block text-sm text-[#a0a0a0] hover:text-white transition-colors">Mi cuenta</RouterLink>
        </template>
        <RouterLink v-else to="/login"
          class="hidden md:block text-sm bg-[#E31837] hover:bg-[#b5112a] text-white px-4 py-2 rounded-lg transition-colors font-medium">
          Ingresar
        </RouterLink>

        <!-- Hamburger móvil -->
        <button @click="menuOpen = !menuOpen" class="md:hidden p-2 text-[#a0a0a0] hover:text-white transition-colors">
          <svg v-if="!menuOpen" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
          </svg>
          <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- Menú móvil -->
    <Transition name="slide-down">
      <div v-if="menuOpen" class="md:hidden bg-[#141414] border-t border-[#2a2a2a] px-4 py-4 space-y-3">
        <RouterLink to="/catalogo" @click="menuOpen = false"
          class="block text-sm text-[#a0a0a0] hover:text-white py-2 transition-colors uppercase tracking-wide">
          Catálogo
        </RouterLink>
        <button @click="irA('destacados')"
          class="block w-full text-left text-sm text-[#a0a0a0] hover:text-white py-2 transition-colors uppercase tracking-wide">
          Destacados
        </button>
        <button @click="irA('novedades')"
          class="block w-full text-left text-sm text-[#a0a0a0] hover:text-white py-2 transition-colors uppercase tracking-wide">
          Novedades
        </button>
        <RouterLink to="/mantenimiento" @click="menuOpen = false"
          class="block text-sm text-[#a0a0a0] hover:text-white py-2 transition-colors uppercase tracking-wide">
          Mantenimiento
        </RouterLink>
        <div class="border-t border-[#2a2a2a] pt-3">
          <template v-if="auth.isLoggedIn">
            <RouterLink v-if="auth.isStaff" to="/admin" @click="menuOpen = false"
              class="block text-sm text-[#a0a0a0] hover:text-white py-2">Panel admin</RouterLink>
            <RouterLink v-else to="/mi-cuenta" @click="menuOpen = false"
              class="block text-sm text-[#a0a0a0] hover:text-white py-2">Mi cuenta</RouterLink>
          </template>
          <RouterLink v-else to="/login" @click="menuOpen = false"
            class="block w-full text-center bg-[#E31837] text-white py-2.5 rounded-lg text-sm font-medium">
            Ingresar
          </RouterLink>
        </div>
      </div>
    </Transition>
  </header>
  <div class="h-16" />
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'

const auth = useAuthStore()
const cart = useCartStore()
const route = useRoute()
const router = useRouter()
const scrolled = ref(false)
const menuOpen = ref(false)

const showBack = computed(() => route.name !== 'home')

function onScroll() { scrolled.value = window.scrollY > 20 }
onMounted(() => window.addEventListener('scroll', onScroll))
onUnmounted(() => window.removeEventListener('scroll', onScroll))

async function irA(seccionId) {
  menuOpen.value = false
  if (route.name !== 'home') {
    // Si no estamos en home, navegar primero y luego hacer scroll
    await router.push('/')
    // Esperar a que Vue renderice la vista
    setTimeout(() => scrollASeccion(seccionId), 150)
  } else {
    scrollASeccion(seccionId)
  }
}

function scrollASeccion(id) {
  const el = document.getElementById(id)
  if (el) {
    const offset = 72 // altura del navbar fijo
    const top = el.getBoundingClientRect().top + window.scrollY - offset
    window.scrollTo({ top, behavior: 'smooth' })
  }
}
</script>

<style scoped>
.slide-down-enter-active, .slide-down-leave-active { transition: all 0.2s ease; }
.slide-down-enter-from, .slide-down-leave-to { opacity: 0; transform: translateY(-10px); }
</style>
