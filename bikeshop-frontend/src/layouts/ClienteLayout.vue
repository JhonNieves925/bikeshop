<template>
  <div class="min-h-screen bg-[#0a0a0a] text-white flex flex-col">
    <NavBar />

    <div class="flex-1 max-w-5xl mx-auto w-full px-4 py-6 md:py-10">

      <!-- ── MÓVIL: tabs superiores ───────────────────────────────── -->
      <div class="md:hidden mb-5">
        <!-- Saludo -->
        <div class="mb-4">
          <p class="text-white font-bold text-lg">Hola, {{ auth.user?.nombre || 'Cliente' }} 👋</p>
          <p class="text-[#a0a0a0] text-xs mt-0.5">Gestiona tus pedidos y servicios</p>
        </div>

        <!-- Tabs de navegación -->
        <div class="flex gap-1.5 bg-[#141414] border border-[#2a2a2a] rounded-xl p-1">
          <RouterLink
            to="/mi-cuenta/pedidos"
            class="flex-1 flex items-center justify-center gap-1.5 py-2.5 rounded-lg text-xs font-medium transition-all"
            :class="route.name === 'mis-pedidos'
              ? 'bg-[#E31837] text-white'
              : 'text-[#a0a0a0] hover:text-white'"
          >
            <span>📦</span> Pedidos
          </RouterLink>
          <RouterLink
            to="/mi-cuenta/mantenimientos"
            class="flex-1 flex items-center justify-center gap-1.5 py-2.5 rounded-lg text-xs font-medium transition-all"
            :class="route.name === 'mis-mantenimientos'
              ? 'bg-[#E31837] text-white'
              : 'text-[#a0a0a0] hover:text-white'"
          >
            <span>🔧</span> Servicios
          </RouterLink>
          <RouterLink
            to="/mi-cuenta/mis-bicicletas"
            class="flex-1 flex items-center justify-center gap-1.5 py-2.5 rounded-lg text-xs font-medium transition-all"
            :class="route.name === 'mis-bicicletas'
              ? 'bg-[#E31837] text-white'
              : 'text-[#a0a0a0] hover:text-white'"
          >
            <span>🚲</span> Mis Bicis
          </RouterLink>
          <button
            @click="handleLogout"
            class="px-3 py-2.5 rounded-lg text-xs text-[#a0a0a0] hover:text-red-400 transition-colors"
            title="Cerrar sesión"
          >
            ⎋
          </button>
        </div>
      </div>

      <!-- ── DESKTOP: sidebar + contenido ────────────────────────── -->
      <div class="hidden md:flex gap-8">
        <!-- Sidebar -->
        <aside class="w-52 shrink-0">
          <div class="bg-[#141414] border border-[#2a2a2a] rounded-xl p-4 sticky top-24">
            <!-- Avatar / nombre -->
            <div class="flex items-center gap-3 mb-5 pb-4 border-b border-[#2a2a2a]">
              <div class="w-9 h-9 rounded-full bg-[#E31837]/20 flex items-center justify-center text-[#E31837] font-black text-sm shrink-0">
                {{ (auth.user?.nombre || 'C')[0].toUpperCase() }}
              </div>
              <div class="min-w-0">
                <p class="text-white text-sm font-semibold truncate">{{ auth.user?.nombre }}</p>
                <p class="text-[#a0a0a0] text-xs">Cliente</p>
              </div>
            </div>

            <p class="text-[#a0a0a0] text-xs uppercase tracking-widest mb-3">Mi cuenta</p>
            <nav class="space-y-1">
              <RouterLink
                to="/mi-cuenta/pedidos"
                class="flex items-center gap-2.5 px-3 py-2.5 rounded-lg text-sm transition-all"
                :class="route.name === 'mis-pedidos'
                  ? 'text-white bg-[#2a2a2a] border-l-2 border-[#E31837] pl-[10px]'
                  : 'text-[#a0a0a0] hover:text-white hover:bg-[#2a2a2a]'"
              >
                <span>📦</span> Mis pedidos
              </RouterLink>
              <RouterLink
                to="/mi-cuenta/mantenimientos"
                class="flex items-center gap-2.5 px-3 py-2.5 rounded-lg text-sm transition-all"
                :class="route.name === 'mis-mantenimientos'
                  ? 'text-white bg-[#2a2a2a] border-l-2 border-[#E31837] pl-[10px]'
                  : 'text-[#a0a0a0] hover:text-white hover:bg-[#2a2a2a]'"
              >
                <span>🔧</span> Mantenimientos
              </RouterLink>
              <RouterLink
                to="/mi-cuenta/mis-bicicletas"
                class="flex items-center gap-2.5 px-3 py-2.5 rounded-lg text-sm transition-all"
                :class="route.name === 'mis-bicicletas'
                  ? 'text-white bg-[#2a2a2a] border-l-2 border-[#E31837] pl-[10px]'
                  : 'text-[#a0a0a0] hover:text-white hover:bg-[#2a2a2a]'"
              >
                <span>🚲</span> Mis Bicicletas
              </RouterLink>
            </nav>

            <div class="border-t border-[#2a2a2a] mt-4 pt-4">
              <button
                @click="handleLogout"
                class="text-sm text-[#a0a0a0] hover:text-[#E31837] transition-colors flex items-center gap-1.5"
              >
                <span>→</span> Cerrar sesión
              </button>
            </div>
          </div>
        </aside>

        <!-- Contenido desktop -->
        <main class="flex-1 min-w-0">
          <RouterView />
        </main>
      </div>

      <!-- Contenido móvil -->
      <div class="md:hidden">
        <RouterView />
      </div>

    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import NavBar from '@/components/common/NavBar.vue'
import AppFooter from '@/components/common/AppFooter.vue'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

async function handleLogout() {
  await auth.logout()
  router.push('/')
}
</script>
