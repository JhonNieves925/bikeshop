<template>
  <div class="space-y-5">

    <!-- Encabezado -->
    <div class="flex items-center justify-between flex-wrap gap-3">
      <div>
        <h1 class="text-xl font-black text-white">Ventas</h1>
        <p class="text-[#a0a0a0] text-sm mt-0.5">
          Historial unificado · web y presencial
        </p>
      </div>
      <!-- Totalizadores rápidos -->
      <div class="flex gap-3 flex-wrap">
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-center min-w-[140px]">
          <p class="text-xs text-[#a0a0a0] uppercase tracking-wider mb-0.5">Total del período</p>
          <p class="text-white font-black text-base">{{ formatPrice(totalImporte) }}</p>
          <p class="text-[#606060] text-xs mt-0.5">{{ totalElementos }} venta{{ totalElementos !== 1 ? 's' : '' }}</p>
        </div>
        <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-center min-w-[90px]">
          <p class="text-xs text-[#a0a0a0] uppercase tracking-wider mb-0.5">En pantalla</p>
          <p class="text-[#E31837] font-black text-base">{{ ventas.length }}</p>
        </div>
      </div>
    </div>

    <!-- Filtros -->
    <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-4">
      <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
        <!-- Fuente -->
        <div>
          <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-1.5">Tipo</label>
          <select v-model="filtros.fuente"
            class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors">
            <option value="">Todos</option>
            <option value="WEB">Web (pedidos)</option>
            <option value="PRESENCIAL">Presencial (facturas)</option>
          </select>
        </div>
        <!-- Desde -->
        <div>
          <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-1.5">Desde</label>
          <input v-model="filtros.desde" type="date"
            class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors" />
        </div>
        <!-- Hasta -->
        <div>
          <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-1.5">Hasta</label>
          <input v-model="filtros.hasta" type="date"
            class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors" />
        </div>
        <!-- Cliente -->
        <div>
          <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-1.5">Cliente</label>
          <input v-model="filtros.nombre" type="text" placeholder="Buscar por nombre…"
            class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2.5 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
            @keyup.enter="buscar" />
        </div>
      </div>
      <div class="flex gap-2 mt-3">
        <button @click="buscar"
          class="bg-[#E31837] hover:bg-[#b5112a] text-white px-5 py-2 rounded-xl text-sm font-semibold transition-colors">
          Buscar
        </button>
        <button @click="limpiar"
          class="bg-[#2a2a2a] hover:bg-[#3a3a3a] text-[#a0a0a0] px-4 py-2 rounded-xl text-sm transition-colors">
          Limpiar
        </button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="space-y-2">
      <div v-for="i in 5" :key="i"
        class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-4 animate-pulse h-14" />
    </div>

    <!-- Sin resultados -->
    <div v-else-if="ventas.length === 0"
      class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-12 text-center">
      <p class="text-4xl mb-3">🧾</p>
      <p class="text-white font-semibold">No hay ventas para este filtro</p>
      <p class="text-[#a0a0a0] text-sm mt-1">Ajusta los filtros o el rango de fechas</p>
    </div>

    <!-- Tabla -->
    <div v-else class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-[#2a2a2a]">
              <th class="px-4 py-3 text-left text-xs uppercase tracking-widest text-[#a0a0a0]">#</th>
              <th class="px-4 py-3 text-left text-xs uppercase tracking-widest text-[#a0a0a0]">Fecha / Hora</th>
              <th class="px-4 py-3 text-left text-xs uppercase tracking-widest text-[#a0a0a0]">Cliente</th>
              <th class="px-4 py-3 text-left text-xs uppercase tracking-widest text-[#a0a0a0]">Tipo</th>
              <th class="px-4 py-3 text-left text-xs uppercase tracking-widest text-[#a0a0a0]">Artículos</th>
              <th class="px-4 py-3 text-right text-xs uppercase tracking-widest text-[#a0a0a0]">Total</th>
              <th class="px-4 py-3 text-center text-xs uppercase tracking-widest text-[#a0a0a0]">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <template v-for="venta in ventas" :key="venta.fuente + '-' + venta.id">
              <!-- Fila principal -->
              <tr class="border-b border-[#2a2a2a] hover:bg-[#2a2a2a]/30 transition-colors cursor-pointer"
                  @click="toggleDetalle(venta.fuente + '-' + venta.id)">
                <td class="px-4 py-3">
                  <span class="text-white font-bold text-xs">{{ venta.referencia }}</span>
                </td>
                <td class="px-4 py-3 text-[#a0a0a0] text-xs whitespace-nowrap">
                  <p>{{ formatFecha(venta.fecha) }}</p>
                  <p class="text-[#606060]">{{ formatHora(venta.fecha) }}</p>
                </td>
                <td class="px-4 py-3">
                  <p class="text-white font-medium truncate max-w-[150px]">{{ venta.nombreCliente }}</p>
                  <p class="text-[#606060] text-xs truncate max-w-[150px]">{{ venta.emailCliente }}</p>
                </td>
                <td class="px-4 py-3">
                  <span v-if="venta.fuente === 'WEB'"
                    class="px-2 py-0.5 rounded-full text-xs font-medium bg-blue-500/20 text-blue-400">
                    🌐 Web
                  </span>
                  <span v-else
                    class="px-2 py-0.5 rounded-full text-xs font-medium bg-orange-500/20 text-orange-400">
                    🏪 Presencial
                  </span>
                  <!-- Estado pedido web -->
                  <span v-if="venta.estado"
                    class="ml-1 px-1.5 py-0.5 rounded text-xs"
                    :class="estadoBadge(venta.estado)">
                    {{ estadoLabel(venta.estado) }}
                  </span>
                </td>
                <td class="px-4 py-3 text-[#a0a0a0] text-xs">
                  {{ venta.items?.length || 0 }} ítem{{ (venta.items?.length || 0) !== 1 ? 's' : '' }}
                </td>
                <td class="px-4 py-3 text-right">
                  <span class="text-[#E31837] font-bold text-sm">{{ formatPrice(venta.total) }}</span>
                  <p v-if="venta.descuento > 0" class="text-green-400 text-xs">
                    − {{ formatPrice(venta.descuento) }}
                  </p>
                </td>
                <td class="px-4 py-3 text-center">
                  <div class="flex items-center justify-center gap-2">
                    <button
                      class="text-[#a0a0a0] hover:text-white text-xs px-2 py-1 rounded bg-[#2a2a2a] hover:bg-[#3a3a3a] transition-colors"
                      @click.stop="toggleDetalle(venta.fuente + '-' + venta.id)">
                      {{ expandedKey === (venta.fuente + '-' + venta.id) ? '▲ Ocultar' : '▼ Detalle' }}
                    </button>
                    <button
                      class="text-white text-xs px-2 py-1 rounded bg-[#E31837] hover:bg-[#b5112a] transition-colors flex items-center gap-1"
                      :disabled="pdfLoading === (venta.fuente + '-' + venta.id)"
                      @click.stop="descargarPdf(venta)">
                      <span v-if="pdfLoading === (venta.fuente + '-' + venta.id)"
                        class="inline-block w-3 h-3 border border-white/30 border-t-white rounded-full animate-spin" />
                      <span v-else>📄</span>
                      PDF
                    </button>
                  </div>
                </td>
              </tr>

              <!-- Fila de detalle expandible -->
              <Transition name="expand">
                <tr v-if="expandedKey === (venta.fuente + '-' + venta.id)"
                  class="bg-[#141414]">
                  <td colspan="7" class="px-6 py-4">
                    <p class="text-xs uppercase tracking-widest text-[#a0a0a0] mb-3">
                      Detalle de ítems
                    </p>
                    <div class="space-y-1.5">
                      <div v-for="(item, idx) in venta.items" :key="idx"
                        class="flex items-center justify-between gap-4 py-1.5 border-b border-[#2a2a2a] last:border-0">
                        <div class="flex items-center gap-2 min-w-0 flex-1">
                          <!-- Badge de tipo de ítem -->
                          <span v-if="item.tieneInventario"
                            class="shrink-0 text-xs px-1.5 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20">
                            Inventario
                          </span>
                          <span v-else
                            class="shrink-0 text-xs px-1.5 py-0.5 rounded bg-yellow-500/10 text-yellow-400 border border-yellow-500/20">
                            Servicio
                          </span>
                          <p class="text-white text-sm truncate">{{ item.descripcion }}</p>
                        </div>
                        <div class="text-right shrink-0">
                          <p class="text-[#a0a0a0] text-xs">
                            {{ item.cantidad }} × {{ formatPrice(item.precioUnitario) }}
                          </p>
                          <p class="text-white text-sm font-semibold">{{ formatPrice(item.subtotal) }}</p>
                        </div>
                      </div>
                    </div>
                    <!-- Totales mini -->
                    <div class="mt-3 pt-3 border-t border-[#2a2a2a] space-y-1">
                      <div v-if="venta.descuento > 0" class="flex justify-between text-sm">
                        <span class="text-[#a0a0a0]">Subtotal</span>
                        <span class="text-[#a0a0a0]">{{ formatPrice(venta.subtotal) }}</span>
                      </div>
                      <div v-if="venta.descuento > 0" class="flex justify-between text-sm">
                        <span class="flex items-center gap-1 text-green-400">
                          🏷️ Cupón
                          <span class="bg-green-500/20 px-1.5 rounded text-xs font-mono">
                            {{ venta.codigoCupon || 'DESCUENTO' }}
                          </span>
                        </span>
                        <span class="text-green-400 font-semibold">− {{ formatPrice(venta.descuento) }}</span>
                      </div>
                      <div class="flex justify-between text-sm font-bold border-t border-[#2a2a2a] pt-1 mt-1">
                        <span class="text-white">Total</span>
                        <span class="text-[#E31837]">{{ formatPrice(venta.total) }}</span>
                      </div>
                    </div>
                    <!-- Info extra web -->
                    <div v-if="venta.fuente === 'WEB' && venta.direccionEntrega"
                      class="mt-3 pt-3 border-t border-[#2a2a2a] text-xs text-[#a0a0a0]">
                      📦 Envío: {{ venta.direccionEntrega }}, {{ venta.ciudadEntrega }}
                    </div>
                  </td>
                </tr>
              </Transition>
            </template>
          </tbody>
        </table>
      </div>

      <!-- Paginación -->
      <div v-if="totalPages > 1" class="flex items-center justify-between px-4 py-3 border-t border-[#2a2a2a]">
        <button @click="cargar(page - 1)" :disabled="page === 0"
          class="px-3 py-1.5 rounded-lg text-sm bg-[#2a2a2a] text-white disabled:opacity-40 hover:bg-[#3a3a3a] transition-colors">
          ← Anterior
        </button>
        <span class="text-sm text-[#a0a0a0]">Página {{ page + 1 }} de {{ totalPages }}</span>
        <button @click="cargar(page + 1)" :disabled="page >= totalPages - 1"
          class="px-3 py-1.5 rounded-lg text-sm bg-[#2a2a2a] text-white disabled:opacity-40 hover:bg-[#3a3a3a] transition-colors">
          Siguiente →
        </button>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/services/api'

// ── Helpers de fecha local (evita bug UTC) ────────────────────────────────────
function fechaHoy() {
  const d = new Date()
  const yyyy = d.getFullYear()
  const mm   = String(d.getMonth() + 1).padStart(2, '0')
  const dd   = String(d.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

// ── Estado ────────────────────────────────────────────────────────────────────
const ventas         = ref([])
const loading        = ref(false)
const page           = ref(0)
const totalPages     = ref(0)
const totalElementos = ref(0)
const totalImporte   = ref(0)
const expandedKey    = ref(null)
const pdfLoading     = ref(null)

// Por defecto: hoy → el total "se resetea" cada día de forma natural
const hoy = fechaHoy()
const filtros = ref({ fuente: '', desde: hoy, hasta: hoy, nombre: '' })

// ── Carga ─────────────────────────────────────────────────────────────────────
async function cargar(p = 0) {
  page.value = p
  loading.value = true
  try {
    const params = new URLSearchParams({ page: String(p), size: '20' })
    if (filtros.value.fuente)  params.set('fuente',  filtros.value.fuente)
    if (filtros.value.desde)   params.set('desde',   filtros.value.desde)
    if (filtros.value.hasta)   params.set('hasta',   filtros.value.hasta)
    if (filtros.value.nombre.trim()) params.set('nombre', filtros.value.nombre.trim())

    const { data } = await api.get(`/admin/ventas?${params}`)
    const payload = data.data || data
    ventas.value         = payload.content       || []
    totalPages.value     = payload.totalPages    || 0
    totalElementos.value = payload.totalElements || 0
    totalImporte.value   = payload.totalImporte  || 0
  } catch (e) {
    console.error(e)
    ventas.value = []
  } finally {
    loading.value = false
  }
}

function buscar() { cargar(0) }

function limpiar() {
  const hoyStr = fechaHoy()
  filtros.value = { fuente: '', desde: hoyStr, hasta: hoyStr, nombre: '' }
  cargar(0)
}

function toggleDetalle(key) {
  expandedKey.value = expandedKey.value === key ? null : key
}

// ── PDF ───────────────────────────────────────────────────────────────────────
async function descargarPdf(venta) {
  const key = venta.fuente + '-' + venta.id
  pdfLoading.value = key
  try {
    const endpoint = venta.fuente === 'WEB'
      ? `/admin/ventas/web/${venta.id}/pdf`
      : `/admin/ventas/presencial/${venta.id}/pdf`

    const response = await api.get(endpoint, { responseType: 'blob' })
    const blob = new Blob([response.data], { type: 'application/pdf' })
    const url  = URL.createObjectURL(blob)
    // Abrir en nueva pestaña — el navegador muestra el visor de PDF nativo
    window.open(url, '_blank')
    // Liberar memoria después de abrir
    setTimeout(() => URL.revokeObjectURL(url), 5000)
  } catch (e) {
    console.error('Error generando PDF', e)
    alert('No se pudo generar el PDF. Intenta de nuevo.')
  } finally {
    pdfLoading.value = null
  }
}

// ── Formatters ────────────────────────────────────────────────────────────────
function formatPrice(v) {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency', currency: 'COP', minimumFractionDigits: 0
  }).format(v ?? 0)
}

function formatFecha(d) {
  if (!d) return '—'
  return new Intl.DateTimeFormat('es-CO', { year: 'numeric', month: 'short', day: 'numeric' }).format(new Date(d))
}

function formatHora(d) {
  if (!d) return ''
  return new Intl.DateTimeFormat('es-CO', { hour: '2-digit', minute: '2-digit', hour12: true }).format(new Date(d))
}

const ESTADO_LABELS = {
  PENDIENTE: 'Pendiente', CONFIRMADO: 'Confirmado', EN_PREPARACION: 'En prep.',
  DESPACHADO: 'Despachado', ENTREGADO: 'Entregado', CANCELADO: 'Cancelado'
}
const ESTADO_CLASES = {
  PENDIENTE: 'bg-yellow-500/20 text-yellow-400', CONFIRMADO: 'bg-blue-500/20 text-blue-400',
  EN_PREPARACION: 'bg-orange-500/20 text-orange-400', DESPACHADO: 'bg-purple-500/20 text-purple-400',
  ENTREGADO: 'bg-green-500/20 text-green-400', CANCELADO: 'bg-red-500/20 text-red-400'
}
function estadoLabel(e)  { return ESTADO_LABELS[e] || e }
function estadoBadge(e)  { return ESTADO_CLASES[e] || 'bg-[#2a2a2a] text-[#a0a0a0]' }

onMounted(() => cargar(0))
</script>

<style scoped>
.expand-enter-active, .expand-leave-active {
  transition: opacity 0.15s ease, max-height 0.2s ease;
  overflow: hidden;
  max-height: 600px;
}
.expand-enter-from, .expand-leave-to { opacity: 0; max-height: 0; }
</style>
