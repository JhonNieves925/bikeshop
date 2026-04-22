<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-black text-white">Mis Pedidos</h1>
        <p class="text-[#a0a0a0] text-sm mt-1">{{ pedidos.length }} pedido{{ pedidos.length !== 1 ? 's' : '' }}</p>
      </div>
      <RouterLink to="/catalogo"
        class="bg-[#E31837] hover:bg-[#b5112a] text-white px-4 py-2 rounded-xl text-sm font-semibold transition-colors">
        Ver catálogo
      </RouterLink>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="space-y-3">
      <div v-for="i in 3" :key="i" class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-5 animate-pulse">
        <div class="flex items-center justify-between">
          <div class="space-y-2">
            <div class="h-4 w-28 bg-[#2a2a2a] rounded" />
            <div class="h-3 w-44 bg-[#2a2a2a] rounded" />
          </div>
          <div class="h-6 w-20 bg-[#2a2a2a] rounded-full" />
        </div>
      </div>
    </div>

    <!-- Vacío -->
    <div v-else-if="pedidos.length === 0"
      class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-16 text-center">
      <p class="text-5xl mb-4">🛒</p>
      <p class="text-white font-semibold text-lg mb-2">Aún no tienes pedidos</p>
      <p class="text-[#a0a0a0] text-sm mb-6">¡Explora nuestro catálogo y encuentra lo que necesitas!</p>
      <RouterLink to="/catalogo"
        class="inline-block bg-[#E31837] hover:bg-[#b5112a] text-white px-6 py-3 rounded-xl text-sm font-semibold transition-colors">
        Explorar catálogo
      </RouterLink>
    </div>

    <!-- Lista -->
    <div v-else class="space-y-3">
      <div v-for="pedido in pedidos" :key="pedido.id"
        class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl overflow-hidden">
        <button type="button" @click="toggleExpand(pedido.id)"
          class="w-full text-left px-5 py-4 hover:bg-[#2a2a2a]/40 transition-colors focus:outline-none">
          <div class="flex items-start justify-between gap-3">
            <div class="min-w-0 flex-1">
              <div class="flex items-center gap-2 flex-wrap">
                <span class="text-white font-semibold text-sm">#{{ pedido.id }}</span>
                <span class="px-2 py-0.5 rounded-full text-xs font-medium shrink-0"
                  :class="estadoBadgeClass(pedido.estado)">
                  {{ estadoLabel(pedido.estado) }}
                </span>
              </div>
              <p class="text-[#a0a0a0] text-xs mt-1">
                {{ formatDate(pedido.creadoEn) }} · {{ pedido.items?.length || 0 }} producto{{ (pedido.items?.length || 0) !== 1 ? 's' : '' }}
              </p>
            </div>
            <div class="text-right shrink-0">
              <p class="text-[#E31837] font-bold text-sm">{{ formatPrice(pedido.total) }}</p>
              <span class="text-[#a0a0a0] text-xs">{{ expandedId === pedido.id ? '▲ Ocultar' : '▼ Detalle' }}</span>
            </div>
          </div>
        </button>

        <Transition name="expand">
          <div v-if="expandedId === pedido.id" class="border-t border-[#2a2a2a] px-5 py-4">
            <p class="text-xs uppercase tracking-widest text-[#a0a0a0] mb-3">Productos del pedido</p>
            <div class="space-y-3">
              <div v-for="(item, idx) in pedido.items" :key="idx"
                class="flex items-center justify-between gap-4">
                <div class="min-w-0 flex-1">
                  <p class="text-white text-sm font-medium truncate">{{ item.productoNombre }}</p>
                  <p class="text-[#a0a0a0] text-xs">{{ item.cantidad }} × {{ formatPrice(item.precioUnitario) }}</p>
                </div>
                <p class="text-white text-sm font-semibold shrink-0">{{ formatPrice(item.subtotal) }}</p>
              </div>
            </div>
            <!-- Desglose de precios -->
            <div class="mt-4 pt-3 border-t border-[#2a2a2a] space-y-2">
              <!-- Subtotal (solo si hay descuento) -->
              <div v-if="pedido.descuento > 0" class="flex items-center justify-between">
                <span class="text-[#a0a0a0] text-sm">Subtotal</span>
                <span class="text-[#a0a0a0] text-sm">{{ formatPrice(pedido.subtotal) }}</span>
              </div>
              <!-- Descuento con código de cupón -->
              <div v-if="pedido.descuento > 0" class="flex items-center justify-between">
                <span class="flex items-center gap-2 text-green-400 text-sm">
                  <span>🏷️ Cupón</span>
                  <span class="bg-green-500/20 px-2 py-0.5 rounded text-xs font-mono font-bold">
                    {{ pedido.codigoCupon || 'DESCUENTO' }}
                  </span>
                </span>
                <span class="text-green-400 text-sm font-semibold">− {{ formatPrice(pedido.descuento) }}</span>
              </div>
              <!-- Total final -->
              <div class="flex items-center justify-between pt-1 border-t border-[#2a2a2a]">
                <span class="text-white text-sm font-semibold">Total pagado</span>
                <span class="text-[#E31837] font-bold">{{ formatPrice(pedido.total) }}</span>
              </div>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import api from '@/services/api'

const pedidos = ref([])
const loading = ref(true)
const expandedId = ref(null)

function toggleExpand(id) { expandedId.value = expandedId.value === id ? null : id }

const ESTADO_LABELS = { PENDIENTE: 'Pendiente', CONFIRMADO: 'Confirmado', EN_PREPARACION: 'En preparación', DESPACHADO: 'Despachado', ENTREGADO: 'Entregado', CANCELADO: 'Cancelado' }
const ESTADO_CLASSES = { PENDIENTE: 'bg-yellow-500/20 text-yellow-400', CONFIRMADO: 'bg-blue-500/20 text-blue-400', EN_PREPARACION: 'bg-orange-500/20 text-orange-400', DESPACHADO: 'bg-purple-500/20 text-purple-400', ENTREGADO: 'bg-green-500/20 text-green-400', CANCELADO: 'bg-red-500/20 text-red-400' }

function estadoLabel(e) { return ESTADO_LABELS[e] || e }
function estadoBadgeClass(e) { return ESTADO_CLASSES[e] || 'bg-[#2a2a2a] text-[#a0a0a0]' }
function formatPrice(p) { return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 }).format(p) }
function formatDate(d) { if (!d) return '—'; return new Intl.DateTimeFormat('es-CO', { year: 'numeric', month: 'long', day: 'numeric' }).format(new Date(d)) }

async function cargar() {
  loading.value = true
  try {
    const { data } = await api.get('/clientes/pedidos')
    pedidos.value = data.data || data || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

onMounted(cargar)
</script>

<style scoped>
.expand-enter-active, .expand-leave-active { transition: opacity 0.2s ease, max-height 0.25s ease; overflow: hidden; max-height: 600px; }
.expand-enter-from, .expand-leave-to { opacity: 0; max-height: 0; }
</style>
