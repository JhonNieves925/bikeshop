<template>
  <div class="min-h-screen bg-[#0a0a0a] text-white">
    <div v-if="sidebarOpen" class="fixed inset-0 bg-black/60 z-20 lg:hidden" @click="sidebarOpen = false" />

    <aside
      class="fixed top-0 left-0 h-full w-60 bg-[#141414] border-r border-[#2a2a2a] flex flex-col z-30 transition-transform duration-300"
      :class="sidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'">

      <div class="p-5 border-b border-[#2a2a2a] flex items-center justify-between">
        <span class="flex items-center gap-1">
          <span class="text-[#E31837] text-xl font-black">BIKE</span>
          <span class="text-white text-xl font-black">SHOP</span>
        </span>
        <button @click="sidebarOpen = false" class="lg:hidden text-[#a0a0a0] hover:text-white">x</button>
      </div>
      <p class="px-5 pt-2 text-[#a0a0a0] text-xs">{{ auth.isAdmin ? 'Administrador' : 'Empleado' }}</p>

      <nav class="flex-1 p-3 space-y-0.5 overflow-y-auto mt-2">
        <RouterLink v-for="link in navLinks" :key="link.to" :to="link.to" @click="sidebarOpen = false"
          class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-all"
          :class="route.name === link.name
            ? 'text-white bg-[#2a2a2a] border-l-2 border-[#E31837]'
            : 'text-[#a0a0a0] hover:text-white hover:bg-[#2a2a2a]'">
          <span>{{ link.icon }}</span>
          <span>{{ link.label }}</span>
        </RouterLink>

        <template v-if="auth.isAdmin">
          <div class="border-t border-[#2a2a2a] my-2" />
          <RouterLink v-for="link in adminLinks" :key="link.to" :to="link.to" @click="sidebarOpen = false"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-all"
            :class="route.name === link.name
              ? 'text-white bg-[#2a2a2a] border-l-2 border-[#E31837]'
              : 'text-[#a0a0a0] hover:text-white hover:bg-[#2a2a2a]'">
            <span>{{ link.icon }}</span>
            <span>{{ link.label }}</span>
          </RouterLink>
        </template>
      </nav>

      <div class="p-4 border-t border-[#2a2a2a]">
        <p class="text-xs text-[#a0a0a0] mb-2 truncate">{{ auth.user?.nombre }}</p>
        <button @click="handleLogout" class="text-xs text-[#a0a0a0] hover:text-[#E31837] transition-colors">
          Cerrar sesion ->
        </button>
      </div>
    </aside>

    <div class="lg:ml-60 flex flex-col min-h-screen">
      <header class="bg-[#141414] border-b border-[#2a2a2a] px-4 md:px-8 py-4 flex items-center gap-3 sticky top-0 z-10">
        <button @click="sidebarOpen = true" class="lg:hidden text-[#a0a0a0] hover:text-white p-1">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
          </svg>
        </button>
        <button v-if="canGoBack" @click="router.back()"
          class="text-[#a0a0a0] hover:text-white transition-colors text-sm"><- Atras</button>
        <h1 class="text-base md:text-lg font-semibold flex-1 truncate">{{ pageTitle }}</h1>
      </header>

      <main class="flex-1 p-4 md:p-8">
        <RouterView v-slot="{ Component }">
          <Transition name="page" mode="out-in">
            <component :is="Component" :key="route.path" />
          </Transition>
        </RouterView>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const sidebarOpen = ref(false)

const navLinks = [
  { to: '/admin/dashboard', name: 'dashboard', icon: 'Dashboard', label: 'Dashboard' },
  { to: '/admin/productos', name: 'admin-productos', icon: 'Productos', label: 'Productos' },
  { to: '/admin/categorias', name: 'admin-categorias', icon: 'Categorias', label: 'Categorias' },
  { to: '/admin/pedidos', name: 'admin-pedidos', icon: 'Pedidos', label: 'Pedidos' },
  { to: '/admin/mantenimientos', name: 'admin-mantenimientos', icon: 'Mantenimientos', label: 'Mantenimientos' },
  { to: '/admin/inventario', name: 'admin-inventario', icon: 'Inventario', label: 'Inventario' },
  { to: '/admin/facturacion', name: 'admin-facturacion', icon: 'Facturacion', label: 'Facturacion' },
  { to: '/admin/ventas', name: 'admin-ventas', icon: 'Ventas', label: 'Ventas' },
  { to: '/admin/novedades', name: 'admin-novedades', icon: 'Novedades', label: 'Novedades' },
  { to: '/admin/cupones', name: 'admin-cupones', icon: 'Cupones', label: 'Cupones' },
  { to: '/admin/agenda', name: 'admin-agenda', icon: 'Agenda', label: 'Agenda' }
]

const adminLinks = [
  { to: '/admin/empleados', name: 'admin-empleados', icon: 'Empleados', label: 'Empleados' },
  { to: '/admin/reportes', name: 'admin-reportes', icon: 'Reportes', label: 'Reportes' }
]

const pageTitles = {
  dashboard: 'Dashboard',
  'admin-productos': 'Productos',
  'admin-categorias': 'Categorias',
  'admin-pedidos': 'Pedidos',
  'admin-mantenimientos': 'Mantenimientos',
  'admin-inventario': 'Inventario',
  'admin-facturacion': 'Facturacion',
  'admin-ventas': 'Ventas',
  'admin-novedades': 'Novedades',
  'admin-empleados': 'Empleados',
  'admin-empleado-detalle': 'Detalle empleado',
  'admin-reportes': 'Reportes',
  'admin-cupones': 'Cupones de descuento',
  'admin-agenda': 'Agenda de Mantenimientos'
}

const pageTitle = computed(() => pageTitles[route.name] || 'Panel Admin')
const canGoBack = computed(() => route.name !== 'dashboard')

async function handleLogout() {
  await auth.logout()
  router.push('/staff/login')
}
</script>

<style scoped>
.page-enter-active,
.page-leave-active {
  transition: opacity 0.15s ease;
}
.page-enter-from,
.page-leave-to {
  opacity: 0;
}
</style>
