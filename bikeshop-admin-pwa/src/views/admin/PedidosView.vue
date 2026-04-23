<template>
  <div class="space-y-6">
    <!-- Filtros por estado -->
    <div class="flex flex-wrap gap-2">
      <button v-for="f in filtros" :key="f.value" @click="cambiarFiltro(f.value)"
        class="border px-3 py-1.5 rounded-xl text-xs font-semibold transition-colors"
        :class="filtroActivo === f.value
          ? 'bg-[#E31837] text-white border-transparent'
          : 'bg-[#1a1a1a] text-[#a0a0a0] border-[#2a2a2a] hover:border-white hover:text-white'">
        {{ f.label }}
      </button>
    </div>

    <!-- Contenido -->
    <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl overflow-hidden">
      <div v-if="loading" class="p-8 space-y-3">
        <div v-for="i in 5" :key="i" class="h-14 bg-[#2a2a2a] rounded-lg animate-pulse" />
      </div>
      <div v-else-if="pedidos.length === 0" class="p-12 text-center">
        <p class="text-4xl mb-3">📦</p>
        <p class="text-[#a0a0a0]">No hay pedidos para este filtro.</p>
      </div>

      <template v-else>
        <!-- Desktop tabla -->
        <div class="hidden md:block overflow-x-auto">
          <table class="w-full">
            <thead class="border-b border-[#2a2a2a]">
              <tr class="text-left">
                <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">#</th>
                <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Cliente</th>
                <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Total</th>
                <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Estado</th>
                <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0]">Fecha</th>
                <th class="px-6 py-4 text-xs uppercase tracking-widest text-[#a0a0a0] text-right">Acciones</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-[#2a2a2a]">
              <tr v-for="p in pedidos" :key="p.id" class="hover:bg-[#2a2a2a]/40 transition-colors">
                <td class="px-6 py-4 text-white font-mono text-sm font-semibold">#{{ p.id }}</td>
                <td class="px-6 py-4">
                  <p class="text-white text-sm">{{ p.nombreCliente }}</p>
                  <p class="text-[#a0a0a0] text-xs">{{ p.emailCliente }}</p>
                </td>
                <td class="px-6 py-4 text-[#E31837] font-semibold text-sm">{{ formatPrice(p.total) }}</td>
                <td class="px-6 py-4">
                  <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="estadoBadge(p.estado)">
                    {{ estadoLabel(p.estado) }}
                  </span>
                </td>
                <td class="px-6 py-4 text-[#a0a0a0] text-xs">{{ formatFecha(p.creadoEn) }}</td>
                <td class="px-6 py-4 text-right">
                  <button @click="abrirDetalle(p)" class="text-[#a0a0a0] hover:text-[#E31837] text-sm transition-colors">
                    Ver detalle
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Móvil cards -->
        <div class="md:hidden divide-y divide-[#2a2a2a]">
          <div v-for="p in pedidos" :key="p.id" @click="abrirDetalle(p)"
            class="px-5 py-4 cursor-pointer hover:bg-[#2a2a2a]/40 transition-colors space-y-1">
            <div class="flex items-center justify-between">
              <span class="text-white font-mono font-semibold text-sm">#{{ p.id }}</span>
              <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="estadoBadge(p.estado)">
                {{ estadoLabel(p.estado) }}
              </span>
            </div>
            <p class="text-[#a0a0a0] text-xs">{{ p.nombreCliente }}</p>
            <div class="flex items-center justify-between">
              <span class="text-[#E31837] font-bold text-sm">{{ formatPrice(p.total) }}</span>
              <span class="text-[#a0a0a0] text-xs">{{ formatFecha(p.creadoEn) }}</span>
            </div>
          </div>
        </div>
      </template>

      <!-- Paginación -->
      <div v-if="totalPages > 1" class="flex items-center justify-between px-6 py-4 border-t border-[#2a2a2a]">
        <button @click="cargar(page - 1)" :disabled="page === 0"
          class="text-sm text-[#a0a0a0] hover:text-white disabled:opacity-30 transition-colors">← Anterior</button>
        <span class="text-sm text-[#a0a0a0]">Página {{ page + 1 }} de {{ totalPages }}</span>
        <button @click="cargar(page + 1)" :disabled="page >= totalPages - 1"
          class="text-sm text-[#a0a0a0] hover:text-white disabled:opacity-30 transition-colors">Siguiente →</button>
      </div>
    </div>

    <!-- Modal detalle -->
    <Transition name="fade">
      <div v-if="modal" class="fixed inset-0 bg-black/70 z-50 flex items-start justify-center p-4 overflow-y-auto"
        @click.self="modal = false">
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-2xl p-6 sm:p-8 w-full max-w-lg my-8">
          <div class="flex items-start justify-between mb-6">
            <div>
              <h2 class="text-xl font-black text-white">Pedido #{{ pedidoActivo?.id }}</h2>
              <p class="text-[#a0a0a0] text-xs mt-1">{{ formatFecha(pedidoActivo?.creadoEn) }}</p>
            </div>
            <span class="px-2 py-1 rounded-full text-xs font-medium" :class="estadoBadge(pedidoActivo?.estado)">
              {{ estadoLabel(pedidoActivo?.estado) }}
            </span>
          </div>

          <div class="space-y-2 mb-6">
            <div class="flex justify-between text-sm"><span class="text-[#a0a0a0]">Cliente</span><span class="text-white font-medium">{{ pedidoActivo?.nombreCliente }}</span></div>
            <div class="flex justify-between text-sm"><span class="text-[#a0a0a0]">Email</span><span class="text-white truncate ml-4 text-xs">{{ pedidoActivo?.emailCliente }}</span></div>
            <div class="flex justify-between text-sm"><span class="text-[#a0a0a0]">Celular</span><span class="text-white">{{ pedidoActivo?.celularCliente || '—' }}</span></div>
            <div class="flex justify-between text-sm"><span class="text-[#a0a0a0]">Dirección</span><span class="text-white text-right ml-4 text-xs">{{ pedidoActivo?.direccionEntrega }}</span></div>
            <div class="flex justify-between text-sm"><span class="text-[#a0a0a0]">Ciudad</span><span class="text-white text-right ml-4 text-xs">{{ pedidoActivo?.ciudadEntrega || '—' }}</span></div>
            <div class="flex justify-between text-sm"><span class="text-[#a0a0a0]">Departamento</span><span class="text-white text-right ml-4 text-xs">{{ pedidoActivo?.departamentoEntrega || '—' }}</span></div>
            <div v-if="pedidoActivo?.notasEntrega" class="flex justify-between text-sm"><span class="text-[#a0a0a0]">Notas</span><span class="text-white text-right ml-4 text-xs">{{ pedidoActivo.notasEntrega }}</span></div>
            <div class="flex justify-between text-sm"><span class="text-[#a0a0a0]">Pago</span><span class="text-xs px-2 py-0.5 rounded-full" :class="pedidoActivo?.estadoPago === 'PAGADO' ? 'bg-green-500/20 text-green-400' : 'bg-yellow-500/20 text-yellow-400'">{{ pedidoActivo?.estadoPago || '—' }}</span></div>
          </div>

          <div class="border border-[#2a2a2a] rounded-xl overflow-hidden mb-6">
            <div class="px-4 py-3 border-b border-[#2a2a2a]">
              <p class="text-xs uppercase tracking-widest text-[#a0a0a0]">Productos</p>
            </div>
            <div class="divide-y divide-[#2a2a2a]">
              <div v-for="(item, i) in pedidoActivo?.items" :key="i"
                class="flex justify-between px-4 py-3 text-sm">
                <div>
                  <p class="text-white">{{ item.productoNombre }}</p>
                  <p class="text-[#a0a0a0] text-xs">{{ item.cantidad }} × {{ formatPrice(item.precioUnitario) }}</p>
                </div>
                <span class="text-white font-semibold">{{ formatPrice(item.subtotal) }}</span>
              </div>
            </div>
          </div>

          <!-- Desglose de precios -->
          <div class="mb-6 pb-4 border-b border-[#2a2a2a] space-y-2">
            <!-- Subtotal (solo si hay descuento) -->
            <div v-if="pedidoActivo?.descuento > 0" class="flex justify-between items-center">
              <span class="text-[#a0a0a0] text-sm">Subtotal</span>
              <span class="text-[#a0a0a0] text-sm">{{ formatPrice(pedidoActivo?.subtotal) }}</span>
            </div>
            <!-- Descuento con código de cupón -->
            <div v-if="pedidoActivo?.descuento > 0" class="flex justify-between items-center">
              <span class="flex items-center gap-2 text-green-400 text-sm">
                <span>🏷️ Cupón</span>
                <span class="bg-green-500/20 px-2 py-0.5 rounded text-xs font-mono font-bold">
                  {{ pedidoActivo?.codigoCupon || 'DESCUENTO' }}
                </span>
              </span>
              <span class="text-green-400 text-sm font-semibold">− {{ formatPrice(pedidoActivo?.descuento) }}</span>
            </div>
            <!-- Total final -->
            <div class="flex justify-between items-center pt-1" :class="pedidoActivo?.descuento > 0 ? 'border-t border-[#2a2a2a]' : ''">
              <span class="text-[#a0a0a0] font-semibold uppercase tracking-wider text-sm">Total</span>
              <span class="text-2xl font-black text-[#E31837]">{{ formatPrice(pedidoActivo?.total) }}</span>
            </div>
          </div>

          <!-- Cambiar estado -->
          <div v-if="estadosSiguientes.length > 0" class="space-y-3">
            <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider">Actualizar estado</label>
            <div class="flex gap-3">
              <select v-model="nuevoEstado"
                class="flex-1 bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors">
                <option value="">Seleccionar estado</option>
                <option v-for="e in estadosSiguientes" :key="e.value" :value="e.value">{{ e.label }}</option>
              </select>
              <button @click="actualizarEstado" :disabled="!nuevoEstado || updatingEstado"
                class="bg-[#E31837] hover:bg-[#b5112a] disabled:opacity-50 text-white px-5 py-3 rounded-xl text-sm font-semibold transition-colors">
                {{ updatingEstado ? '...' : 'Aplicar' }}
              </button>
            </div>
            <p v-if="errorEstado" class="text-red-400 text-xs">{{ errorEstado }}</p>
          </div>

          <button @click="modal = false"
            class="w-full mt-4 border border-[#2a2a2a] hover:border-white text-white py-3 rounded-xl text-sm font-semibold transition-colors">
            Cerrar
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '@/services/api'

const pedidos = ref([])
const loading = ref(true)
const modal = ref(false)
const pedidoActivo = ref(null)
const nuevoEstado = ref('')
const updatingEstado = ref(false)
const errorEstado = ref('')
const page = ref(0)
const totalPages = ref(0)
const filtroActivo = ref('')

const filtros = [
  { label: 'Todos', value: '' },
  { label: 'Pendiente', value: 'PENDIENTE' },
  { label: 'Confirmado', value: 'CONFIRMADO' },
  { label: 'En preparación', value: 'EN_PREPARACION' },
  { label: 'Despachado', value: 'DESPACHADO' },
  { label: 'Entregado', value: 'ENTREGADO' },
  { label: 'Cancelado', value: 'CANCELADO' },
]

const ESTADO_LABELS = { PENDIENTE: 'Pendiente', CONFIRMADO: 'Confirmado', EN_PREPARACION: 'En preparación', DESPACHADO: 'Despachado', ENTREGADO: 'Entregado', CANCELADO: 'Cancelado' }
const ESTADO_BADGES = { PENDIENTE: 'bg-yellow-500/20 text-yellow-400', CONFIRMADO: 'bg-blue-500/20 text-blue-400', EN_PREPARACION: 'bg-orange-500/20 text-orange-400', DESPACHADO: 'bg-purple-500/20 text-purple-400', ENTREGADO: 'bg-green-500/20 text-green-400', CANCELADO: 'bg-red-500/20 text-red-400' }
const FLUJO = { PENDIENTE: ['CONFIRMADO', 'CANCELADO'], CONFIRMADO: ['EN_PREPARACION', 'CANCELADO'], EN_PREPARACION: ['DESPACHADO', 'CANCELADO'], DESPACHADO: ['ENTREGADO', 'CANCELADO'], ENTREGADO: [], CANCELADO: [] }

const estadosSiguientes = computed(() => {
  const sig = FLUJO[pedidoActivo.value?.estado] || []
  return sig.map(e => ({ value: e, label: ESTADO_LABELS[e] }))
})

function estadoLabel(e) { return ESTADO_LABELS[e] || e }
function estadoBadge(e) { return ESTADO_BADGES[e] || 'bg-[#2a2a2a] text-[#a0a0a0]' }
function formatPrice(p) { return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 }).format(p) }
function formatFecha(iso) { if (!iso) return '—'; return new Intl.DateTimeFormat('es-CO', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(new Date(iso)) }

async function cargar(p = 0) {
  page.value = p
  loading.value = true
  try {
    const params = new URLSearchParams({ page: String(p), size: '20' })
    if (filtroActivo.value) params.set('estado', filtroActivo.value)
    const { data } = await api.get(`/admin/pedidos?${params}`)
    pedidos.value = data.data?.content || []
    totalPages.value = data.data?.totalPages || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function cambiarFiltro(val) { filtroActivo.value = val; cargar(0) }

function abrirDetalle(pedido) {
  pedidoActivo.value = pedido
  nuevoEstado.value = ''
  errorEstado.value = ''
  modal.value = true
}

async function actualizarEstado() {
  if (!nuevoEstado.value) return
  updatingEstado.value = true
  errorEstado.value = ''
  try {
    const { data } = await api.patch(`/admin/pedidos/${pedidoActivo.value.id}/estado`, { estado: nuevoEstado.value })
    pedidoActivo.value = data.data
    nuevoEstado.value = ''
    await cargar(page.value)
  } catch (e) {
    errorEstado.value = e.response?.data?.message || 'Error al actualizar estado'
  } finally { updatingEstado.value = false }
}

onMounted(() => cargar(0))
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
